"""
Quick DB connection tester for this project.
- Reads DATABASE_URL from .env in the repo root (universal_backend/.env) or from environment.
- Attempts to connect with SQLAlchemy and prints a redacted URL and server version if successful.

Usage (PowerShell):
> cd d:\Project\crime-app\universal_backend
> python .\scripts\check_db.py

If you want to test a specific URL without .env, run:
> python .\scripts\check_db.py "mysql+mysqlconnector://user:pass@127.0.0.1:3306/crimewatch"

This script will not print passwords; it redacts them.
"""
import os
import sys
import re
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

load_dotenv()

def redact_url(url: str) -> str:
    # remove password from the URL for safe printing
    if not url:
        return "(none)"
    # simple regex to redact between : and @
    return re.sub(r":(.*)@", ":*****@", url)


def test_url(url: str):
    print("Testing DATABASE_URL:", redact_url(url))
    try:
        engine = create_engine(url)
        with engine.connect() as conn:
            version = conn.execute(text('select version()')).scalar()
            print("Connected successfully. Server version:", version)
    except Exception as e:
        print("Connection failed:")
        import traceback
        traceback.print_exc()


if __name__ == '__main__':
    if len(sys.argv) > 1:
        url = sys.argv[1]
    else:
        url = os.environ.get('DATABASE_URL')
    if not url:
        print("DATABASE_URL not found in environment. Please create a .env file with DATABASE_URL or pass it as an argument.")
        print("Example .env content:")
        print('DATABASE_URL="mysql+mysqlconnector://crime_user:crime_pass@127.0.0.1:3306/crimewatch?charset=utf8mb4"')
        sys.exit(1)
    test_url(url)
