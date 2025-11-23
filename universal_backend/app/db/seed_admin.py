# seed_admin.py
from sqlalchemy.orm import sessionmaker
from app.db.session import engine
from app.models.sqlalchemy_models import Admin, Base
# --- New Import for Argon2 ---
from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError # Recommended for verification later

# --- Initialize Argon2 Password Hasher ---
# It's best practice to tune these parameters for your environment.
# Defaults are usually a good start for modern systems.
ph = PasswordHasher()

# --- Initialize database ---
Base.metadata.create_all(bind=engine)
SessionLocal = sessionmaker(bind=engine)
db = SessionLocal()

# --- Check if admin already exists ---
existing = db.query(Admin).first()
if existing:
    print("⚠️ Admin already exists. No action taken.")
else:
    # --- Create default admin ---
    # Use the Argon2 PasswordHasher instance to hash the password
    hashed_password = ph.hash("testadmin")
   
    # Store the Argon2 hash string directly in the password field
    admin = Admin(password=hashed_password)
    db.add(admin)
    db.commit()
    print("✅ Admin seeded successfully!")
    print("➡️  Login credentials:")
    print("    ID:", admin.admin_id)
    print("    Password: Testadmin")

db.close()
