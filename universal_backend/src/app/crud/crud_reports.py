# src/app/crud/crud_reports.py
from sqlalchemy.orm import Session
from datetime import datetime
from app.models import sqlalchemy_models as models


def create_report(
    db: Session,
    reporter_id: int,
    crime_type_id: int = None,
    description: str = None,
    location_id: int = None,
    occurrence_time: datetime = None,
    image_1: str = None,
    image_2: str = None,
):
    """Create a new crime report entry."""
    rpt = models.CrimeReport(
        reporter_id=reporter_id,
        crime_type_id=crime_type_id,
        location_id=location_id,
        description=description,
        occurrence_time=occurrence_time or datetime.utcnow(),
        image_1=image_1,
        image_2=image_2,
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
    file_size: int = None
):
    """Attach an uploaded file (image/video) to an existing report."""
    addon = models.ReportAddon(
        report_id=report_id,
        file_path=file_path,
        file_type=file_type,
        file_size=file_size
    )

    db.add(addon)
    db.commit()
    db.refresh(addon)
    return addon
