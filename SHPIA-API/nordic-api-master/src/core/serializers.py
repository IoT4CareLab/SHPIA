# Django
from collections import OrderedDict

# DRF
from rest_framework import serializers

# Core
from .models import *



class AmbientSerializerCreate(serializers.ModelSerializer):

    class Meta:
        model = Ambient
        fields = ['id', 'name', 'parent', 'levels']


class AmbientSerializerRetrieve(serializers.ModelSerializer):

    users = serializers.SerializerMethodField()

    class Meta:
        model = Ambient
        fields = ['id', 'key', 'name', 'parent', 'master', 'levels', 'sensors', 'users']

    def get_users(self, obj):

        users = []

        for u in obj.users.all():
            users.append(u.username)

        return users


class SensorSerializer(serializers.ModelSerializer):

    class Meta:
        model = Sensor
        fields = ['address', 'name', 'description', 'priority', 'ambient']



class NonNullModelSerializer(serializers.ModelSerializer):

    def to_representation(self, instance):
        result = super(NonNullModelSerializer, self).to_representation(instance)

        return OrderedDict([(key, result[key]) for key in result if (result[key] is not None and result[key]!='' or result[key] != '' and result[key]!=[])])

