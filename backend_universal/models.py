from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Float, Enum, Boolean
from sqlalchemy.orm import relationship
from datetime import datetime
from backend_universal.database import Base
import enum


# ---------- ENUMS ----------
class CrimeType(enum.Enum):
    THEFT = "Theft"
    ASSAULT = "Assault"
    HOMICIDE = "Homicide"
    FRAUD = "Fraud"
    OTHER = "Other"


# ---------- USERS ----------
class Admin(Base):
    __tablename__ = "admins"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)


class Reporter(Base):
    __tablename__ = "reporters"

    id = Column(Integer, primary_key=True, index=True)
    alias = Column(String(100), unique=True, nullable=False)  # public-facing name
    full_name = Column(String(255), nullable=True)
    phone_number = Column(String(20), nullable=True)
    email = Column(String(255), nullable=True)
    location = Column(String(255), nullable=True)
    registered_at = Column(DateTime, default=datetime.utcnow)

    reports = relationship("Report", back_populates="reporter")


class ExternalOrg(Base):
    __tablename__ = "external_orgs"

    id = Column(Integer, primary_key=True, index=True)
    org_name = Column(String(255), unique=True, nullable=False)
    contact_person = Column(String(255), nullable=True)
    email = Column(String(255), nullable=True)
    phone_number = Column(String(50), nullable=True)
    api_key = Column(String(255), unique=True, nullable=False)
    verified = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    data_requests = relationship("DataRequest", back_populates="organization")


# ---------- REPORTS ----------
class Report(Base):
    __tablename__ = "reports"

    id = Column(Integer, primary_key=True, index=True)
    reporter_id = Column(Integer, ForeignKey("reporters.id"))
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=False)
    crime_type = Column(Enum(CrimeType), nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    location_name = Column(String(255), nullable=True)
    date_reported = Column(DateTime, default=datetime.utcnow)
    status = Column(String(50), default="Pending")  # Pending, Verified, Resolved

    reporter = relationship("Reporter", back_populates="reports")


# ---------- DATA REQUESTS ----------
class DataRequest(Base):
    __tablename__ = "data_requests"

    id = Column(Integer, primary_key=True, index=True)
    org_id = Column(Integer, ForeignKey("external_orgs.id"))
    requested_at = Column(DateTime, default=datetime.utcnow)
    parameters = Column(Text, nullable=True)  # filters, date range, etc.
    approved = Column(Boolean, default=False)

    organization = relationship("ExternalOrg", back_populates="data_requests")


# ---------- HEATMAP CONFIGURATIONS ----------
class HeatmapConfig(Base):
    __tablename__ = "heatmap_configs"

    id = Column(Integer, primary_key=True, index=True)
    reporter_id = Column(Integer, ForeignKey("reporters.id"), nullable=True)
    name = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    filters = Column(Text, nullable=True)  # JSON string with parameters
    created_at = Column(DateTime, default=datetime.utcnow)
