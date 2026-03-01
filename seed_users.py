import firebase_admin
from firebase_admin import credentials, firestore, auth
import datetime

# ---------------------------------------------------------
# HOW TO SEED USERS TO FIREBASE:
# 1. Ensure 'serviceAccountKey.json' is in this folder.
# 2. Run: python seed_users.py
# ---------------------------------------------------------

# TEST PASSWORD FOR ALL SEEDED ACCOUNTS: SmartMart123!
TEST_PASSWORD = "SmartMart123!"

try:
    cred = credentials.Certificate('serviceAccountKey.json')
    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred)
except Exception as e:
    print("❌ ERROR: Could not find or read 'serviceAccountKey.json'.")
    print("Error details:", e)
    exit()

db = firestore.client()

users = [
    {
        "uid": "test_user_001",
        "username": "lakshana",
        "name": "Lakshana Atapattu",
        "email": "lakshana@example.com",
        "photoUrl": "",
        "createdAt": datetime.datetime.now(),
        "updatedAt": datetime.datetime.now(),
        "lastLogin": datetime.datetime.now(),
        "isActive": True,
        "role": "customer"
    },
    {
        "uid": "test_user_002",
        "username": "admin_smart",
        "name": "Smart Mart Admin",
        "email": "admin@lankasmart.lk",
        "photoUrl": "",
        "createdAt": datetime.datetime.now(),
        "updatedAt": datetime.datetime.now(),
        "lastLogin": datetime.datetime.now(),
        "isActive": True,
        "role": "admin"
    }
]

def seed_users():
    print(f"Starting user seed (Firestore + Auth) to Firebase...")
    print(f"Default password for all accounts will be: {TEST_PASSWORD}\n")
    
    for user_data in users:
        try:
            # 1. Create or Update Auth account
            try:
                user = auth.get_user_by_email(user_data["email"])
                print(f"User {user_data['email']} already exists in Auth. Updating password...")
                auth.update_user(user.uid, password=TEST_PASSWORD)
            except auth.UserNotFoundError:
                user = auth.create_user(
                    email=user_data["email"],
                    password=TEST_PASSWORD,
                    display_name=user_data["name"]
                )
                print(f"Created Auth account for: {user_data['email']}")
            
            # Use the actual Auth UID for the Firestore document
            actual_uid = user.uid
            user_data["uid"] = actual_uid
            
            # 2. Create or Update Firestore document
            db.collection("users").document(actual_uid).set(user_data)
            print(f"Saved Firestore data for: {user_data['name']} (@{user_data['username']})")
            
        except Exception as e:
            print(f"❌ Error seeding {user_data['email']}: {e}")

    print("\n✅ Success! Test accounts are ready to use.")
    print(f"Login with: {users[0]['email']} / {TEST_PASSWORD}")

if __name__ == "__main__":
    seed_users()
