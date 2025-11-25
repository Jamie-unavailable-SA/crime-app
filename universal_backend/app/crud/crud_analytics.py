from sqlalchemy.orm import Session
from sqlalchemy import func, desc
from datetime import datetime, timedelta

from ..models.sqlalchemy_models import Report, CrimeType


def parse_range(range: str):
    n = int(range.split()[0])
    return datetime.utcnow() - timedelta(days=n)


# -------------------------------------------------
# 1) SUMMARY — reports count grouped by crime type
# -------------------------------------------------
def get_summary(db: Session, location_id: int, crime_type_id: int, range: str):
    since = parse_range(range)

    query = (
        db.query(
            CrimeType.name.label("crime_type"),
            func.count(Report.report_id).label("count")
        )
        .join(CrimeType, CrimeType.crime_type_id == Report.crime_type_id)
        .filter(Report.location_id == location_id)
        .filter(Report.created_at >= since)
    )

    if crime_type_id:
        query = query.filter(Report.crime_type_id == crime_type_id)

    results = query.group_by(CrimeType.name).all()

    return [
        {"crime_type": r.crime_type, "count": r.count}
        for r in results
    ]


# -------------------------------------------------
# 2) RISK LEVELS — simple rule-based scoring
# -------------------------------------------------
def get_risk_levels(db: Session, location_id: int):
    # Count number of reports for each crime type
    results = (
        db.query(
            CrimeType.name.label("crime_type"),
            func.count(Report.report_id).label("count")
        )
        .join(CrimeType, CrimeType.crime_type_id == Report.crime_type_id)
        .filter(Report.location_id == location_id)
        .group_by(CrimeType.name)
        .all()
    )

    risk_data = []
    for r in results:
        if r.count >= 15:
            level = "High"
        elif r.count >= 5:
            level = "Medium"
        else:
            level = "Low"

        risk_data.append(
            {
                "crime_type": r.crime_type,
                "level": level
            }
        )

    return risk_data


# -------------------------------------------------
# 3) RECENT REPORTS
# -------------------------------------------------
def get_recent_reports(db: Session, location_id: int, limit: int = 10):
    results = (
        db.query(Report)
        .filter(Report.location_id == location_id)
        .order_by(desc(Report.created_at))
        .limit(limit)
        .all()
    )

    output = []
    for r in results:
        output.append(
            {
                "crime_type": r.crime_type.name,
                "description": r.description,
                "time_ago": r.created_at.strftime("%Y-%m-%d %H:%M")
            }
        )

    return output
