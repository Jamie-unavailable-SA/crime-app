from fastapi import APIRouter, Depends, HTTPException, Body
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.models import sqlalchemy_models as models
from app.crud import crud_auth

router = APIRouter(prefix="/api/admin", tags=["Admin Management"])


# -------------------------
# LIST ORGANISATIONS
# -------------------------
@router.get("/organizations")
def list_orgs(db: Session = Depends(get_db)):
    orgs = db.query(models.ExternalOrg).all()
    return [
        {
            "id": o.org_id,
            "name": o.org_name,
            "email": o.contact_email,
            "phone": o.contact_phone,
        }
        for o in orgs
    ]


# DELETE ORG
@router.delete("/organizations/{org_id}")
def delete_org(org_id: int, db: Session = Depends(get_db)):
    # use the correct model name (ExternalOrg)
    org = db.query(models.ExternalOrg).filter(models.ExternalOrg.org_id == org_id).first()
    if not org:
        raise HTTPException(status_code=404, detail="Organization not found")

    db.delete(org)
    db.commit()
    return {"status": "ok", "message": "Organization deleted"}


# -------------------------
# LIST REPORTERS
# -------------------------
@router.get("/reporters")
def list_reporters(db: Session = Depends(get_db)):
    reps = db.query(models.Reporter).all()
    return [
        {
            "id": r.reporter_id,
            "alias": r.alias,
            "email": r.email,
            "phone": r.phone,
            "reports": len(r.reports) if r.reports is not None else 0,
        }
        for r in reps
    ]


# DELETE REPORTER
@router.delete("/reporters/{reporter_id}")
def delete_reporter(reporter_id: int, db: Session = Depends(get_db)):
    rep = db.query(models.Reporter).filter(models.Reporter.reporter_id == reporter_id).first()
    if not rep:
        raise HTTPException(status_code=404, detail="Reporter not found")

    db.delete(rep)
    db.commit()
    return {"status": "ok", "message": "Reporter deleted"}


# -------------------------
# LIST ADMINS
# -------------------------
@router.get("/admins/list")
def list_admins(db: Session = Depends(get_db)):
    admins = db.query(models.Admin).all()
    return [
        {
            "admin_id": a.admin_id,
            "date_created": a.date_created,
            "last_login": a.last_login,
        }
        for a in admins
    ]


# ADD ADMIN
@router.post("/admins/add")
def add_admin(password: str = Body(..., embed=True), db: Session = Depends(get_db)):
    """
    Create an admin with only a password.
    Request body: { "password": "thepassword" }
    """
    if not password or len(password) < 6:
        raise HTTPException(status_code=400, detail="Password required (min 6 chars)")

    new_admin = crud_auth.create_admin(db, password)
    return {"status": "ok", "id": new_admin.admin_id}


# DELETE ADMIN
@router.delete("/admins/{admin_id}")
def delete_admin(admin_id: int, db: Session = Depends(get_db)):
    admin = db.query(models.Admin).filter(models.Admin.admin_id == admin_id).first()
    if not admin:
        raise HTTPException(status_code=404, detail="Admin not found")

    db.delete(admin)
    db.commit()
    return {"status": "ok", "message": "Admin deleted"}
