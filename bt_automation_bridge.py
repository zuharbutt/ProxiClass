import asyncio
import requests
import time
from bleak import BleakScanner

# CONFIGURATION
JAVA_API_URL = "http://localhost:8080/api/teacher/session/{}/detect-bulk"
SCAN_INTERVAL = 10  # Seconds between scans

async def run_automation_bridge():
    print("=== Bluetooth Attendance Automation Bridge ===")
    print("Scanning for students every {} seconds...".format(SCAN_INTERVAL))
    print("Make sure you have an active session in the browser!")

    while True:
        try:
            # 1. Get Active Sessions from Java
            res = requests.get("http://localhost:8080/api/teacher/sessions/active")
            active_sessions = res.json()
            
            if not active_sessions:
                print("[INFO] Waiting for teacher to start a session in the browser...")
                await asyncio.sleep(5)
                continue

            # 2. Scan for all nearby Bluetooth devices
            print("[SCAN] Searching for devices...")
            devices = await BleakScanner.discover(timeout=5.0)
            
            # Extract all MAC addresses/IDs found
            found_ids = [d.address for d in devices]
            print("[SCAN] Found {} devices nearby.".format(len(found_ids)))

            # 3. Send all found IDs to every active session
            for session in active_sessions:
                session_id = session['id']
                target_url = JAVA_API_URL.format(session_id)
                
                try:
                    # Send as bulk list
                    post_res = requests.post(target_url, json=found_ids)
                    if post_res.status_code == 200:
                        print("[SYNC] Sent detections to session: {}".format(session['courseName']))
                except Exception as e:
                    print("[ERR] Failed to sync with Java: {}".format(e))

        except Exception as e:
            print("[ERR] Bridge Error: {}".format(e))
            print("Make sure the Java backend is running at localhost:8080")

        await asyncio.sleep(SCAN_INTERVAL)

if __name__ == "__main__":
    try:
        asyncio.run(run_automation_bridge())
    except KeyboardInterrupt:
        print("\nStopping automation bridge...")
