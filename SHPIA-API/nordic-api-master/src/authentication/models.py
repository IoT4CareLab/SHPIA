# Django
from django.db import models
from django.contrib.auth.models import AbstractUser
from django.conf import settings
from django.db.models.signals import post_save
from django.dispatch import receiver

# DRF
from rest_framework.authtoken.models import Token

# App
from core.models import Ambient

class User(AbstractUser):
    ambients = models.ManyToManyField(Ambient, related_name='users', blank=True)


@receiver(post_save, sender=settings.AUTH_USER_MODEL)
def create_auth_token(sender, instance=None, created=False, **kwargs):
    if created:
        Token.objects.create(user=instance)