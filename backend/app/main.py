from fastapi import FastAPI
from fastapi_crons import Crons
from api.v1.rawSensorController import router as rawSensorRouter
from api.v1.authController import router as authRouter
from api.v1.userController import router as userRouter
from api.v1.activityController import router as activityRouter
from api.v1.jobController import router as jobRouter
from api.v1.progressController import router as progressRouter
from api.jobs import weekly_progress_cron

app = FastAPI(title="HARbit API")
app.include_router(rawSensorRouter)
app.include_router(authRouter)
app.include_router(userRouter)
app.include_router(activityRouter)
app.include_router(jobRouter)
app.include_router(progressRouter)

# Initialize cron jobs
crons = Crons()

# Register weekly progress job - Every Sunday at 8:00 PM (20:00 UTC)
@crons.cron("0 20 * * 0")
async def run_weekly_progress():
    """Execute weekly progress calculation for all users."""
    await weekly_progress_cron()

# Register cron jobs with FastAPI lifecycle
app.add_event_handler("startup", crons.start)
app.add_event_handler("shutdown", crons.stop)