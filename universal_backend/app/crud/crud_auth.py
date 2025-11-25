from sqlalchemy.orm import Session
from datetime import datetime, timedelta, timezone
import secrets
from ..models.sqlalchemy_models import Report, ReportAddon

from ..models.sqlalchemy_models import ExternalOrg, Admin, Reporter, Session as DBSession
from ..core.security import hash_password, verify_password

def get_org_by_email(db: Session, email: str):
    return db.query(ExternalOrg).filter(ExternalOrg.contact_email == email).first()

def get_org_by_id(db: Session, org_id: int):
    return db.query(ExternalOrg).filter(ExternalOrg.org_id == org_id).first()

def create_org(db: Session, org_name: str, contact_person: str, contact_email: str, contact_phone: str, password: str):
    hashed = hash_password(password)
    org = ExternalOrg(
        org_name=org_name,
        contact_person=contact_person,
        contact_email=contact_email,
        contact_phone=contact_phone,
        password=hashed,
    )
    org.is_approved = False
    db.add(org)
    db.commit()
    db.refresh(org)
    return org

def authenticate_org(db: Session, identifier: str, password: str):
    # identifier may be email or org id or name
    org = None
    if "@" in identifier:
        org = get_org_by_email(db, identifier)
    else:
        # try by id
        try:
            org_id = int(identifier)
            org = get_org_by_id(db, org_id)
        except Exception:
            # try by name
            org = db.query(ExternalOrg).filter(ExternalOrg.org_name == identifier).first()

    if org and verify_password(password, org.password):
        return org
    return None

def get_admin_by_id(db: Session, admin_id: int):
    return db.query(Admin).filter(Admin.admin_id == admin_id).first()

def authenticate_admin(db: Session, admin_identifier: str, password: str):
    try:
        admin_id = int(admin_identifier)
        admin = get_admin_by_id(db, admin_id)
    except Exception:
        # no other admin lookup implemented yet
        admin = None
    if admin and verify_password(password, admin.password):
        return admin
    return None

def create_reporter(db: Session, alias: str, password: str, email: str = None, phone: str = None):
    hashed = hash_password(password)
    rep = Reporter(alias=alias, password=hashed, email=email, phone=phone)
    db.add(rep)
    db.commit()
    db.refresh(rep)
    return rep

def get_reporter_by_id(db: Session, reporter_id: int):
    """Fetch a reporter record by its ID."""
    return db.query(Reporter).filter(Reporter.reporter_id == reporter_id).first()

def update_reporter(db: Session, reporter, alias=None, f_name=None, l_name=None, email=None, phone=None):
    """Update reporter details and save changes."""
    if alias is not None:
        reporter.alias = alias
    if f_name is not None:
        reporter.f_name = f_name
    if l_name is not None:
        reporter.l_name = l_name
    if email is not None:
        reporter.email = email
    if phone is not None:
        reporter.phone = phone
    db.add(reporter)   
    db.commit()
    db.refresh(reporter)
    return reporter

def authenticate_reporter(db: Session, identifier: str, password: str):
    # identifier may be alias or email
    rep = None
    if "@" in identifier:
        rep = db.query(Reporter).filter(Reporter.email == identifier).first()
    else:
        rep = db.query(Reporter).filter(Reporter.alias == identifier).first()
    if rep and verify_password(password, rep.password):
        rep.last_login = datetime.now()
        db.add(rep)
        db.commit()
        db.refresh(rep)
        return rep
    return None

# Session management helpers
def create_session(db: Session, user_type: str, user_id: int, hours_valid: int = 24):
    """Create a session token for the given user and persist it to the sessions table."""
    token = secrets.token_urlsafe(32)
    expires_at = datetime.utcnow() + timedelta(hours=hours_valid)
    sess = DBSession(user_type=user_type, user_id=user_id, token=token, expires_at=expires_at)
    db.add(sess)
    db.commit()
    db.refresh(sess)
    return sess

def get_session_by_token(db: Session, token: str):
    if not token:
        return None
    now = datetime.utcnow()
    return db.query(DBSession).filter(DBSession.token == token, DBSession.expires_at > now).first()

def delete_session(db: Session, token: str):
    if not token:
        return False
    sess = db.query(DBSession).filter(DBSession.token == token).first()
    if not sess:
        return False
    db.delete(sess)
    db.commit()
    return True

def delete_reporter(db: Session, reporter_id: int, hard_delete: bool = False) -> bool:
    """
    Delete a reporter and related data.

    - If hard_delete=True: permanently delete reports, addons, sessions and reporter.
    - If hard_delete=False: soft-delete (set a 'deleted' flag) if you have such a column; otherwise do hard delete.
    Returns True if deleted, False if reporter not found.
    """
    rep = db.query(Reporter).filter(Reporter.reporter_id == reporter_id).first()
    if not rep:
        return False

    # Delete sessions for this user (sessions table name/class DBSession used in your project)
    try:
        db.query(DBSession).filter(DBSession.user_type == "reporter", DBSession.user_id == reporter_id).delete(synchronize_session=False)
    except Exception:
        # If DBSession class/field names differ, adjust accordingly.
        pass

    # Delete report addons first (files saved on disk are not removed here — you can remove them if you'd like)
    try:
        addons = db.query(ReportAddon).join(Report).filter(Report.reporter_id == reporter_id).all()
        for a in addons:
            # Optionally remove files from disk:
            # try:
            #     if a.file_path and os.path.exists(a.file_path):
            #         os.remove(a.file_path)
            # except Exception:
            #     pass
            db.delete(a)
    except Exception:
        pass

    # Delete reports
    try:
        db.query(Report).filter(Report.reporter_id == reporter_id).delete(synchronize_session=False)
    except Exception:
        pass

    # Finally delete the reporter row
    try:
        db.delete(rep)
        db.commit()
    except Exception as e:
        db.rollback()
        raise

    return True