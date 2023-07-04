# Django
from django.shortcuts import render

# DRF
from rest_framework import status, permissions
from rest_framework.response import Response
from rest_framework.views import APIView

# App
from .serializers import LoginSerializer, RegisterSerializer
from .models import User


class UserCreateView(APIView):
    permission_classes = (permissions.AllowAny,)

    def post(self, request, format='json'):
        serializer = RegisterSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            if user:
                json = serializer.data
                return Response(json, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


