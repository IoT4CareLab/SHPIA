package com.raffaello.nordic.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SharedPreferencesHelper {

    private static final String PREF_TIME = "Cache time";
    private static final String AUTH_TOKEN = "Auth token";
    private static final String MOTION_STATUS = "pref_enable_motion";
    private static final String TEMP_STATUS = "pref_enable_temperature";
    private static final String PRESSURE_STATUS = "pref_enable_pressure";
    private static final String HUMIDITY_STATUS = "pref_enable_humidity";
    private static final String AQ_STATUS = "pref_enable_airQuality";
    private static SharedPreferencesHelper instance;
    private SharedPreferences prefs;


    private SharedPreferencesHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context);
        }

        return instance;
    }

    // Refresh time
    public void saveUpdateTime(long time, String documentId, DocumentType documentType) {
        prefs.edit().putLong(documentType.name() + "::" + documentId + ":" + PREF_TIME, time).apply();
    }

    public long getUpdateTime(String documentId, DocumentType documentType) {
        return prefs.getLong(documentType.name() + "::" + documentId + ":" + PREF_TIME, 0);
    }

    // Sensors
    public boolean getMotionStatus(){
        return prefs.getBoolean(MOTION_STATUS, true);
    }

    public boolean getTemperatureStatus(){
        return prefs.getBoolean(TEMP_STATUS, false);
    }

    public boolean getPressureStatus(){
        return prefs.getBoolean(PRESSURE_STATUS, false);
    }

    public boolean getHumidityStatus(){
        return prefs.getBoolean(HUMIDITY_STATUS, false);
    }

    public boolean getAirQualityStatus(){
        return prefs.getBoolean(AQ_STATUS, false);
    }

    // Save user token
    public void saveAuthToken(String token) {
        prefs.edit().putString(AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return prefs.getString(AUTH_TOKEN, "");
    }

    public void clearSharedPrefs(){
        prefs.edit().clear().commit();
    }



}
