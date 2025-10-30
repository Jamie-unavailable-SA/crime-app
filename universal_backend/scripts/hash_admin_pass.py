from src.app.core.security import hash_password

# Generate hash for admin password
admin_pass = "adminpass1"  # or whatever password you want to use
hashed = hash_password(admin_pass)
print(f"Hashed password: {hashed}")