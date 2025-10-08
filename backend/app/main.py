from fastapi import FastAPI
from api.v1.rawSensorController import router as rawSensorRouter

app = FastAPI(title="HARbit API")
app.include_router(rawSensorRouter)