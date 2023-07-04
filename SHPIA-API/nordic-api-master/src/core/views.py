# Django
from django.http import Http404

# DRF
from rest_framework import status, permissions, authentication
from rest_framework.views import APIView
from rest_framework.response import Response

# Core
from .models import *
from .serializers import *

# Others
from pymongo import MongoClient
import random, string, json


class AmbientList(APIView):

    permission_classes = (permissions.IsAuthenticated,)

    def get(self, request, parent_id = None, format='json'):

        if parent_id:
            parent = self.get_object(parent_id)

            users = parent.master.users.all()

            if request.user in users:
                ambients = Ambient.objects.filter(parent = parent)
            
            else:
                return Response(status=status.HTTP_401_UNAUTHORIZED)

        else:
            ambients = Ambient.objects.filter(parent = None, users=request.user)

        serializer = AmbientSerializerRetrieve(ambients, many=True)

        return Response(serializer.data)


    def post(self, request, parent_id = None, format='json'):

        user = request.user

        # Share ambient
        if 'key' in request.data:
            ambient = self.get_object_from_key(request.data['key'])
            if ambient in user.ambients.all() or ambient.parent:
                return Response(status=status.HTTP_400_BAD_REQUEST)


            user.ambients.add(ambient)
            user.save()
            serializer = AmbientSerializerRetrieve(ambient)
            return Response(serializer.data, status=status.HTTP_201_CREATED)

        # Create new ambient
        else:
            serializer = AmbientSerializerCreate(data=request.data)
            if serializer.is_valid():
                ambient = serializer.save()

                if not ambient.parent:
                    ambient.master = ambient
                    user.ambients.add(ambient)
                    user.save()
                else:
                    ambient.master = ambient.parent.master
                
                ambient.key = ''.join(random.choice(string.ascii_uppercase + string.ascii_lowercase + string.digits) for _ in range(12))
                ambient.save()
                return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


    def get_object_from_key(self, key):
        try:
            return Ambient.objects.filter(key=key).first()
        except Ambient.DoesNotExist:
            raise Http404

    def get_object(self, pk):
        try:
            return Ambient.objects.get(pk=pk)
        except Ambient.DoesNotExist:
            raise Http404


class AmbientDetail(APIView):

    permission_classes = (permissions.IsAuthenticated,)


    def delete(self, request, ambient_id, format='json'):

        ambient = self.get_object(ambient_id)

        """         if ambient.levels.all():
            return Response("Ambient deleted", status=status.HTTP_406_NOT_ACCEPTABLE)

        else:
            ambient.delete()
            return Response("Ambient deleted", status=status.HTTP_200_OK) """

        ambient.delete()
        return Response("Ambient deleted", status=status.HTTP_200_OK)


    def get_object(self, pk):
        try:
            return Ambient.objects.get(pk=pk)
        except Ambient.DoesNotExist:
            raise Http404


class SensorList(APIView):

    permission_classes = (permissions.IsAuthenticated,)

    def get(self, request, ambient_id, format='json'):

        ambient = self.get_object(ambient_id)
        sensors = Sensor.objects.filter(ambient=ambient)
        serializer = SensorSerializer(sensors, many=True)

        return Response(serializer.data, status=status.HTTP_200_OK)


    def post(self, request, ambient_id = None, format='json'):
        serializer = SensorSerializer(data=request.data)
        if serializer.is_valid():
            sensor = serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


    def get_object(self, pk):
        try:
            return Ambient.objects.get(pk=pk)
        except Ambient.DoesNotExist:
            raise Http404


class SensorAll(APIView):

    permission_classes = (permissions.IsAuthenticated,)

    def get(self, request, format='json'):

        sensors = Sensor.objects.all()
        serializer = SensorSerializer(sensors, many=True)

        return Response(serializer.data, status=status.HTTP_200_OK)


class SensorDetail(APIView):

    permission_classes = (permissions.IsAuthenticated,)


    def delete(self, request, sensor_id, format='json'):

        sensor = self.get_object(sensor_id)
        sensor.delete()
        # TODO: delete
        #SensorData.objects.using('data').filter(address=sensor_id).delete()
        return Response("Sensor deleted", status=status.HTTP_200_OK)

    def get_object(self, pk):
        try:
            return Sensor.objects.get(pk=pk)
        except Sensor.DoesNotExist:
            raise Http404


class SensorNestedList(APIView):

    permission_classes = (permissions.IsAuthenticated,)

    def get(self, request, format='json'):

        ambients = request.user.ambients.all()
        sensors = Sensor.objects.all()

        user_sensors = []

        for sensor in sensors:
            master = sensor.ambient.master
            if master in ambients:
                user_sensors.append(sensor)

        serializer = SensorSerializer(user_sensors, many=True)

        return Response(serializer.data, status=status.HTTP_200_OK)

class SensorSubmit(APIView):

    permission_classes = (permissions.IsAuthenticated,)

    def post(self, request, format='json'):

        connection = MongoClient("mongodb://localhost:27017/")
        database = connection["nordic"]
        collection = database["data"]
        clean_data = []
        for d in request.data:
            reading = {}
            for k, v in d.items():
                if isinstance(v, dict) and len(v.keys()) > 0:
                    reading[k] = self.clean_timestamps(v)
                elif k == "address":
                    reading[k] = v
            clean_data.append(reading)
        collection.insert_many(clean_data)
        connection.close()
        return Response(status=status.HTTP_200_OK)

    def clean_timestamps(self, reading):
        clean_reading = {}
        for k,v in reading.items():
            clean_reading[k.replace(".", ":")] = v
        return {k:v for k,v in sorted(clean_reading.items())}


class SensorSubmitSync(APIView):

    permission_classes = (permissions.IsAuthenticated,)

    def post(self, request, format='json'):
        connection = MongoClient("mongodb://localhost:27017/")
        database = connection["nordic"]
        collection = database["data"]
        clean_data = []
        for readingString in request.data:
            d = json.loads(readingString)
            reading = {}
            for k, v in d.items():
                if isinstance(v, dict) and len(v.keys()) > 0:
                    reading[k] = self.clean_timestamps(v)
                elif k == "address":
                    reading[k] = v
            clean_data.append(reading)
        collection.insert_many(clean_data)
        connection.close()
        return Response(status=status.HTTP_200_OK)

    def clean_timestamps(self, reading):
        clean_reading = {}
        for k,v in reading.items():
            clean_reading[k.replace(".", ":")] = v
        return {k:v for k,v in sorted(clean_reading.items())}



