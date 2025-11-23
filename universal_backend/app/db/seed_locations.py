import os
import sys
from app.db.session import engine
from sqlalchemy.orm import sessionmaker
from app.models.sqlalchemy_models import Location, Base

# --- Allow running directly ---
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_DIR = os.path.abspath(os.path.join(ROOT_DIR, "../../"))
if SRC_DIR not in sys.path:
    sys.path.insert(0, SRC_DIR)
    print(f"🔧 Added to sys.path: {SRC_DIR}")

# --- Initialize DB ---
Base.metadata.create_all(bind=engine)
SessionLocal = sessionmaker(bind=engine)
session = SessionLocal()

# --- Nairobi locations with sub-areas ---
locations = [
    {"area": "CBD", "sub_area": "Kenyatta Avenue", "latitude": "-1.286389", "longitude": "36.817223", "description": "Central business district"},
    {"area": "Westlands", "sub_area": "Parklands", "latitude": "-1.2646", "longitude": "36.8148", "description": "Commercial and residential area"},
    {"area": "Kilimani", "sub_area": "Yaya Centre", "latitude": "-1.2922", "longitude": "36.7839", "description": "Upscale residential and mixed-use area"},
    {"area": "Kibera", "sub_area": "Olympic", "latitude": "-1.3119", "longitude": "36.7845", "description": "High-density residential area"},
    {"area": "Langata", "sub_area": "Wilson Airport", "latitude": "-1.3227", "longitude": "36.8145", "description": "Near Wilson Airport and Karen Road"},
    {"area": "Karen", "sub_area": "Hardy", "latitude": "-1.3171", "longitude": "36.7157", "description": "Leafy suburb near Nairobi National Park"},
    {"area": "Embakasi", "sub_area": "Pipeline", "latitude": "-1.3297", "longitude": "36.9062", "description": "Densely populated eastern suburb"},
    {"area": "Donholm", "sub_area": "Phase 8", "latitude": "-1.3079", "longitude": "36.8881", "description": "Residential area with malls and estates"},
    {"area": "Eastleigh", "sub_area": "Section 3", "latitude": "-1.2835", "longitude": "36.8552", "description": "Busy commercial district"},
    {"area": "Runda", "sub_area": "Runda Grove", "latitude": "-1.2192", "longitude": "36.8273", "description": "High-end residential area"},
    {"area": "Gigiri", "sub_area": "UN Avenue", "latitude": "-1.2334", "longitude": "36.8172", "description": "UN offices and diplomatic zone"},
    {"area": "Kasarani", "sub_area": "Mwiki", "latitude": "-1.2196", "longitude": "36.8968", "description": "Residential and sports complex area"},
    {"area": "Roy Sambu", "sub_area": "Thika Road Mall", "latitude": "-1.2248", "longitude": "36.8819", "description": "Commercial zone near TRM"},
    {"area": "Ngong Road", "sub_area": "Adams Arcade", "latitude": "-1.3009", "longitude": "36.7819", "description": "Shopping and business hub"},
    {"area": "South B", "sub_area": "Hazina", "latitude": "-1.3205", "longitude": "36.8455", "description": "Middle-class residential estate"},
    {"area": "South C", "sub_area": "Bellevue", "latitude": "-1.3249", "longitude": "36.8343", "description": "Residential area near Mombasa Road"},
    {"area": "Kawangware", "sub_area": "Stage 56", "latitude": "-1.2867", "longitude": "36.7455", "description": "Populated low-income area"},
    {"area": "Dagoretti", "sub_area": "Mutuini", "latitude": "-1.2921", "longitude": "36.7388", "description": "Residential region near Ngong Forest"},
    {"area": "Umoja", "sub_area": "Innercore", "latitude": "-1.2803", "longitude": "36.8945", "description": "Densely populated residential area"},
    {"area": "Buruburu", "sub_area": "Phase 3", "latitude": "-1.2928", "longitude": "36.8753", "description": "Residential estate east of Nairobi"},
]

# --- Seed database ---
inserted = 0
for loc in locations:
    exists = session.query(Location).filter(
        Location.area == loc["area"],
        Location.sub_area == loc["sub_area"]
    ).first()
    if not exists:
        session.add(Location(**loc))
        inserted += 1

session.commit()
print(f"✅ Seed complete: {inserted} new locations added.")
session.close()