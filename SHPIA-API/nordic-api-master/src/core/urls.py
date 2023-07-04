# Django
from django.urls import path

# Core
from .views import *

urlpatterns = [
    path('ambient/', AmbientList.as_view(), name='ambient_list_root'),
    path('ambient/<int:parent_id>/', AmbientList.as_view(), name='ambient_list'),
    path('ambient/delete/<int:ambient_id>/', AmbientDetail.as_view(), name='ambient_delete'),
    path('sensor/<int:ambient_id>/', SensorList.as_view(), name='sensor_list'),
    path('sensor/delete/<str:sensor_id>/', SensorDetail.as_view(), name='sensor_delete'),
    path('sensor/submit/', SensorSubmit.as_view(), name='sensor_submit'),
    path('sensor/sync/', SensorSubmitSync.as_view(), name='sensor_sync'),
    path('sensor/nested/', SensorNestedList.as_view(), name='sensor_nested_list'),
    path('sensor/all/', SensorAll.as_view(), name='sensor_all_list'),
]