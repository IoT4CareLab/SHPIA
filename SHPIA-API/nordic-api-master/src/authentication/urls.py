# Django
from django.urls import path

# DRF
from rest_framework.authtoken.views import obtain_auth_token;

#Core
from .views import UserCreateView

urlpatterns = [
    path('user/register/', UserCreateView.as_view(), name='user_register'),
    path('user/login/', obtain_auth_token, name='user_login'),
]
