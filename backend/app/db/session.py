from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base

# Update with your actual PostgreSQL credentials and database name
DATABASE_URL = "postgresql+psycopg://postgres:12345678@localhost/harbitdb"

# Create the SQLAlchemy engine
engine = create_engine(DATABASE_URL, echo=True, future=True)

# Create a configured "Session" class
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine, future=True)

# Base class for your ORM models
Base = declarative_base()