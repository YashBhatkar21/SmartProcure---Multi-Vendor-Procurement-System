import requests
import json
import sys

BASE_URL = "http://localhost:8081/api"

def log(message):
    print(f"[TEST] {message}")

def test_login():
    url = f"{BASE_URL}/auth/login"
    payload = {
        "email": "customer@smartprocure.com",
        "password": "password"
    }
    headers = {"Content-Type": "application/json"}
    
    log(f"Logging in to {url}...")
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        data = response.json()
        token = data.get("accessToken")
        if token:
            log("Login successful. Token obtained.")
            return token
        else:
            log("Login failed. No token in response.")
            print(data)
            sys.exit(1)
    except Exception as e:
        log(f"Login failed: {e}")
        if 'response' in locals():
            print(response.text)
        sys.exit(1)

def test_create_request(token):
    url = f"{BASE_URL}/procurement-requests"
    payload = {
        "title": "Office Chairs",
        "description": "Need 10 ergonomic chairs.",
        "budget": 5000.00,
        "dueDate": "2026-12-31T23:59:59Z"
    }
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    log(f"Creating procurement request at {url}...")
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        data = response.json()
        log(f"Request created. ID: {data.get('id')}")
        return data.get('id')
    except Exception as e:
        log(f"Create request failed: {e}")
        if 'response' in locals():
            print(response.text)
        sys.exit(1)

def test_list_requests(token):
    url = f"{BASE_URL}/procurement-requests"
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    log(f"Listing procurement requests at {url}...")
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        data = response.json()
        log(f"Found {len(data)} requests.")
        if len(data) > 0:
            log("List requests successful.")
        else:
            log("List requests returned empty list (unexpected if we just created one).")
    except Exception as e:
        log(f"List requests failed: {e}")
        if 'response' in locals():
            print(response.text)
        sys.exit(1)

if __name__ == "__main__":
    token = test_login()
    req_id = test_create_request(token)
    test_list_requests(token)
    log("ALL TESTS PASSED")
