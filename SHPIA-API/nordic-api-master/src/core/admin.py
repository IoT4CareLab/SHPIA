# Django
from django.contrib import admin

# Core
from .models import *

# Register your models here.
admin.site.register(Ambient)
admin.site.register(Sensor)