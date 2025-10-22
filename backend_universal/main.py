from fastapi import FastAPI, Request, Form
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from fastapi.responses import RedirectResponse, JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException
from pathlib import Path
import logging
import sqlalchemy
from backend_universal.database import Base, engine, SessionLocal
from backend_universal import models

logger = logging.getLogger("backend_universal")
logging.basicConfig(level=logging.INFO)

app = FastAPI(title="Crime Reporting API")

# Resolve path to the project's Web-app templates directory
BASE_DIR = Path(__file__).resolve().parent.parent
TEMPLATES_DIR = BASE_DIR / "Web-app" / "templates"
STATIC_DIR = BASE_DIR / "Web-app" / "static"

templates = None
if TEMPLATES_DIR.exists():
    templates = Jinja2Templates(directory=str(TEMPLATES_DIR))

# Mount static files if folder exists
if STATIC_DIR.exists():
    app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")


@app.on_event("startup")
def startup_event():
    """Attempt to create DB tables on startup. Log errors instead of failing import.
    This avoids crashes when the DB is temporarily unavailable during import time.
    """
    try:
        logger.info("Creating database tables (if not exist)")
        Base.metadata.create_all(bind=engine)
        # quick test connection
        with engine.connect() as conn:
            conn.execute(sqlalchemy.text("SELECT 1"))
        logger.info("Database ready")
    except Exception as e:
        logger.exception("Database initialization failed: %s", e)


@app.get("/")
def root(request: Request):
    # If templates are available, render index.html from Web-app/templates
    if templates:
        try:
            return templates.TemplateResponse("index.html", {"request": request})
        except Exception as e:
            logger.exception("Failed to render template: %s", e)
            return {"message": "Backend API is running (template render failed)", "error": str(e)}

    return {"message": "Backend API is running"}


@app.get("/api")
def api_root():
    return {"message": "Backend API is running"}


# -----------------
# Admin routes (templates expect /admin/*)
# -----------------


@app.get("/admin/login")
def admin_login_get(request: Request):
    if templates:
        return templates.TemplateResponse("admin_login.html", {"request": request})
    return {"message": "admin login"}


@app.post("/admin/login")
def admin_login_post(request: Request, admin_id: str = Form(...), password: str = Form(...)):
    # very small, non-production auth: check for admin existence and redirect to dashboard
    try:
        db = SessionLocal()
        admin = db.query(models.Admin).filter(models.Admin.username == admin_id).first()
    except Exception:
        admin = None
    finally:
        try:
            db.close()
        except Exception:
            pass

    if not admin and templates:
        return templates.TemplateResponse("admin_login.html", {"request": request, "error": "Invalid credentials"})

    # Redirect to admin dashboard (pass admin_id as query param)
    return RedirectResponse(url=f"/admin/dashboard?admin_id={admin_id}", status_code=303)


@app.get("/admin/dashboard")
def admin_dashboard(request: Request, admin_id: str = None):
    # Gather recent reports for the admin dashboard (map to template fields)
    reports = []
    try:
        db = SessionLocal()
        rows = db.query(models.Report).order_by(models.Report.date_reported.desc()).limit(20).all()
        for r in rows:
            reporter_name = getattr(r.reporter, "alias", None) or getattr(r.reporter, "full_name", None) or "Anonymous"
            reports.append({
                "reporter_name": reporter_name,
                "description": r.description,
                "report_datetime": r.date_reported,
            })
    except Exception:
        reports = []
    finally:
        try:
            db.close()
        except Exception:
            pass

    supports = []
    data_requests = []

    if templates:
        return templates.TemplateResponse("admin_dashboard.html", {"request": request, "admin_id": admin_id or "admin", "reports": reports, "supports": supports, "data_requests": data_requests})

    return {"admin": admin_id, "reports": reports}


@app.get("/admin/logout")
def admin_logout():
    return RedirectResponse(url="/", status_code=303)


@app.post("/admin/support/fix")
def admin_support_fix(support_id: str = Form(...), admin_id: str = Form(...)):
    # placeholder: would mark support fixed; redirect back to dashboard
    return RedirectResponse(url=f"/admin/dashboard?admin_id={admin_id}", status_code=303)


@app.post("/admin/requests/respond")
def admin_requests_respond(request_id: str = Form(...), admin_id: str = Form(...), action: str = Form(...)):
    # placeholder: would process approve/reject
    return RedirectResponse(url=f"/admin/dashboard?admin_id={admin_id}", status_code=303)


# -----------------
# Organisation routes
# -----------------


@app.get("/org/login")
def org_login_get(request: Request):
    if templates:
        return templates.TemplateResponse("org_login.html", {"request": request})
    return {"message": "org login"}


@app.post("/org/login")
def org_login_post(request: Request, org_id: str = Form(...), password: str = Form(...)):
    # minimal check for organisation; redirect to org dashboard
    try:
        db = SessionLocal()
        org = db.query(models.ExternalOrg).filter(models.ExternalOrg.org_name == org_id).first()
    except Exception:
        org = None
    finally:
        try:
            db.close()
        except Exception:
            pass

    if not org and templates:
        return templates.TemplateResponse("org_login.html", {"request": request, "error": "Invalid credentials"})

    return RedirectResponse(url=f"/org/dashboard?org_name={org_id}", status_code=303)


@app.get("/org/dashboard")
def org_dashboard(request: Request, org_name: str = None):
    # placeholder: render org_dashboard.html and pass an empty reports list unless data exists
    reports = []
    if templates:
        return templates.TemplateResponse("org_dashboard.html", {"request": request, "org_name": org_name or "Org", "reports": reports})
    return {"org": org_name, "reports": reports}


@app.get("/org/logout")
def org_logout():
    return RedirectResponse(url="/", status_code=303)


@app.post("/org/dashboard")
def org_dashboard_search():
    # placeholder for POST search; simply redirect back
    return RedirectResponse(url="/org/dashboard", status_code=303)


@app.get("/index.html")
def index_html_redirect():
    return RedirectResponse(url="/")


@app.exception_handler(StarletteHTTPException)
async def custom_http_exception_handler(request: Request, exc: StarletteHTTPException):
    # If a 404 and we have templates, serve the SPA landing page so client-side routing still works
    if exc.status_code == 404 and templates:
        try:
            return templates.TemplateResponse("index.html", {"request": request}, status_code=404)
        except Exception:
            # fall back to JSON
            return JSONResponse({"detail": "Not Found"}, status_code=404)

    return JSONResponse({"detail": exc.detail}, status_code=exc.status_code)


@app.get("/health")
def health():
    try:
        with engine.connect() as conn:
            conn.execute(sqlalchemy.text("SELECT 1"))
        return {"healthy": True}
    except Exception as e:
        logger.warning("Health check failed: %s", e)
        return {"healthy": False, "error": str(e)}


if __name__ == "__main__":
    # Allow running directly: python main.py -> starts uvicorn
    import uvicorn

    uvicorn.run("backend_universal.main:app", host="127.0.0.1", port=8000, reload=True)
