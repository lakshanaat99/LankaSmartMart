import firebase_admin
from firebase_admin import credentials, firestore, storage
import os
import datetime

# ---------------------------------------------------------
# HOW TO UPLOAD YOUR PRODUCTS TO FIREBASE STORAGE & FIRESTORE:
# 1. Open a terminal or command prompt in this project folder.
# 2. Run: pip install firebase-admin
# 3. Go to Firebase Console -> Storage -> Get your bucket name (e.g. your-project.appspot.com)
# 4. Paste it in BUCKET_NAME below.
# 5. Run this script: python seed_firestore.py
# ---------------------------------------------------------

# Replace with YOUR bucket name from Firebase Storage Console
BUCKET_NAME = "lankasmart-217c6.firebasestorage.app" 

try:
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred, {
        'storageBucket': BUCKET_NAME
    })
except Exception as e:
    print("❌ ERROR: Could not find or read 'serviceAccountKey.json' or bucket issue.")
    print("Error details:", e)
    exit()

db = firestore.client()
bucket = storage.bucket()

# Local folder where images are stored
DRAWABLE_PATH = "app/src/main/res/drawable/"

def upload_image(local_filename, folder):
    """Uploads an image to Firebase Storage and returns the download URL."""
    local_path = os.path.join(DRAWABLE_PATH, local_filename)
    if not os.path.exists(local_path):
        print(f"⚠️ Warning: Image {local_filename} not found in {DRAWABLE_PATH}. Using fallback.")
        return "https://via.placeholder.com/150"

    blob = bucket.blob(f"{folder}/{local_filename}")
    blob.upload_from_filename(local_path)
    
    # Make the blob public so we can get a simple URL
    blob.make_public()
    return blob.public_url

categories = [
    {"id": 1, "name": "Groceries", "image": "groceries.png"},
    {"id": 2, "name": "Household", "image": "household.png"},
    {"id": 3, "name": "Personal Care", "image": "personal_care.png"},
    {"id": 4, "name": "Stationery", "image": "stationery.png"}
]

products = [
    {"id": 101, "name": "Fresh Coconut", "description": "Large fresh coconut directly from the farm.", "price": 100.00, "image": "coconut.png", "categoryId": 1, "isAvailable": True},
    {"id": 102, "name": "Sliced Bread", "description": "Freshly baked customized sliced bread.", "price": 150.00, "image": "bread.png", "categoryId": 1, "isAvailable": True},
    {"id": 103, "name": "Olive Oil", "description": "Imported extra virgin olive oil.", "price": 1200.00, "image": "oliveoli.png", "categoryId": 1, "isAvailable": True},
    {"id": 104, "name": "Fresh Milk", "description": "Pasteurized fresh milk.", "price": 300.00, "image": "milk.png", "categoryId": 1, "isAvailable": True},

    {"id": 201, "name": "Dog Food", "description": "Premium nutrition for your pet.", "price": 2500.00, "image": "dogfood.png", "categoryId": 2, "isAvailable": True},
    {"id": 202, "name": "Water Bottle", "description": "Durable reusable water bottle.", "price": 850.00, "image": "waterbottle.png", "categoryId": 2, "isAvailable": True},
    {"id": 203, "name": "Hammer", "description": "Heavy-duty steel hammer.", "price": 1100.00, "image": "hammer.png", "categoryId": 2, "isAvailable": True},
    {"id": 204, "name": "Iron", "description": "Steam iron for clothes.", "price": 4500.00, "image": "iorn.png", "categoryId": 2, "isAvailable": True},
    {"id": 205, "name": "Non-stick Pan", "description": "High quality non-stick frying pan.", "price": 3200.00, "image": "pan.png", "categoryId": 2, "isAvailable": True},

    {"id": 301, "name": "Herbal Shampoo", "description": "Organic herbal shampoo for daily use.", "price": 450.00, "image": "shampoo.png", "categoryId": 3, "isAvailable": True},
    {"id": 302, "name": "Face Cream", "description": "Moisturizing face cream.", "price": 950.00, "image": "cream.png", "categoryId": 3, "isAvailable": True},
    {"id": 303, "name": "Makeup Kit", "description": "Complete mostly essential makeup kit.", "price": 5500.00, "image": "makeup.png", "categoryId": 3, "isAvailable": True},

    {"id": 401, "name": "Blue Pens (Pack of 5)", "description": "Smooth writing blue ballpoint pens.", "price": 250.00, "image": "pen.png", "categoryId": 4, "isAvailable": True},
    {"id": 402, "name": "HB Pencils", "description": "Box of 10 HB pencils.", "price": 120.00, "image": "pencil.png", "categoryId": 4, "isAvailable": True},
    {"id": 403, "name": "Stapler", "description": "Standard office stapler.", "price": 450.00, "image": "stapler.png", "categoryId": 4, "isAvailable": True},
    {"id": 404, "name": "Notebook", "description": "200 page ruled notebook.", "price": 350.00, "image": "book.png", "categoryId": 4, "isAvailable": True}
]

def seed_data():
    print(f"🚀 Starting Cloud Migration to {BUCKET_NAME}...")
    
    # Process Categories
    for cat in categories:
        print(f"📸 Uploading category icon: {cat['image']}...")
        icon_url = upload_image(cat['image'], "categories")
        
        doc_ref = db.collection("Categories").document(str(cat["id"]))
        doc_ref.set({
            "id": cat["id"],
            "name": cat["name"],
            "iconUrl": icon_url
        })
        print(f"✅ Added Category: {cat['name']} with URL: {icon_url[:40]}...")

    # Process Products
    for prod in products:
        print(f"📸 Uploading product image: {prod['image']}...")
        image_url = upload_image(prod['image'], "products")
        
        doc_ref = db.collection("Products").document(str(prod["id"]))
        doc_ref.set({
            "id": prod["id"],
            "name": prod["name"],
            "description": prod["description"],
            "price": prod["price"],
            "imageUrl": image_url,
            "categoryId": prod["categoryId"],
            "isAvailable": prod["isAvailable"]
        })
        print(f"✅ Added Product: {prod['name']} with URL: {image_url[:40]}...")
        
    print("\n🎉 SUCCESS! All local images have been moved to Firebase Storage.")
    print("✅ Firestore has been updated with these live Storage URLs.")

if __name__ == "__main__":
    seed_data()
