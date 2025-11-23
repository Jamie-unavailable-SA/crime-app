# src/app/crud/crud_reports.py
from sqlalchemy.orm import Session
from datetime import datetime
from app.models import sqlalchemy_models as models

def create_report(
    db: Session,
    reporter_id: int,
    crime_type_id: int,
    location_id: int,
    description: str,
    occurrence_time: datetime,
):
    rpt = models.Report(  # ✅ changed from models.CrimeReport
        reporter_id=reporter_id,
        crime_type_id=crime_type_id,
        location_id=location_id,
        description=description,
        occurrence_time=occurrence_time,
    )
    db.add(rpt)
    db.commit()
    db.refresh(rpt)
    return rpt

def add_report_addon(
    db: Session,
    report_id: int,
    file_path: str,
    file_type: str,
    file_size: int = None,
):
    addon = models.ReportAddon(
        report_id=report_id,
        file_path=file_path,
        file_type=file_type,
        file_size=file_size,
    )
    db.add(addon)
    db.commit()
    db.refresh(addon)
    return addon

def get_all_reports(db: Session):
    """Fetch all crime reports with reporter, crime type, and date info."""
    return (
        db.query(models.Report)
        .join(models.Reporter)
        .join(models.CrimeType)
        .all()
    )