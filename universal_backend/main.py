from typing import Optional, List
from fastapi import FastAPI, Request, Form, Depends, APIRouter, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from sqlalchemy.orm import Session
import os
from uuid import uuid4
from datetime import datetime, timedelta, timezone
from pydantic import BaseModel
import json
from fastapi import Body
UPLOAD_DIR = os.environ.get("REPORT_UPLOAD_DIR", r"D:\Project\crime-app\uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)
from starlette import status
from fastapi import Query
from app.db.session import get_db

from app.routers import analytics_router, org_analytics_router, admin_management_router
from app.db import session as db_session
from app.models import sqlalchemy_models as models
from app.crud import crud_auth, crud_reports



class ReporterUpdate(BaseModel):
    alias: Optional[str] = None
    f_name: Optional[str] = None
    l_name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None

class ReportRequest(BaseModel):
    reporter_id: int
    crime_type_id: int
    description: str
    area_id: int
    report_type: Optional[str] = "sighting"
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    imageUrls: Optional[List[str]] = []
    occurrence_time: Optional[str] = None

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

router = APIRouter()
app.include_router(analytics_router.router, prefix="/api")
app.include_router(org_analytics_router.router, prefix="/api/org")
app.include_router(admin_management_router.router, prefix="/api/admin")
app.mount(
    "/static",
    StaticFiles(directory=r"D:\Project\crime-app\web_app\static"),
    name="static"
)
app.mount("/uploads", StaticFiles(directory="D:/Project/crime-app/uploads"), name="uploads")



# Mount templates
templates = Jinja2Templates(directory="D:\Project\crime-app\web_app")

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
    return templates.TemplateResponse("org_register.html", {"request": request})

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
            "org_register.html",
            {"request": request, "error": "Passwords do not match"}
        )
    # ensure tables exist
    models.Base.metadata.create_all(bind=db_session.engine)
    existing = crud_auth.get_org_by_email(db, contact_email)
    if existing:
        return templates.TemplateResponse(
            "org_register.html",
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
    adm.last_login = datetime.now(timezone.utc)
    db.commit()
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

@app.post("/api/reporters/login")
async def api_login_reporter(payload: dict, db: Session = Depends(db_session.get_db)):
    models.Base.metadata.create_all(bind=db_session.engine)
    identifier = payload.get("identifier")
    password = payload.get("password")
    if not identifier or not password:
        return {"error": "identifier and password required"}
    rep = crud_auth.authenticate_reporter(db, identifier, password)
    if not rep:
        return {"error": "invalid credentials"}
    # Return profile details immediately
    return {
        "status": "ok",
        "reporter_id": rep.reporter_id,
        "alias": rep.alias,
        "f_name": rep.f_name,
        "l_name": rep.l_name,
        "email": rep.email,
        "phone": rep.phone,
        "date_joined": rep.date_joined,
        "last_login": rep.last_login,
    }

@app.get("/api/reporters/{reporter_id}")
async def api_get_reporter(reporter_id: int, db: Session = Depends(db_session.get_db)):
    rep = crud_auth.get_reporter_by_id(db, reporter_id)
    if not rep:
        return {"error": "reporter not found"}
    return {
        "alias": rep.alias,
        "f_name": rep.f_name,
        "l_name": rep.l_name,
        "email": rep.email,
        "phone": rep.phone,
    }

@app.put("/api/reporters/{reporter_id}/update")
async def api_update_reporter(
    reporter_id: int,
    payload: ReporterUpdate,
    db: Session = Depends(db_session.get_db)
):
    rep = crud_auth.get_reporter_by_id(db, reporter_id)
    if not rep:
        return {"error": "reporter not found"}

    updated_rep = crud_auth.update_reporter(
        db,
        rep,
        alias=payload.alias,
        f_name=payload.f_name,
        l_name=payload.l_name,
        email=payload.email,
        phone=payload.phone
    )

    return updated_rep

@app.post("/api/reports")
async def api_create_report(
    reporter_id: int = Form(...),
    crime_type_id: int = Form(...),
    location_id: int = Form(...),
    occurrence_time: str = Form(...),
    description: str = Form(...),
    db: Session = Depends(db_session.get_db),
):
    reporter = db.query(models.Reporter).filter(models.Reporter.reporter_id == reporter_id).first()
    if not reporter:
        raise HTTPException(status_code=404, detail="Reporter not found")
    try:
        occ_time = datetime.strptime(occurrence_time, "%d/%m/%Y %H:%M")
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid occurrence_time format. Use 'DD/MM/YYYY HH:MM'")
    new_report = crud_reports.create_report(
        db=db,
        reporter_id=reporter_id,
        crime_type_id=crime_type_id,
        location_id=location_id,
        description=description,
        occurrence_time=occ_time,
    )

    return {
        "status": "ok",
        "report_id": new_report.report_id,
        "message": "Report created successfully",
        "timestamp": new_report.date_reported,
    }

# -------------------------
# Add report addons
# -------------------------
@app.post("/api/reports/{report_id}/addons")
async def api_add_report_addon(
    report_id: int,
    file: UploadFile = File(...),
    db: Session = Depends(db_session.get_db),
):
    rpt = db.query(models.Report).filter(models.Report.report_id == report_id).first()
    if not rpt:
        raise HTTPException(status_code=404, detail="Report not found")
    file_type = (
        "image" if "image" in file.content_type else
        "video" if "video" in file.content_type else
        "other"
    )
    filename = f"{uuid4().hex}_{file.filename}"
    file_path = os.path.join(UPLOAD_DIR, filename)
    with open(file_path, "wb") as f:
        f.write(await file.read())
    size = os.path.getsize(file_path)
    addon = crud_reports.add_report_addon(db, report_id, file_path, file_type, size)
    return {
        "status": "ok",
        "addon_id": addon.addon_id,
        "file_path": addon.file_path,
        "file_type": addon.file_type,
    }

@app.get("/api/crime-types")
async def get_crime_types(db: Session = Depends(db_session.get_db)):
    """Return all available crime types for dropdowns."""
    types = db.query(models.CrimeType).all()
    return [{"id": t.crime_type_id, "name": t.name, "description": t.description} for t in types]

@app.get("/api/locations")
async def get_locations(db: Session = Depends(db_session.get_db)):
    locs = db.query(models.Location).all()
    return [{"id": l.location_id, "area": l.area, "latitude": l.latitude, "longitude": l.longitude} for l in locs]

@app.post("/api/reports/upload")
async def upload_media(file: UploadFile = File(...)):
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    with open(file_path, "wb") as f:
        f.write(await file.read())
    return {"filename": file.filename, "url": f"/uploads/{file.filename}"}
app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")

@app.delete("/api/reporters/{reporter_id}", status_code=200)
async def api_delete_reporter(
    reporter_id: int,
    db: Session = Depends(db_session.get_db),
    # optionally protect with cookie/session token or require body confirmation
    confirm: bool = Query(False, description="Must be true to confirm deletion")
):
    """
    Delete a reporter account and related data.
    To avoid accidental deletes the client **must** call with ?confirm=true
    """
    if not confirm:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="You must set confirm=true to delete account")

    # Basic auth check: confirm the reporter exists and optionally verify session/user identity
    rep = crud_auth.get_reporter_by_id(db, reporter_id)
    if not rep:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Reporter not found")

    # OPTIONAL: verify the request is made by the same user (session token) — strongly recommended
    # token = request.cookies.get("session_token") or check Authorization header
    # sess = crud_auth.get_session_by_token(db, token)
    # if not sess or sess.user_type != "reporter" or sess.user_id != reporter_id:
    #     raise HTTPException(status_code=401, detail="Not authorized")

    success = crud_auth.delete_reporter(db, reporter_id)
    if not success:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Delete failed")
    return {"status": "ok", "message": "Account deleted"}

# External Organization Routes
@app.get("/org/login", response_class=HTMLResponse)
async def org_login_page(request: Request):
    return templates.TemplateResponse("org_login.html", {"request": request})

@app.post("/org/login")
async def org_login(request: Request, org_id: str = Form(...), password: str = Form(...), db: Session = Depends(db_session.get_db)):
    models.Base.metadata.create_all(bind=db_session.engine)
    org = crud_auth.authenticate_org(db, org_id, password)
    if not org:
        # pass back an error message to the template
        return templates.TemplateResponse("org_login.html", {"request": request, "error": "Invalid credentials"})
    # successful login: create a session and set a secure cookie, then redirect to dashboard
    sess = crud_auth.create_session(db, user_type="external_org", user_id=org.org_id)
    response = RedirectResponse(url="/org/dashboard", status_code=303)
    # set httpOnly cookie; max_age matches session validity in seconds (24h default)
    response.set_cookie("session_token", sess.token, httponly=True, samesite="lax", max_age=24 * 3600)
    return response

# -------------------------
# Upload file for a report (image/video)
# -------------------------
@app.post("/api/reports/{report_id}/addons")
async def api_upload_report_addon(report_id: int, file: UploadFile = File(...), db: Session = Depends(db_session.get_db)):
    """
    Multipart upload:
      - file: binary file (image/video)
    Response:
      { "status": "ok", "addon_id": 1, "file_path": "..." }
    """
    # verify report exists
    rpt = db.query(models.Report).filter(models.Report.report_id == report_id).first()
    if not rpt:
        raise HTTPException(status_code=404, detail="report not found")
    # simple validation for file type (image/video) based on content type
    content_type = file.content_type or ""
    if "image" in content_type:
        file_type = "image"
    elif "video" in content_type:
        file_type = "video"
    else:
        # default to image — or reject
        raise HTTPException(status_code=400, detail="file must be image or video")
    # save file on disk
    ext = os.path.splitext(file.filename)[1] or (".jpg" if file_type == "image" else ".mp4")
    filename = f"{uuid4().hex}{ext}"
    dest_path = os.path.join(UPLOAD_DIR, filename)
    with open(dest_path, "wb") as f:
        content = await file.read()
        f.write(content)
    size = os.path.getsize(dest_path)
    addon = crud_reports.add_report_addon(db=db, report_id=report_id, file_path=dest_path, file_type=file_type, file_size=size)
    return {"status": "ok", "addon_id": addon.addon_id, "file_path": addon.file_path}

@app.get("/org/dashboard", response_class=HTMLResponse)
async def org_dashboard(request: Request, db: Session = Depends(db_session.get_db)):
    token = request.cookies.get("session_token")
    sess = crud_auth.get_session_by_token(db, token) if token else None
    if not sess or sess.user_type != "external_org":
        return RedirectResponse(url="/org/login", status_code=303)
    org = crud_auth.get_org_by_id(db, sess.user_id)
    # Fetch all crime types for the dropdown
    crime_types = db.query(models.CrimeType).all()
    reports = []
    return templates.TemplateResponse(
        "org_dashboard.html",
        {
            "request": request,
            "org_name": org.org_name if org else None,
            "reports": reports,
            "crime_types": crime_types,  # 👈 pass to template
        }
    )

@app.get("/org/dashboard", response_class=HTMLResponse)
async def org_dashboard(request: Request, db: Session = Depends(db_session.get_db)):
    token = request.cookies.get("session_token")
    sess = crud_auth.get_session_by_token(db, token) if token else None
    if not sess or sess.user_type != "external_org":
        return RedirectResponse(url="/org/login", status_code=303)
    org = crud_auth.get_org_by_id(db, sess.user_id)
    # Ensure tables exist (important)
    models.Base.metadata.create_all(bind=db_session.engine)
    # Fetch all crime types for dropdown
    crime_types = db.query(models.CrimeType).all()
    # Get all reports (you can later filter or paginate)
    reports = (
        db.query(models.Report)
        .join(models.CrimeType)
        .join(models.Reporter)
        .order_by(models.Report.date_reported.desc())
        .all()
    )
    formatted_reports = []
    for r in reports:
        formatted_reports.append({
            "id": r.report_id,
            "type": r.crime_type.name if r.crime_type else "N/A",
            "reporter": r.reporter.alias if r.reporter else "Unknown",
            "description": r.description[:80] + "..." if r.description else "",
            "date": r.date_reported.strftime("%d %b %Y %H:%M") if r.date_reported else "N/A",
            "location": getattr(r.location, "area", "N/A") if hasattr(r, "location") else "N/A",
        })
    return templates.TemplateResponse(
        "org_dashboard.html",
        {
            "request": request,
            "org_name": org.org_name if org else None,
            "crime_types": crime_types,
            "reports": formatted_reports,
        },
    )

@app.post("/org/dashboard", response_class=HTMLResponse)
async def org_dashboard_search(
    request: Request,
    type: str = Form(""),
    date_from: str = Form(""),
    date_to: str = Form(""),
    db: Session = Depends(db_session.get_db),
):
    token = request.cookies.get("session_token")
    sess = crud_auth.get_session_by_token(db, token) if token else None
    if not sess or sess.user_type != "external_org":
        return RedirectResponse(url="/org/login", status_code=303)
    org = crud_auth.get_org_by_id(db, sess.user_id)
    models.Base.metadata.create_all(bind=db_session.engine)
    # Fetch dropdown data again
    crime_types = db.query(models.CrimeType).all()
    query = db.query(models.Report).join(models.CrimeType).join(models.Reporter)
    if type:
        query = query.filter(models.CrimeType.name.ilike(f"%{type}%"))
    if date_from:
        try:
            query = query.filter(models.Report.occurrence_time >= datetime.strptime(date_from, "%Y-%m-%d"))
        except ValueError:
            pass
    if date_to:
        try:
            query = query.filter(models.Report.occurrence_time <= datetime.strptime(date_to, "%Y-%m-%d"))
        except ValueError:
            pass
    reports = query.order_by(models.Report.date_reported.desc()).all()
    formatted_reports = []
    for r in reports:
        formatted_reports.append({
            "id": r.report_id,
            "type": r.crime_type.name if r.crime_type else "N/A",
            "reporter": r.reporter.alias if r.reporter else "Unknown",
            "description": r.description[:80] + "..." if r.description else "",
            "date": r.date_reported.strftime("%d %b %Y %H:%M") if r.date_reported else "N/A",
            "location": getattr(r.location, "area", "N/A") if hasattr(r, "location") else "N/A",
        })
    return templates.TemplateResponse(
        "org_dashboard.html",
        {
            "request": request,
            "org_name": org.org_name if org else "Unknown Organization",
            "reports": formatted_reports,
            "crime_types": crime_types,
        },
    )

@app.get("/org/logout")
async def org_logout_get(request: Request, db: Session = Depends(db_session.get_db)):
    # allow GET for simple logout link usage
    token = request.cookies.get("session_token")
    if token:
        crud_auth.delete_session(db, token)
    response = RedirectResponse(url="/", status_code=303)
    response.delete_cookie("session_token")
    return response

@app.get("/api/analytics/regions")
def region_heatmap(days: int = 30, db: Session = Depends(get_db)):
    since = datetime.utcnow() - timedelta(days=days)

    regions = db.query(models.Location).all()

    results = []

    for r in regions:
        count = (
            db.query(models.Report)
            .filter(
                models.Report.location_id == r.location_id,
                models.Report.date_reported >= since
            )
            .count()
        )

        # Color logic
        level = (
            "High" if count >= 20 else
            "Medium" if count >= 5 else
            "Low"
        )

        results.append({
            "location_id": r.location_id,
            "area": r.area,
            "count": count,
            "level": level
        })

    return results

@app.get("/org/report/{report_id}", response_class=HTMLResponse)
async def org_view_report(report_id: int, request: Request, db: Session = Depends(get_db)):
    report = (
        db.query(models.Report)
        .filter(models.Report.report_id == report_id)
        .first()
    )

    if not report:
        raise HTTPException(status_code=404, detail="Report not found")

    return templates.TemplateResponse(
        "org_view_report.html",
        {
            "request": request,
            "report": report
        }
    )

@app.get("/org/report/{report_id}/files", response_class=HTMLResponse)
async def org_view_report_files(report_id: int, request: Request, db: Session = Depends(get_db)):
    report = db.query(models.Report).filter(models.Report.report_id == report_id).first()

    if not report:
        raise HTTPException(status_code=404, detail="Report not found")

    return templates.TemplateResponse(
        "org_report_files.html",
        {"request": request, "report": report, "files": report.addons}
    )

@app.get("/reporter/{reporter_id}/reports", response_class=HTMLResponse)
def reporter_reports(
    reporter_id: int,
    request: Request,
    db: Session = Depends(get_db)
):
    # Fetch reporter
    reporter = (
        db.query(models.Reporter)
        .filter(models.Reporter.reporter_id == reporter_id)
        .first()
    )

    if not reporter:
        return templates.TemplateResponse(
            "error.html",
            {"request": request, "message": "Reporter not found"},
            status_code=404
        )

    # Fetch reports with addons, crime type, and location
    reports = (
        db.query(models.Report)
        .filter(models.Report.reporter_id == reporter_id)
        .all()
    )

    return templates.TemplateResponse(
        "reporter_reports.html",
        {
            "request": request,
            "reporter": reporter,
            "reports": reports
        }
    )

@app.post("/reports/{report_id}/delete")
def delete_report(report_id: int, db: Session = Depends(get_db)):
    report = db.query(models.Report).filter(models.Report.report_id == report_id).first()

    if not report:
        raise HTTPException(404, "Report not found")

    # Delete associated addon files
    for addon in report.addons:
        file_path = f"uploads/{addon.file_path}"
        if os.path.exists(file_path):
            os.remove(file_path)

    # Delete report
    db.delete(report)
    db.commit()

    return RedirectResponse(url="/admin/dashboard", status_code=303)
