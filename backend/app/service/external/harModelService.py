import httpx
from model.dto.request import sensorRequestDto
from model.dto.response.harModelResponseDto import harModelResponseDto

class HarModelService:
    def __init__(self):
        # Configure the external HAR model endpoint
        self.external_endpoint = "http://192.168.18.113:5000/api/classify"
        self.timeout = 300  # seconds

    def send_data_to_har_model(self, data: sensorRequestDto):
        """Send sensor data to external endpoint and wait for response"""
        try:
            print(f"Sending data to endpoint: {self.external_endpoint}")

            # Prepare the data as JSON
            payload = data.model_dump()

            # Make the HTTP request with timeout
            with httpx.Client(timeout=self.timeout) as client:
                response = client.post(
                    self.external_endpoint,
                    json=payload,
                    headers={
                        "Content-Type": "application/json",
                        "User-Agent": "HARbit-Backend/1.0"
                    }
                )

                # Check if request was successful
                response.raise_for_status()

                print(f"✅ Successfully sent data. Status: {response.status_code}")
                print(f"Response: {response.text[:200]}...")  # First 200 chars of response

                # Parse response as DTO if JSON, else return raw text
                if response.headers.get("content-type", "").startswith("application/json"):
                    response_data = response.json()
                    dto_response = harModelResponseDto(**response_data)
                    return {
                        "success": True,
                        "status_code": response.status_code,
                        "response_data": dto_response
                    }
                else:
                    return {
                        "success": True,
                        "status_code": response.status_code,
                        "response_data": response.text
                    }
                
        except httpx.TimeoutException:
            error_msg = f"Timeout after {self.timeout} seconds when sending to {self.external_endpoint}"
            print(f"❌ {error_msg}")
            return {
                "success": False,
                "error": "timeout",
                "message": error_msg
            }
            
        except httpx.HTTPStatusError as e:
            error_msg = f"HTTP error {e.response.status_code}: {e.response.text}"
            print(f"❌ {error_msg}")
            return {
                "success": False,
                "error": "http_error",
                "status_code": e.response.status_code,
                "message": error_msg
            }
            
        except Exception as e:
            error_msg = f"Unexpected error sending data: {str(e)}"
            print(f"❌ {error_msg}")
            return {
                "success": False,
                "error": "unexpected_error",
                "message": error_msg
            }