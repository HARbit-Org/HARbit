from fastapi import FastAPI
from api.v1.rawSensorController import router as rawSensorRouter

# Import all models to register them with SQLAlchemy
from model.entity import users, rawSensorRecords, userProviders, processedActivities, notifications, progressInsights, dailySteps, heartRateReadings

app = FastAPI(title="HARbit API")
app.include_router(rawSensorRouter)