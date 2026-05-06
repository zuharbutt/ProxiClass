import asyncio
import urllib.request
import json
import time
from bleak import BleakScanner

# ── Configuration ──────────────────────────────────────────────────────
SERVER_URL  = "http://localhost:8080"
SCAN_INTERVAL = 8.0   # seconds between scans
POLL_INTERVAL = 5.0   # seconds between polling for an active session

# Credentials for a teacher account (needed to call teacher APIs)
# The bridge uses a shared service token — using dr.ahmed as default
TEACHER_USERNAME = "dr.ahmed"
TEACHER_PASSWORD = "password123"

token = None   # JWT token, fetched at startup

# ── Auth ───────────────────────────────────────────────────────────────
def login():
    global token
    url  = f"{SERVER_URL}/api/auth/teacher/login"
    body = json.dumps({"username": TEACHER_USERNAME, "password": TEACHER_PASSWORD}).encode()
    req  = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req) as r:
            data  = json.loads(r.read().decode())
            token = data.get("token")
            print(f"✅ Logged in as {TEACHER_USERNAME}")
    except Exception as e:
        print(f"❌ Login failed: {e}")

def api_get(path):
    req = urllib.request.Request(f"{SERVER_URL}{path}")
    req.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read().decode())

def api_post(path, body=None):
    data = json.dumps(body or {}).encode()
    req  = urllib.request.Request(f"{SERVER_URL}{path}", data=data, method="POST")
    req.add_header("Authorization", f"Bearer {token}")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read().decode())

# ── Helpers ────────────────────────────────────────────────────────────
def get_active_session():
    """Returns the first active session ID, or None."""
    try:
        sessions = api_get("/api/teacher/sessions/active")
        if sessions:
            s = sessions[0]
            return s["id"], s.get("courseName", ""), s.get("section", "")
    except Exception as e:
        print(f"⚠️  Could not fetch sessions: {e}")
    return None, None, None

def send_devices(session_id, device_addresses):
    if not device_addresses:
        return
    try:
        result = api_post(f"/api/teacher/session/{session_id}/detect-bulk", device_addresses)
        processed = result.get("processed", 0)
        print(f"   → Sent {len(device_addresses)} MACs to server (processed={processed})")
    except Exception as e:
        print(f"   ❌ Error sending to server: {e}")

# ── Main Scanner ───────────────────────────────────────────────────────
async def run():
    print("╔══════════════════════════════════════════════╗")
    print("║  📡  NU Bluetooth Auto-Attendance Bridge     ║")
    print("╚══════════════════════════════════════════════╝")

    # 1. Authenticate
    login()
    if not token:
        print("Cannot continue without a valid token. Check credentials.")
        return

    print("Waiting for teacher to start a session...\n")

    current_session = None

    while True:
        # 2. Poll for an active session
        session_id, course, section = get_active_session()

        if not session_id:
            if current_session is not None:
                print("ℹ️  Session ended. Waiting for next session...")
                current_session = None
            await asyncio.sleep(POLL_INTERVAL)
            continue

        if session_id != current_session:
            print(f"\n🟢 Active Session Detected!")
            print(f"   Session ID : {session_id}")
            print(f"   Course     : {course}")
            print(f"   Section    : {section}")
            print(f"   Scanning every {SCAN_INTERVAL}s...\n")
            current_session = session_id

        # 3. Scan for nearby Bluetooth devices
        try:
            print(f"🔍 Scanning for Bluetooth devices...")
            devices = await BleakScanner.discover(timeout=5.0)
            addresses = [d.address for d in devices]

            if devices:
                for d in devices:
                    name = d.name or "Unknown"
                    print(f"   📱 Found: {d.address}  ({name})")
            else:
                print("   (no devices found nearby)")

            send_devices(session_id, addresses)

        except Exception as e:
            print(f"⚠️  Scan error: {e}")

        print(f"⏲️  Next scan in {SCAN_INTERVAL}s...")
        await asyncio.sleep(SCAN_INTERVAL)

if __name__ == "__main__":
    try:
        asyncio.run(run())
    except KeyboardInterrupt:
        print("\n👋 Scanner stopped.")
