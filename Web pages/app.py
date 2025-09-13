from fastapi import FastAPI, Form, Request, Depends
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
import mysql.connector
from mysql.connector import Error
import uvicorn

# Initialize FastAPI
app = FastAPI()

# Templates folder
templates = Jinja2Templates(directory="templates")

# Database connection helper
def get_db_connection():
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",         # change to your MySQL user
            password="",         # change to your MySQL password
            database="crime_app" # change to your DB name
        )
        return conn
    except Error as e:
        print("Error while connecting to MySQL:", e)
        return None


# GET landing page
@app.get("/", response_class=HTMLResponse)
async def landing_page(request: Request):
    return templates.TemplateResponse("landing.html", {"request": request})

# GET admin login page
@app.get("/admin/login", response_class=HTMLResponse)
async def login_page(request: Request):
    return templates.TemplateResponse("admin_login.html", {"request": request, "error": None})


# POST login form
@app.post("/login", response_class=HTMLResponse)
async def login(request: Request, admin_id: str = Form(...), password: str = Form(...)):
    conn = get_db_connection()
    if conn:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM administrator WHERE admin_id = %s AND password = %s", (admin_id, password))
        result = cursor.fetchone()
        cursor.close()
        conn.close()

        if result:
            # Redirect to dashboard with admin_id as query parameter
            response = RedirectResponse(url=f"/dashboard?admin_id={admin_id}", status_code=303)
            return response
        else:
            return templates.TemplateResponse("admin_login.html", {"request": request, "error": "Invalid credentials"})

    return templates.TemplateResponse("admin_login.html", {"request": request, "error": "Database connection failed"})

# GET dashboard page
@app.get("/dashboard", response_class=HTMLResponse)
async def dashboard(request: Request, admin_id: str = None):
    return templates.TemplateResponse("admin_dashboard.html", {"request": request, "admin_id": admin_id})





# Logout route (redirects to landing page for both admin and org)
@app.get("/logout")
async def logout():
    return RedirectResponse(url="/", status_code=303)


# GET organization login page
@app.get("/org/login", response_class=HTMLResponse)
async def org_login_page(request: Request):
    return templates.TemplateResponse("org_login.html", {"request": request, "error": None})

# POST organization login form
@app.post("/org/login", response_class=HTMLResponse)
async def org_login(request: Request, org_id: str = Form(...), password: str = Form(...)):
    conn = get_db_connection()
    if conn:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM external_orgs WHERE org_id = %s AND password = %s", (org_id, password))
        result = cursor.fetchone()
        cursor.close()
        conn.close()

        if result:
            response = RedirectResponse(url=f"/org/dashboard?org_id={org_id}", status_code=303)
            return response
        else:
            return templates.TemplateResponse("org_login.html", {"request": request, "error": "Invalid credentials"})

    return templates.TemplateResponse("org_login.html", {"request": request, "error": "Database connection failed"})


# Helper functions for org dashboard

# Get org name from external_orgs
def get_org_name(org_id):
    conn = get_db_connection()
    name = ""
    if conn:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT name FROM external_orgs WHERE org_id = %s", (org_id,))
        result = cursor.fetchone()
        if result:
            name = result["name"]
        cursor.close()
        conn.close()
    return name

# Get all activity reports (admin-generated)
def get_activity_reports():
    conn = get_db_connection()
    reports = []
    if conn:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT report_id, datetime_from, datetime_to, generated_at FROM activity_reports ORDER BY generated_at DESC")
        reports = cursor.fetchall()
        cursor.close()
        conn.close()
    return reports

# Search activity reports by date range
def search_activity_reports(date_from, date_to):
    conn = get_db_connection()
    reports = []
    if conn:
        cursor = conn.cursor(dictionary=True)
        query = "SELECT report_id, datetime_from, datetime_to, generated_at FROM activity_reports WHERE 1=1"
        params = []
        if date_from:
            query += " AND generated_at >= %s"
            params.append(date_from)
        if date_to:
            query += " AND generated_at <= %s"
            params.append(date_to)
        query += " ORDER BY generated_at DESC"
        cursor.execute(query, tuple(params))
        reports = cursor.fetchall()
        cursor.close()
        conn.close()
    return reports


# GET organization dashboard page
@app.get("/org/dashboard", response_class=HTMLResponse)
async def org_dashboard(request: Request, org_id: str = None):
    org_name = get_org_name(org_id)
    reports = get_activity_reports()
    return templates.TemplateResponse("org_dashboard.html", {"request": request, "org_id": org_id, "org_name": org_name, "reports": reports})

# POST organization dashboard search (date range only)
@app.post("/org/dashboard", response_class=HTMLResponse)
async def org_dashboard_search(request: Request, org_id: str = Form(None), date_from: str = Form(""), date_to: str = Form("")):
    org_name = get_org_name(org_id)
    reports = search_activity_reports(date_from, date_to)
    return templates.TemplateResponse("org_dashboard.html", {"request": request, "org_id": org_id, "org_name": org_name, "reports": reports})


# View activity report details
@app.get("/org/reports/{report_id}", response_class=HTMLResponse)
async def org_report_view(request: Request, report_id: int):
    conn = get_db_connection()
    report = None
    if conn:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM activity_reports WHERE report_id = %s", (report_id,))
        report = cursor.fetchone()
        cursor.close()
        conn.close()
    if report:
        return HTMLResponse(f"<h2>Activity Report {report_id}</h2><pre>{report}</pre>")
    else:
        return HTMLResponse(f"<h2>Report {report_id} not found.</h2>")

# Download activity report (stub)
@app.get("/org/reports/{report_id}/download")
async def org_report_download(report_id: int):
    return HTMLResponse(f"Download for activity report {report_id} (to implement)")

# Email activity report (stub)
@app.get("/org/reports/{report_id}/email")
async def org_report_email(report_id: int):
    return HTMLResponse(f"Email for activity report {report_id} (to implement)")

if __name__ == "__main__":
    uvicorn.run("app:app", host="127.0.0.1", port=8000, reload=True)
