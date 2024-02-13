import os
import json
import firebase_admin
from firebase_admin import credentials, db, storage
from config import DATABASE_URL, STORAGE_BUCKET
from PIL import Image
import copy

LANG_PATH = 'lang'
ASSETS_PATH = 'assets'
CACHE_FILE = ".uploadcache"

cred = credentials.Certificate("admin/serviceAccountKey.json")  
firebase_admin.initialize_app(cred, {
    'databaseURL': DATABASE_URL,
    'storageBucket': STORAGE_BUCKET
})

ref = db.reference('/portfolio')
bucket = storage.bucket()

#Reads each json file that is in LANG_PATH 
def retrieve_data():
    data = {}
    for filename in os.listdir(LANG_PATH):
        if filename.endswith('.json'):
            file_path = os.path.join(LANG_PATH, filename)
            language_key = filename.split('.')[0]

        with open(file_path, 'r', encoding='utf-8') as file:
            json_data = json.load(file)
            updated_json_data = process_media(json_data)
            data[language_key] = updated_json_data

    with open("contact.json", 'r', encoding='utf-8') as file:
            json_data = json.load(file)
            data["contact"] = json_data
    return data

#Probably needs to seperate logic of upload and checking instance
def process_media(data):
    # Make a deep copy of the data to avoid modifying the original
    updated_data = copy.deepcopy(data)

    if isinstance(updated_data, list):
        for index, item in enumerate(updated_data):
            updated_data[index] = process_media(item)
    elif isinstance(updated_data, dict):
        if 'icon' in updated_data:
            icon_url = updated_data.get('icon', '')
            icon_file_path = os.path.join(ASSETS_PATH, icon_url)
            if os.path.isfile(icon_file_path):
                uploaded_icon_url = upload_file_to_storage(icon_file_path)
                if uploaded_icon_url:
                    updated_data['icon'] = uploaded_icon_url

        if 'bannerImage' in updated_data:
            icon_url = updated_data.get('bannerImage', '')
            icon_file_path = os.path.join(ASSETS_PATH, icon_url)
            if os.path.isfile(icon_file_path):
                uploaded_icon_url = upload_file_to_storage(icon_file_path)
                if uploaded_icon_url:
                    updated_data['bannerImage'] = uploaded_icon_url

        if 'media' in updated_data:
            media_array = updated_data['media']
            for index, media_item in enumerate(media_array):
                image_url = media_item.get('imageUrl', '')
                image_file_path = os.path.join(ASSETS_PATH, image_url)
                if '/' in image_url:
                    image_file_path = os.path.join(ASSETS_PATH, *image_url.split('/'))
                if os.path.isfile(image_file_path):
                    uploaded_url = upload_file_to_storage(image_file_path)
                    if uploaded_url:
                        updated_data['media'][index]['imageUrl'] = uploaded_url

        if 'projects' in updated_data:
            projects_array = updated_data['projects']
            for index, project_item in enumerate(projects_array):
                print(f"checking medias in {project_item['title']} that has a length of {len(project_item['media'])}")
                if 'media' in project_item:
                    project_media_array = project_item['media']
                    for media_index, media_item in enumerate(project_media_array):
                        media_image_url = media_item.get('imageUrl', '')
                        media_image_file_path = os.path.join(ASSETS_PATH, media_image_url)
                        if os.path.isfile(media_image_file_path):
                            media_uploaded_url = upload_file_to_storage(media_image_file_path)
                            if media_uploaded_url:
                                updated_data['projects'][index]['media'][media_index]['imageUrl'] = media_uploaded_url

        for key, value in updated_data.items():
            updated_data[key] = process_media(value)

    return updated_data

def load_cache():
    try:
        with open(CACHE_FILE, 'r') as cache_file:
            return json.load(cache_file)
    except FileNotFoundError:
        return {}

def save_cache(cache_data):
    with open(CACHE_FILE, 'w') as cache_file:
        json.dump(cache_data, cache_file)

#We check if the file is already uploaded in the .uploadcache file
#if already uploaded serve the cached url 
def upload_file_to_storage(file_path):
    try:
        timestamp = ''#int(time.time())

        file_name, file_extension = os.path.splitext(os.path.basename(file_path))
        new_file_name = f"{file_name}_{timestamp}{file_extension}"

        # Check if the file is already in the cache
        cache = load_cache()
        if file_name in cache:
            print(f"File already uploaded. Returning cached URL.")
            return cache[file_name]

        blob = bucket.blob(new_file_name)
        print(f"Uploading file: {file_name}")
        # Upload the original file
        blob.upload_from_filename(file_path)

        # Make the original file public
        blob.make_public()

        # Create and upload the thumbnail
        if file_extension.lower() in ['.png', '.jpg', '.jpeg', '.webp']:
            print(f"Creating thumbnail for: {file_name}")
            thumbnail_path = create_thumbnail(file_path, file_extension)
            if thumbnail_path:
                thumbnail_file_name = f"{file_name}_{timestamp}_thumbnail{file_extension}"
                thumbnail_blob = bucket.blob(thumbnail_file_name)
                print("Uploading thumbnail")
                thumbnail_blob.upload_from_filename(thumbnail_path)
                thumbnail_blob.make_public()
                os.remove(thumbnail_path)  # Remove the temporary thumbnail file

        # Update the cache with the new file information
        cache[file_name] = blob.public_url
        save_cache(cache)

        return blob.public_url
    except Exception as e:
        print(f"File upload failed: {e}")
        return None
    
def create_thumbnail(file_path, file_extension):
    try:
        image = Image.open(file_path)

        # Set the maximum dimension for the thumbnail
        max_dimension = 150  

        # Calculate new width and height while maintaining aspect ratio
        width, height = image.size

        if width > height:
            new_width = max_dimension
            new_height = int((max_dimension / width) * height)
        else:
            new_height = max_dimension
            new_width = int((max_dimension / height) * width)

        # Resize the image
        image.thumbnail((new_width, new_height))

        
        thumbnail_path = f"temp_thumbnail{file_extension}"
        image.save(thumbnail_path)

        return thumbnail_path
    except Exception as e:
        print(f"Thumbnail creation failed: {e}")
        return None
    

def insert_language_data(language_data):
    try:
        ref.update(language_data)
        print(f"Data insertion successful")
    except Exception as e:
        print(f"Data insertion failed: {e}")


language_data = retrieve_data()
insert_language_data(language_data)