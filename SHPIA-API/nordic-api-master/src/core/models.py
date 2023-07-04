# Django
from django.db import models
from djongo import models as nomodels

# Ambient
class Ambient(models.Model):
    name = models.CharField(max_length=100)
    key = models.CharField(max_length=100, unique=True)
    parent = models.ForeignKey('self', on_delete=models.CASCADE, related_name='levels', blank=True, null=True)
    master = models.ForeignKey('self', on_delete=models.CASCADE, related_name='root', blank=True, null=True)

    def __str__(self):
        return self.name

    class params:
        db = 'default'


# Sensor
class Sensor(models.Model):
    address = models.CharField(max_length=100, primary_key=True)
    name = models.CharField(max_length=100)
    description = models.CharField(max_length=100)
    priority = models.PositiveSmallIntegerField()
    ambient = models.ForeignKey(Ambient, on_delete=models.CASCADE, related_name='sensors')

    def __str__(self):
        return self.name

    class params:
        db = 'default'

