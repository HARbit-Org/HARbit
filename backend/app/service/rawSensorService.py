from model.dto import rawSensorDto

class RawSensorService:
    def __init__(self):
        pass # TODO: Add repository or other dependencies if needed

    def process_raw_data(self, data: rawSensorDto):
        print(data)