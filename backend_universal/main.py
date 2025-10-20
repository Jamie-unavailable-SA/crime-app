# backend_universal/main.py

from fastapi import FastAPI
from . import models
from .database import engine

models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Crime Reporting API")

@app.get("/")
def root():
    return {"message": "Crime App Backend is running"}
