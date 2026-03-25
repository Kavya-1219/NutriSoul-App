import requests
import json

URL = "http://180.235.121.253:8107/api/forgot-password/"
DATA = {"email": "user@example.com"}

try:
    response = requests.post(URL, json=DATA)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}")
except Exception as e:
    print(f"Error: {e}")
