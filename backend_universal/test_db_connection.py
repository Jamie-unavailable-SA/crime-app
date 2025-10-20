from sqlalchemy import create_engine, text

# Adjust if your MySQL has a password
DATABASE_URL = "mysql+mysqlconnector://root@localhost:3306/crime_app"

try:
    engine = create_engine(DATABASE_URL)
    with engine.connect() as connection:
        result = connection.execute(text("SELECT DATABASE();"))
        print("✅ Connected to database:", result.scalar())
except Exception as e:
    print("❌ Connection failed:", e)
