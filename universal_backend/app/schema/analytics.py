from pydantic import BaseModel


class ReportCountSummary(BaseModel):
    crime_type: str
    count: int

    class Config:
        orm_mode = True


class RiskLevel(BaseModel):
    crime_type: str
    level: str

    class Config:
        orm_mode = True


class RecentReport(BaseModel):
    crime_type: str
    description: str
    time_ago: str

    class Config:
        orm_mode = True
