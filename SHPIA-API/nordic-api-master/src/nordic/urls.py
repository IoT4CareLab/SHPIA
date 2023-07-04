from django.contrib import admin
from django.urls import path, include

urlpatterns = [

    # Admin
    path('admin/', admin.site.urls),

    # Authentication
    path('auth/', include('authentication.urls')),

    # Core
    path('api/', include('core.urls'))
]
