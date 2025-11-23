from passlib.context import CryptContext

# Use Argon2 as the preferred hashing scheme to avoid bcrypt's 72-byte
# input limit. Keep bcrypt_sha256 and bcrypt in the schemes list so
# existing bcrypt hashes continue to verify.
#
# Requirements: argon2-cffi (already present in requirements.txt) or
# passlib will fall back to other schemes if not available.
pwd_context = CryptContext(
    schemes=["argon2", "bcrypt_sha256", "bcrypt"],
    deprecated="auto",
)

def hash_password(password: str) -> str:
    """Hash a plaintext password. New hashes will use Argon2.
    Argon2 supports arbitrarily long inputs so this avoids the
    `bcrypt` 72-byte truncation error. Verification will still succeed
    for existing bcrypt hashes because they remain supported by the
    CryptContext.
    """
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)