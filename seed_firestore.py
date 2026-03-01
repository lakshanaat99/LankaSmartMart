import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

# ---------------------------------------------------------
# HOW TO UPLOAD YOUR PRODUCTS TO FIREBASE:
# 1. Open a terminal or command prompt in this project folder.
# 2. Run: pip install firebase-admin
# 3. Go to Firebase Web Console -> Settings (Gear Icon) -> Project Settings -> Service Accounts
# 4. Click "Generate new private key". It downloads a JSON file.
# 5. Rename that downloaded JSON file exactly to 'serviceAccountKey.json'.
# 6. Place 'serviceAccountKey.json' in this SAME folder (LankaSmartMart folder).
# 7. Run this script in the terminal: python seed_firestore.py
# ---------------------------------------------------------

try:
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred)
except Exception as e:
    print("❌ ERROR: Could not find or read 'serviceAccountKey.json'. Did you download it from Firebase?")
    print("Error details:", e)
    exit()

db = firestore.client()

# Sample public URLs for testing the Cloud Migration
URL_PREFIX = "https://raw.githubusercontent.com/lakshanaat99/LankaSmartMart/main/app/src/main/res/drawable/"

categories = [
    {"id": 1, "name": "Groceries", "iconUrl": URL_PREFIX + "groceries.png"},
    {"id": 2, "name": "Household", "iconUrl": URL_PREFIX + "household.png"},
    {"id": 3, "name": "Personal Care", "iconUrl": URL_PREFIX + "personal_care.png"},
    {"id": 4, "name": "Stationery", "iconUrl": URL_PREFIX + "stationery.png"}
]

products = [
    {"id": 101, "name": "Fresh Coconut", "description": "Large fresh coconut directly from the farm.", "price": 100.00, "imageUrl": URL_PREFIX + "coconut.png", "categoryId": 1, "isAvailable": True},
    {"id": 102, "name": "Sliced Bread", "description": "Freshly baked customized sliced bread.", "price": 150.00, "imageUrl": URL_PREFIX + "bread.png", "categoryId": 1, "isAvailable": True},
    {"id": 103, "name": "Olive Oil", "description": "Imported extra virgin olive oil.", "price": 1200.00, "imageUrl": URL_PREFIX + "oliveoli.png", "categoryId": 1, "isAvailable": True},
    {"id": 104, "name": "Fresh Milk", "description": "Pasteurized fresh milk.", "price": 300.00, "imageUrl": URL_PREFIX + "milk.png", "categoryId": 1, "isAvailable": True},

    {"id": 201, "name": "Dog Food", "description": "Premium nutrition for your pet.", "price": 2500.00, "imageUrl": URL_PREFIX + "dogfood.png", "categoryId": 2, "isAvailable": True},
    {"id": 202, "name": "Water Bottle", "description": "Durable reusable water bottle.", "price": 850.00, "imageUrl": URL_PREFIX + "waterbottle.png", "categoryId": 2, "isAvailable": True},
    {"id": 203, "name": "Hammer", "description": "Heavy-duty steel hammer.", "price": 1100.00, "imageUrl": URL_PREFIX + "hammer.png", "categoryId": 2, "isAvailable": True},
    {"id": 204, "name": "Iron", "description": "Steam iron for clothes.", "price": 4500.00, "imageUrl": URL_PREFIX + "iorn.png", "categoryId": 2, "isAvailable": True},
    {"id": 205, "name": "Non-stick Pan", "description": "High quality non-stick frying pan.", "price": 3200.00, "imageUrl": URL_PREFIX + "pan.png", "categoryId": 2, "isAvailable": True},

    {"id": 301, "name": "Herbal Shampoo", "description": "Organic herbal shampoo for daily use.", "price": 450.00, "imageUrl": URL_PREFIX + "shampoo.png", "categoryId": 3, "isAvailable": True},
    {"id": 302, "name": "Face Cream", "description": "Moisturizing face cream.", "price": 950.00, "imageUrl": URL_PREFIX + "cream.png", "categoryId": 3, "isAvailable": True},
    {"id": 303, "name": "Makeup Kit", "description": "Complete mostly essential makeup kit.", "price": 5500.00, "imageUrl": URL_PREFIX + "makeup.png", "categoryId": 3, "isAvailable": True},

    {"id": 401, "name": "Blue Pens (Pack of 5)", "description": "Smooth writing blue ballpoint pens.", "price": 250.00, "imageUrl": URL_PREFIX + "pen.png", "categoryId": 4, "isAvailable": True},
    {"id": 402, "name": "HB Pencils", "description": "Box of 10 HB pencils.", "price": 120.00, "imageUrl": URL_PREFIX + "pencil.png", "categoryId": 4, "isAvailable": True},
    {"id": 403, "name": "Stapler", "description": "Standard office stapler.", "price": 450.00, "imageUrl": URL_PREFIX + "stapler.png", "categoryId": 4, "isAvailable": True},
    {"id": 404, "name": "Notebook", "description": "200 page ruled notebook.", "price": 350.00, "imageUrl": URL_PREFIX + "book.png", "categoryId": 4, "isAvailable": True}
]

def seed_data():
    print("Starting database seed to Firestore...")
    
    for cat in categories:
        doc_ref = db.collection("Categories").document(str(cat["id"]))
        doc_ref.set(cat)
        print(f"Added Category: {cat['name']}")

    for prod in products:
        doc_ref = db.collection("Products").document(str(prod["id"]))
        doc_ref.set(prod)
        print(f"Added Product: {prod['name']}")
        
    print("\n✅ Success! All Categories and Products have been uploaded to Firebase Firestore.")
    print("✅ The app will now fetch image URLs directly from Firestore!")

if __name__ == "__main__":
    seed_data()

