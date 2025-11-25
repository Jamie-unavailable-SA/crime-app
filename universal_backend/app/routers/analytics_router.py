from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
from typing import Optional

from app.db.session import get_db
from app.models import sqlalchemy_models as models
from app.schema import analytics as schemas

router = APIRouter(prefix="/analytics", tags=["Analytics"])


# ------------------------------------------------------------------------------
# SUMMARY COUNTS
# ------------------------------------------------------------------------------
@router.get("/summary", response_model=list[schemas.ReportCountSummary])
def analytics_summary(
    location_id: int = Query(...),
    crime_type_id: Optional[int] = Query(None),
    range: str = Query("30 days"),
    db: Session = Depends(get_db),
):
    days_map = {"7 days": 7, "30 days": 30, "90 days": 90}
    days = days_map.get(range, 30)
    since_date = datetime.utcnow() - timedelta(days=days)

    crime_types = db.query(models.CrimeType).all()
    summaries = []

    for c in crime_types:
        q = db.query(models.Report).filter(
            models.Report.location_id == location_id,
            models.Report.crime_type_id == c.crime_type_id,
            models.Report.date_reported >= since_date
        )

        if crime_type_id and crime_type_id != c.crime_type_id:
            continue

        count = q.count()

        if count > 0:
            summaries.append({"crime_type": c.name, "count": count})

    return summaries


# ------------------------------------------------------------------------------
# RISK LEVELS
# ------------------------------------------------------------------------------
@router.get("/risk-levels", response_model=list[schemas.RiskLevel])
def risk_levels(
    location_id: int = Query(...),
    db: Session = Depends(get_db),
):
    crime_types = db.query(models.CrimeType).all()
    results = []

    for c in crime_types:
        count = db.query(models.Report).filter(
            models.Report.location_id == location_id,
            models.Report.crime_type_id == c.crime_type_id
        ).count()

        level = "Low"
        if count >= 20:
            level = "High"
        elif count >= 5:
            level = "Medium"

        results.append({
            "crime_type": c.name,
            "level": level,
        })

    return results


# ------------------------------------------------------------------------------
# RECENT REPORTS
# ------------------------------------------------------------------------------
@router.get("/recent", response_model=list[schemas.RecentReport])
def recent_reports(
    location_id: int = Query(...),
    limit: int = Query(10),
    db: Session = Depends(get_db),
):
    reports = (
        db.query(models.Report)
        .filter(models.Report.location_id == location_id)
        .order_by(models.Report.date_reported.desc())
        .limit(limit)
        .all()
    )

    result = []
    for r in reports:
        delta = datetime.utcnow() - r.date_reported
        hours = delta.total_seconds() // 3600
        time_ago = f"{int(hours)}h ago" if hours < 24 else f"{int(hours // 24)}d ago"

        result.append({
            "crime_type": r.crime_type.name,
            "description": r.description,
            "time_ago": time_ago,
        })

    return result


# ------------------------------------------------------------------------------
# FULL BUNDLE ENDPOINT
# ------------------------------------------------------------------------------
@router.get("/", summary="Returns risk levels, counts and recent reports")
def full_analytics(
    location_id: int,
    crime_type_id: Optional[int] = None,
    range: str = "30 days",
    db: Session = Depends(get_db),
):
    # Reuse the logic from endpoints above
    return {
        "risk_levels": risk_levels(location_id, db),
        "report_counts": analytics_summary(location_id, crime_type_id, range, db),
        "recent_reports": recent_reports(location_id, 10, db),
    }
