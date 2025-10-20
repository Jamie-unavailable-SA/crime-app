from fastapi import FastAPI
import logging
import sqlalchemy
from backend_universal.database import Base, engine
from backend_universal import models

logger = logging.getLogger("backend_universal")
logging.basicConfig(level=logging.INFO)

app = FastAPI(title="Crime Reporting API")


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
def root():
    return {"message": "Backend API is running"}


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
