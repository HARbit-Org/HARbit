from fastapi import FastAPI
from api.v1.rawSensorController import router as rawSensorRouter
from api.v1.authController import router as authRouter
from api.v1.userController import router as userRouter
from api.v1.activityController import router as activityRouter

# Import all models to register them with SQLAlchemy
from model.entity import users, rawSensorRecords, userProviders, processedActivities, notifications, progressInsights, dailySteps, heartRateReadings

app = FastAPI(title="HARbit API")
app.include_router(rawSensorRouter)
app.include_router(authRouter)
app.include_router(userRouter)
app.include_router(activityRouter)