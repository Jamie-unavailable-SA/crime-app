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

# GET login page
@app.get("/", response_class=HTMLResponse)
async def login_page(request: Request):
    return templates.TemplateResponse("login.html", {"request": request, "error": None})


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
            return templates.TemplateResponse("login.html", {"request": request, "error": "Invalid credentials"})

    return templates.TemplateResponse("login.html", {"request": request, "error": "Database connection failed"})

# GET dashboard page
@app.get("/dashboard", response_class=HTMLResponse)
async def dashboard(request: Request, admin_id: str = None):
    return templates.TemplateResponse("dashboard.html", {"request": request, "admin_id": admin_id})




# Logout route
@app.get("/logout")
async def logout():
    response = RedirectResponse(url="/", status_code=303)
    return response

if __name__ == "__main__":
    uvicorn.run("app:app", host="127.0.0.1", port=8000, reload=True)
