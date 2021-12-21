package com.raffaello.nordic.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NordicApi {

    // Authentication
    @POST("auth/user/login/")
    Call<AuthResponse> login(@Body AuthRequest request);


    // CRUD
    @GET("api/ambient/")
    Call<List<Ambient>> getAmbientList(@Header("Authorization") String header);

    @GET("api/ambient/{id}/")
    Call<List<Ambient>> getAmbientListFromParent(
            @Header("Authorization") String header,
            @Path("id") long parentId);

    @GET("api/sensor/{id}/")
    Call<List<NordicDevice>> getSensorListFromAmbient(
            @Header("Authorization") String header,
            @Path("id") long ambientId);

    @GET("api/sensor/nested/")
    Call<List<NordicDevice>> getAllUserSensors(
            @Header("Authorization") String header);

    @GET("api/sensor/all/")
    Call<List<NordicDevice>> getAllSensors(
            @Header("Authorization") String header);

    @DELETE("api/ambient/delete/{id}/")
    Call<Void> deleteAmbient(
            @Header("Authorization") String header,
            @Path("id") long ambientId);

    @DELETE("api/sensor/delete/{id}/")
    Call<Void> deleteSensor(
            @Header("Authorization") String header,
            @Path("id") String sensorId);

    @POST("api/ambient/")
    Call<Ambient> addAmbient(
            @Header("Authorization") String header,
            @Body Ambient ambient);

    @POST("api/sensor/{id}/")
    Call<NordicDevice> addSensor(
            @Header("Authorization") String header,
            @Path("id") long ambientId,
            @Body NordicDevice sensor);

    @POST("api/sensor/submit/")
    Call<Void> submitData2(
            @Header("Authorization") String header,
            @Body List<NordicDeviceDataList> data);

    @POST("api/sensor/sync/")
    Call<Void> syncData(
            @Header("Authorization") String header,
            @Body List<String> data);

    @POST("api/sensor/submit/")
    Call<Void> submitData(
            @Header("Authorization") String header,
            @Body List<NordicDeviceData> data);

}
