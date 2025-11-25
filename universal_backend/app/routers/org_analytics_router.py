from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
from sqlalchemy import func

from app.db.session import get_db
from app.models import sqlalchemy_models as models

router = APIRouter(prefix="/api/org", tags=["Org Analytics"])


@router.get("/analytics")
def get_org_analytics(
    days: int = 30,
    db: Session = Depends(get_db)
):
    since_date = datetime.utcnow() - timedelta(days=days)

    # ----------------------------------------
    # Fetch all reports used for multiple sections
    # ----------------------------------------
    reports = (
        db.query(models.Report)
        .filter(models.Report.date_reported >= since_date)
        .all()
    )

    # ----------------------------------------
    # 1. HEATMAP
    # ----------------------------------------
    heatmap_raw = (
        db.query(
            models.Location.area.label("area"),
            models.Location.latitude.label("lat"),
            models.Location.longitude.label("lng"),
            func.count(models.Report.report_id).label("count")
        )
        .join(models.Report, models.Report.location_id == models.Location.location_id)
        .filter(
            models.Report.date_reported >= since_date,
            models.Location.latitude.isnot(None),
            models.Location.longitude.isnot(None)
        )
        .group_by(
            models.Location.area,
            models.Location.latitude,
            models.Location.longitude
        )
        .having(func.count(models.Report.report_id) > 0)
        .all()
    )

    heatmap = []
    for h in heatmap_raw:
        try:
            lat = float(h.lat)
            lng = float(h.lng)
        except Exception:
            continue

        heatmap.append({
            "area": h.area,
            "lat": lat,
            "lng": lng,
            "count": int(h.count)
        })

    # ----------------------------------------
    # 2. CRIME COUNTS
    # ----------------------------------------
    crime_counts = [
        {"crime_type": c.crime_type, "count": int(c.count)}
        for c in (
            db.query(
                models.CrimeType.name.label("crime_type"),
                func.count(models.Report.report_id).label("count")
            )
            .join(models.Report, models.Report.crime_type_id == models.CrimeType.crime_type_id)
            .filter(models.Report.date_reported >= since_date)
            .group_by(models.CrimeType.name)
            .all()
        )
    ]

    # ----------------------------------------
    # 3. REPORTS BY LOCATION
    # ----------------------------------------
    location_counts = {}
    for r in reports:
        if r.location:
            loc = r.location.area
            location_counts[loc] = location_counts.get(loc, 0) + 1

    location_counts_list = [
        {"location": k, "count": v}
        for k, v in location_counts.items()
        if v > 0
    ]

    # ----------------------------------------
    # 4. TREND
    # ----------------------------------------
    trend_raw = (
        db.query(
            func.date(models.Report.date_reported).label("date"),
            func.count(models.Report.report_id).label("count")
        )
        .filter(models.Report.date_reported >= since_date)
        .group_by(func.date(models.Report.date_reported))
        .order_by(func.date(models.Report.date_reported))
        .all()
    )

    trend = []
    for t in trend_raw:
        d = t.date
        date_str = d if isinstance(d, str) else d.isoformat()
        trend.append({"date": date_str, "count": int(t.count)})

    return {
        "heatmap": heatmap,
        "crime_counts": crime_counts,
        "trend": trend,
        "location_counts": location_counts_list
    }
