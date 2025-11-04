from sqlalchemy import Column, Integer, String, DateTime, func
from sqlalchemy.orm import declarative_base


Base = declarative_base()




class Admin(Base):
    __tablename__ = "admins"


    admin_id = Column(Integer, primary_key=True, autoincrement=True)
    password = Column(String(255), nullable=False)
    date_created = Column(DateTime, server_default=func.now())
    last_login = Column(DateTime, nullable=True)




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




class Session(Base):
    __tablename__ = "sessions"


    session_id = Column(Integer, primary_key=True, autoincrement=True)
    user_type = Column(String(50), nullable=False)
    user_id = Column(Integer, nullable=False)
    token = Column(String(255), nullable=False, unique=True)
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, server_default=func.now())
