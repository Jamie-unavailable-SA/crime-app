# backend-universal/models.py

from sqlalchemy import Column, Integer, String, Text, Enum, ForeignKey, TIMESTAMP
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from .database import Base

class Reporter(Base):
    __tablename__ = "reporter"
    reporter_id = Column(Integer, primary_key=True, index=True)
    reporter_fname = Column(String(100), nullable=False)
    reporter_lname = Column(String(50), nullable=False)
    reporter_alias = Column(String(50), nullable=False)
    reporter_age = Column(Integer)
    reporter_location = Column(String(150))
    password = Column(String(255), nullable=False)
    pnumber = Column(String(15))
    date_created = Column(TIMESTAMP, server_default=func.current_timestamp())
    last_login = Column(TIMESTAMP, nullable=True)

class CrimeType(Base):
    __tablename__ = "crime_type"
    type_id = Column(Integer, primary_key=True, index=True)
    type_name = Column(String(100), unique=True, nullable=False)

class Location(Base):
    __tablename__ = "location"
    location_id = Column(Integer, primary_key=True, index=True)
    location_name = Column(String(100), nullable=False)
    county = Column(String(50))
    subcounty = Column(String(50))
    ward = Column(String(50))

class Administrator(Base):
    __tablename__ = "administrator"
    admin_id = Column(Integer, primary_key=True, index=True)
    password = Column(String(255), nullable=False)
    last_login = Column(TIMESTAMP, nullable=True)

class CrimeReport(Base):
    __tablename__ = "crime_reports"
    report_id = Column(Integer, primary_key=True, index=True)
    reporter_id = Column(Integer, ForeignKey("reporter.reporter_id", ondelete="SET NULL"))
    crime_type = Column(Integer, ForeignKey("crime_type.type_id", ondelete="CASCADE"))
    location = Column(Integer, ForeignKey("location.location_id", ondelete="CASCADE"))
    incident_datetime = Column(TIMESTAMP, nullable=False)
    report_datetime = Column(TIMESTAMP, server_default=func.current_timestamp())
    report_type = Column(Enum("sighting", "victimization"))
    description = Column(Text)
    status = Column(Enum("pending", "approved", "rejected"), default="pending")
    verified_at = Column(TIMESTAMP, nullable=True)
    verified_by = Column(Integer, ForeignKey("administrator.admin_id", ondelete="SET NULL"))

class ReportAddon(Base):
    __tablename__ = "report_addons"
    media_id = Column(Integer, primary_key=True, index=True)
    report_id = Column(Integer, ForeignKey("crime_reports.report_id", ondelete="CASCADE"))
    media_type = Column(Enum("image", "video"))
    file_path = Column(String(255), nullable=False)
    file_size = Column(Integer)
    uploaded_at = Column(TIMESTAMP, server_default=func.current_timestamp())

class CustomerSupport(Base):
    __tablename__ = "customer_support"
    support_id = Column(Integer, primary_key=True, index=True)
    requester_id = Column(Integer, ForeignKey("reporter.reporter_id", ondelete="SET NULL"))
    attendant_id = Column(Integer, ForeignKey("administrator.admin_id", ondelete="SET NULL"))
    description = Column(Text)
    status = Column(Enum("open", "in progress", "resolved", "closed"), default="open")

class ActivityReport(Base):
    __tablename__ = "activity_reports"
    report_id = Column(Integer, primary_key=True, index=True)
    created_by = Column(Integer, ForeignKey("administrator.admin_id", ondelete="CASCADE"))
    datetime_from = Column(TIMESTAMP, nullable=False)
    datetime_to = Column(TIMESTAMP)
    generated_at = Column(TIMESTAMP, server_default=func.current_timestamp())
