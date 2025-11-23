from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, func
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()

# ----------------------------
# Admins Table
# ----------------------------
class Admin(Base):
    __tablename__ = "admins"
    admin_id = Column(Integer, primary_key=True, autoincrement=True)
    password = Column(String(255), nullable=False)
    date_created = Column(DateTime, server_default=func.now())
    last_login = Column(DateTime, nullable=True)

# ----------------------------
# External Organizations Table
# ----------------------------
class ExternalOrg(Base):
    __tablename__ = "external_orgs"
    org_id = Column(Integer, primary_key=True, autoincrement=True)
    org_name = Column(String(200), nullable=False)
    contact_person = Column(String(200), nullable=True)
    contact_email = Column(String(200), unique=True, nullable=False)
    contact_phone = Column(String(50), nullable=True)
    password = Column(String(255), nullable=False)
    date_added = Column(DateTime, server_default=func.now())
    last_login = Column(DateTime, nullable=True)

# ----------------------------
# Reporter Table
# ----------------------------
class Reporter(Base):
    __tablename__ = "reporters"
    reporter_id = Column(Integer, primary_key=True, autoincrement=True)
    alias = Column(String(100), nullable=False)
    f_name = Column(String(100), nullable=True)
    l_name = Column(String(100), nullable=True)
    email = Column(String(200), unique=True, nullable=True)
    phone = Column(String(50), nullable=True)
    password = Column(String(255), nullable=False)
    date_joined = Column(DateTime, server_default=func.now())
    last_login = Column(DateTime, nullable=True)
    # ✅ Relationship to reports
    reports = relationship("Report", back_populates="reporter")

# ----------------------------
# Session Table
# ----------------------------
class Session(Base):
    __tablename__ = "sessions"
    session_id = Column(Integer, primary_key=True, autoincrement=True)
    user_type = Column(String(50), nullable=False)
    user_id = Column(Integer, nullable=False)
    token = Column(String(255), nullable=False, unique=True)
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, server_default=func.now())

# ----------------------------
# Crime Types Table
# ----------------------------
class CrimeType(Base):
    __tablename__ = "crime_types"
    crime_type_id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), nullable=False, unique=True)
    description = Column(String(255), nullable=True)
    date_added = Column(DateTime, server_default=func.now())
    # ✅ Relationship to reports
    reports = relationship("Report", back_populates="crime_type")

# ----------------------------
# Locations Table
# ----------------------------
class Location(Base):
    __tablename__ = "locations"
    location_id = Column(Integer, primary_key=True, autoincrement=True)
    area = Column(String(200), nullable=False)
    sub_area = Column(String(200), nullable=True)
    latitude = Column(String(50), nullable=False)
    longitude = Column(String(50), nullable=False)
    description = Column(String(255), nullable=True)
    # ✅ Relationship to reports
    reports = relationship("Report", back_populates="location")

# ----------------------------
# Reports Table
# ----------------------------
class Report(Base):
    __tablename__ = "reports"
    report_id = Column(Integer, primary_key=True, autoincrement=True)
    reporter_id = Column(Integer, ForeignKey("reporters.reporter_id"), nullable=False)
    crime_type_id = Column(Integer, ForeignKey("crime_types.crime_type_id"), nullable=False)
    location_id = Column(Integer, ForeignKey("locations.location_id"), nullable=False)
    description = Column(String(500), nullable=False)
    occurrence_time = Column(DateTime, nullable=False)
    date_reported = Column(DateTime, server_default=func.now())
    # ✅ Relationships
    addons = relationship("ReportAddon", back_populates="report", cascade="all, delete-orphan")
    reporter = relationship("Reporter", back_populates="reports")
    crime_type = relationship("CrimeType", back_populates="reports")
    location = relationship("Location", back_populates="reports")

# ----------------------------
# Report Addons Table
# ----------------------------
class ReportAddon(Base):
    __tablename__ = "report_addons"
    addon_id = Column(Integer, primary_key=True, autoincrement=True)
    report_id = Column(Integer, ForeignKey("reports.report_id", ondelete="CASCADE"), nullable=False)
    file_path = Column(String(255), nullable=False)
    file_type = Column(String(50), nullable=False)
    file_size = Column(Integer, nullable=True)
    date_uploaded = Column(DateTime, server_default=func.now())
    # ✅ Relationship back to Report
    report = relationship("Report", back_populates="addons")