from typing import Optional
from fastapi import FastAPI, Request, Form, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse, RedirectResponse
from sqlalchemy.orm import Session

from .app.db import session as db_session
from .app.models import sqlalchemy_models as models
from .app.crud import crud_auth

app = FastAPI(
    title="CrimeWatch API",
    description="Backend API for CrimeWatch web and mobile applications",
    version="1.0.0"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount templates
templates = Jinja2Templates(directory="../web_app")

@app.get("/health")
async def health_check():
    return {"status": "healthy", "message": "API is running"}

# Landing page
@app.get("/", response_class=HTMLResponse)
async def landing_page(request: Request):
    # show transient flash messages set via cookies (e.g., after logout)
    flash = request.cookies.get("flash")
    response = templates.TemplateResponse("index.html", {"request": request, "flash": flash})
    if flash:
        # clear the flash cookie so the message shows only once
        response.delete_cookie("flash")
    return response

# External Organization registration
@app.get("/register", response_class=HTMLResponse)
async def register_page(request: Request):
    return templates.TemplateResponse("register.html", {"request": request})

@app.post("/register")
async def register_organization(
    request: Request,
    org_name: str = Form(...),
    contact_person: str = Form(...),
    contact_email: str = Form(...),
    contact_phone: str = Form(...),
    password: str = Form(...),
    confirm_password: str = Form(...),
    db: Session = Depends(db_session.get_db),
):
    if password != confirm_password:
        return templates.TemplateResponse(
            "register.html",
            {"request": request, "error": "Passwords do not match"}
        )

    # ensure tables exist
    models.Base.metadata.create_all(bind=db_session.engine)

    existing = crud_auth.get_org_by_email(db, contact_email)
    if existing:
        return templates.TemplateResponse(
            "register.html",
            {"request": request, "error": "An organization with that email already exists"}
        )

    org = crud_auth.create_org(db, org_name, contact_person, contact_email, contact_phone, password)

    # redirect to org login with a flag
    return RedirectResponse(url="/org/login?registered=1", status_code=303)

# Admin Routes
@app.get("/admin/login", response_class=HTMLResponse)
async def admin_login_page(request: Request):
    return templates.TemplateResponse("admin_login.html", {"request": request})

@app.post("/admin/login")
async def admin_login(request: Request, admin_id: str = Form(...), password: str = Form(...), db: Session = Depends(db_session.get_db)):
    models.Base.metadata.create_all(bind=db_session.engine)
    adm = crud_auth.authenticate_admin(db, admin_id, password)
    if not adm:
        return templates.TemplateResponse("admin_login.html", {"request": request, "error": "Invalid credentials"})

    # successful admin login: create a session and set a secure cookie, then redirect to admin dashboard
    sess = crud_auth.create_session(db, user_type="admin", user_id=adm.admin_id)
    response = RedirectResponse(url="/admin/dashboard", status_code=303)
    response.set_cookie("session_token", sess.token, httponly=True, samesite="lax", max_age=24 * 3600)
    return response


@app.get("/admin/dashboard", response_class=HTMLResponse)
async def admin_dashboard(request: Request, db: Session = Depends(db_session.get_db)):
    """Render the admin dashboard.

    Requires a valid `session_token` cookie created at login. If the cookie is missing
    or invalid the user is redirected to the admin login page.
    """
    token = request.cookies.get("session_token")
    sess = None
    if token:
        sess = crud_auth.get_session_by_token(db, token)

    if not sess or sess.user_type != "admin":
        return RedirectResponse(url="/admin/login", status_code=303)

    admin = crud_auth.get_admin_by_id(db, sess.user_id)
    return templates.TemplateResponse("admin_dashboard.html", {"request": request, "admin": admin})


@app.post("/admin/logout")
async def admin_logout(request: Request, db: Session = Depends(db_session.get_db)):
    token = request.cookies.get("session_token")
    if token:
        crud_auth.delete_session(db, token)
    response = RedirectResponse(url="/", status_code=303)
    # set a short-lived flash message and clear the session cookie
    response.set_cookie("flash", "Logged out successfully", max_age=5)
    response.delete_cookie("session_token")
    return response


@app.get("/admin/logout")
async def admin_logout_get(request: Request, db: Session = Depends(db_session.get_db)):
    token = request.cookies.get("session_token")
    if token:
        crud_auth.delete_session(db, token)
    response = RedirectResponse(url="/", status_code=303)
    response.set_cookie("flash", "Logged out successfully", max_age=5)
    response.delete_cookie("session_token")
    return response

# Reporter API endpoints for mobile app
@app.post('/api/reporters/register')
async def api_register_reporter(payload: dict, db: Session = Depends(db_session.get_db)):
    models.Base.metadata.create_all(bind=db_session.engine)
    alias = payload.get('alias')
    password = payload.get('password')
    email = payload.get('email')
    phone = payload.get('phone')
    if not alias or not password:
        return {"error": "alias and password required"}
    existing = db.query(models.Reporter).filter(models.Reporter.alias == alias).first()
    if existing:
        return {"error": "alias already exists"}
    rep = crud_auth.create_reporter(db, alias, password, email=email, phone=phone)
    return {"status": "ok", "reporter_id": rep.reporter_id}

@app.post('/api/reporters/login')
async def api_login_reporter(payload: dict, db: Session = Depends(db_session.get_db)):
    models.Base.metadata.create_all(bind=db_session.engine)
    identifier = payload.get('identifier')
    password = payload.get('password')
    if not identifier or not password:
        return {"error": "identifier and password required"}
    rep = crud_auth.authenticate_reporter(db, identifier, password)
    if not rep:
        return {"error": "invalid credentials"}
    return {"status": "ok", "reporter_id": rep.reporter_id}

# External Organization Routes
@app.get("/org/login", response_class=HTMLResponse)
async def org_login_page(request: Request):
    return templates.TemplateResponse("external_org_login.html", {"request": request})

@app.post("/org/login")
async def org_login(request: Request, org_id: str = Form(...), password: str = Form(...), db: Session = Depends(db_session.get_db)):
    models.Base.metadata.create_all(bind=db_session.engine)
    org = crud_auth.authenticate_org(db, org_id, password)
    if not org:
        # pass back an error message to the template
        return templates.TemplateResponse("external_org_login.html", {"request": request, "error": "Invalid credentials"})
    # successful login: create a session and set a secure cookie, then redirect to dashboard
    sess = crud_auth.create_session(db, user_type="external_org", user_id=org.org_id)
    response = RedirectResponse(url="/org/dashboard", status_code=303)
    # set httpOnly cookie; max_age matches session validity in seconds (24h default)
    response.set_cookie("session_token", sess.token, httponly=True, samesite="lax", max_age=24 * 3600)
    return response


@app.get("/org/dashboard", response_class=HTMLResponse)
async def org_dashboard(request: Request, db: Session = Depends(db_session.get_db)):
    """Render the external organization dashboard.

    This endpoint requires a valid `session_token` cookie created at login.
    If the cookie is missing or invalid the user is redirected to the login page.
    """
    token = request.cookies.get("session_token")
    sess = None
    if token:
        sess = crud_auth.get_session_by_token(db, token)

    if not sess or sess.user_type != "external_org":
        # not authenticated; redirect to login
        return RedirectResponse(url="/org/login", status_code=303)

    org = crud_auth.get_org_by_id(db, sess.user_id)
    # provide template-friendly fields
    org_name = org.org_name if org else None
    reports = []
    return templates.TemplateResponse(
        "external_org_dashboard.html",
        {"request": request, "org": org, "org_name": org_name, "reports": reports}
    )


@app.post("/org/logout")
async def org_logout(request: Request, db: Session = Depends(db_session.get_db)):
    token = request.cookies.get("session_token")
    if token:
        crud_auth.delete_session(db, token)
    response = RedirectResponse(url="/", status_code=303)
    # clear cookie
    response.delete_cookie("session_token")
    return response


@app.get("/org/logout")
async def org_logout_get(request: Request, db: Session = Depends(db_session.get_db)):
    # allow GET for simple logout link usage
    token = request.cookies.get("session_token")
    if token:
        crud_auth.delete_session(db, token)
    response = RedirectResponse(url="/", status_code=303)
    response.delete_cookie("session_token")
    return response