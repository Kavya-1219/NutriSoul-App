import os
import django
from django.conf import settings

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'myproject.settings')
django.setup()

print(f"GEMINI_API_KEY: {settings.GEMINI_API_KEY}")
print(f"DATABASE: {settings.DATABASES['default']['NAME']}")
