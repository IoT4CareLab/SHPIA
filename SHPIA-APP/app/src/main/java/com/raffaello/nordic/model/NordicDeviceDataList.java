package com.raffaello.nordic.model;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class NordicDeviceDataList implements Cloneable{

    private String k;
    private String address;
    private Map<String, String> temperatureValues = new HashMap<>();
    private Map<String, String> pressureValues = new HashMap<>();
    private Map<String, String> humidityValues = new HashMap<>();
    private Map<String, Integer> airQualityValues_eco2 = new HashMap<>();
    private Map<String, Integer> airQualityValues_tvoc = new HashMap<>();
    private Map<String, Integer> orientationValues = new HashMap<>();
    private Map<String, Float> quaternionValues_w = new HashMap<>();
    private Map<String, Float> quaternionValues_x = new HashMap<>();
    private Map<String, Float> quaternionValues_y = new HashMap<>();
    private Map<String, Float> quaternionValues_z = new HashMap<>();
    private Map<String, Float> accelerometerValues_x = new HashMap<>();
    private Map<String, Float> accelerometerValues_y = new HashMap<>();
    private Map<String, Float> accelerometerValues_z = new HashMap<>();
    private Map<String, Float> gyroscopeValues_x = new HashMap<>();
    private Map<String, Float> gyroscopeValues_y = new HashMap<>();
    private Map<String, Float> gyroscopeValues_z = new HashMap<>();
    private Map<String, Float> compassValues_x = new HashMap<>();
    private Map<String, Float> compassValues_y = new HashMap<>();
    private Map<String, Float> compassValues_z = new HashMap<>();
    private Map<String, Float> eulerAngleValues_roll = new HashMap<>();
    private Map<String, Float> eulerAngleValues_pitch = new HashMap<>();
    private Map<String, Float> eulerAngleValues_yaw = new HashMap<>();
    private Map<String, Float> headingValues = new HashMap<>();
    private Map<String, Float> gravityValues_x = new HashMap<>();
    private Map<String, Float> gravityValues_y = new HashMap<>();
    private Map<String, Float> gravityValues_z = new HashMap<>();

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public NordicDeviceDataList(String address) {
        this.k = getAlphaNumericString(8);
        this.address = address;
    }

    // Jackson
    public NordicDeviceDataList(){}
    public String getDocumentId(){
        return "document_nordicData_" + address + "_" + k;
    }

    static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


    public String getK() {
        return k;
    }

    public String getAddress() {
        return address;
    }

    public Map<String, String> getTemperatureValues() {
        return temperatureValues;
    }

    public Map<String, String> getPressureValues() {
        return pressureValues;
    }

    public Map<String, String> getHumidityValues() {
        return humidityValues;
    }

    public Map<String, Integer> getAirQualityValues_eco2() {
        return airQualityValues_eco2;
    }

    public Map<String, Integer> getAirQualityValues_tvoc() {
        return airQualityValues_tvoc;
    }

    public Map<String, Integer> getOrientationValues() {
        return orientationValues;
    }

    public Map<String, Float> getQuaternionValues_w() {
        return quaternionValues_w;
    }

    public Map<String, Float> getQuaternionValues_x() {
        return quaternionValues_x;
    }

    public Map<String, Float> getQuaternionValues_y() {
        return quaternionValues_y;
    }

    public Map<String, Float> getQuaternionValues_z() {
        return quaternionValues_z;
    }

    public Map<String, Float> getAccelerometerValues_x() {
        return accelerometerValues_x;
    }

    public Map<String, Float> getAccelerometerValues_y() {
        return accelerometerValues_y;
    }

    public Map<String, Float> getAccelerometerValues_z() {
        return accelerometerValues_z;
    }

    public Map<String, Float> getGyroscopeValues_x() {
        return gyroscopeValues_x;
    }

    public Map<String, Float> getGyroscopeValues_y() {
        return gyroscopeValues_y;
    }

    public Map<String, Float> getGyroscopeValues_z() {
        return gyroscopeValues_z;
    }

    public Map<String, Float> getCompassValues_x() {
        return compassValues_x;
    }

    public Map<String, Float> getCompassValues_y() {
        return compassValues_y;
    }

    public Map<String, Float> getCompassValues_z() {
        return compassValues_z;
    }

    public Map<String, Float> getEulerAngleValues_roll() {
        return eulerAngleValues_roll;
    }

    public Map<String, Float> getEulerAngleValues_pitch() {
        return eulerAngleValues_pitch;
    }

    public Map<String, Float> getEulerAngleValues_yaw() {
        return eulerAngleValues_yaw;
    }

    public Map<String, Float> getHeadingValues() {
        return headingValues;
    }

    public Map<String, Float> getGravityValues_x() {
        return gravityValues_x;
    }

    public Map<String, Float> getGravityValues_y() {
        return gravityValues_y;
    }

    public Map<String, Float> getGravityValues_z() {
        return gravityValues_z;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        String s = "";
        try{
            s = objectMapper.writeValueAsString(this);
        }catch (Exception e){ }

        return s;
    }
}
