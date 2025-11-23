import os, sys
from sqlalchemy.orm import sessionmaker
from app.db.session import engine
from app.models.sqlalchemy_models import CrimeType, Base

# Ensure current working dir in path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
print("🔧 Added to sys.path:", os.getcwd())

# Create tables if not exist
Base.metadata.create_all(bind=engine)
SessionLocal = sessionmaker(bind=engine)
session = SessionLocal()

crime_types = [
    {"name": "Theft", "description": "Stealing property or belongings."},
    {"name": "Assault", "description": "Physical attack on a person."},
    {"name": "Robbery", "description": "Theft using violence or threat."},
    {"name": "Burglary", "description": "Breaking into a property to steal."},
    {"name": "Fraud", "description": "Deception for personal or financial gain."},
    {"name": "Homicide", "description": "Unlawful killing of another person."},
    {"name": "Vandalism", "description": "Intentional damage to property."},
]

for crime in crime_types:
    exists = session.query(CrimeType).filter(CrimeType.name == crime["name"]).first()
    if not exists:
        session.add(CrimeType(**crime))

session.commit()
session.close()

print("✅ Crime types seeded successfully!")