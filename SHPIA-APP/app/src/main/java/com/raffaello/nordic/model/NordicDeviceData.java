package com.raffaello.nordic.model;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.raffaello.nordic.model.dataclass.AirQualityData;
import com.raffaello.nordic.model.dataclass.EulerAngleData;
import com.raffaello.nordic.model.dataclass.PedometerData;
import com.raffaello.nordic.model.dataclass.QuaternionData;
import com.raffaello.nordic.model.dataclass.ThreeAxisData;

import java.sql.Timestamp;


public class NordicDeviceData implements Cloneable{

    @JsonProperty("timestamp")
    @SerializedName("timestamp")
    private String timestamp;

    @JsonProperty("sensorAddress")
    @SerializedName("sensorAddress")
    private String sensorAddress;

    @JsonProperty("batteryLevel")
    @SerializedName("batteryLevel")
    private int batteryLevel;

    @JsonProperty("temperature")
    @SerializedName("temperature")
    private String temperature;

    @JsonProperty("pressure")
    @SerializedName("pressure")
    private String pressure;

    @JsonProperty("humidity")
    @SerializedName("humidity")
    private String humidity;

    @JsonProperty("airQuality")
    @SerializedName("airQuality")
    private AirQualityData airQuality;

    @JsonProperty("orientation")
    @SerializedName("orientation")
    private int orientation;

    @JsonProperty("quaternion")
    @SerializedName("quaternion")
    private QuaternionData quaternion;

    @JsonProperty("pedometer")
    @SerializedName("pedometer")
    private PedometerData pedometer;

    @JsonProperty("accelerometer")
    @SerializedName("accelerometer")
    private ThreeAxisData accelerometer;

    @JsonProperty("gyroscope")
    @SerializedName("gyroscope")
    private ThreeAxisData gyroscope;

    @JsonProperty("compass")
    @SerializedName("compass")
    private ThreeAxisData compass;

    @JsonProperty("eulerAngle")
    @SerializedName("eulerAngle")
    private EulerAngleData eulerAngle;

    @JsonProperty("rotationMatrix")
    @SerializedName("rotationMatrix")
    private byte[] rotationMatrix;

    @JsonProperty("heading")
    @SerializedName("heading")
    private float heading;

    @JsonProperty("gravity")
    @SerializedName("gravity")
    private ThreeAxisData gravity;

    @JsonProperty("microphone")
    @SerializedName("microphone")
    private byte[] microphone;

    public NordicDeviceData(String sensorAddress){
        this.sensorAddress = sensorAddress;
    }

    // Jackson
    public NordicDeviceData(){}
    public String getDocumentId(){
        return "document::nordicData:" + sensorAddress + ":" + timestamp;
    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // Getters and setters
    public void setTimestamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.timestamp = timestamp.toString();
    }

    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setAirQuality(AirQualityData airQuality) {
        this.airQuality = airQuality;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setQuaternion(QuaternionData quaternion) {
        this.quaternion = quaternion;
    }

    public void setPedometer(PedometerData pedometer) {
        this.pedometer = pedometer;
    }

    public void setAccelerometer(ThreeAxisData accelerometer) {
        this.accelerometer = accelerometer;
    }

    public void setGyroscope(ThreeAxisData gyroscope) {
        this.gyroscope = gyroscope;
    }

    public void setCompass(ThreeAxisData compass) {
        this.compass = compass;
    }

    public void setEulerAngle(EulerAngleData eulerAngle) {
        this.eulerAngle = eulerAngle;
    }

    public void setRotationMatrix(byte[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public void setGravity(ThreeAxisData gravity) {
        this.gravity = gravity;
    }

    public void setMicrophone(byte[] microphone) {
        this.microphone = microphone;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public String getSensorAddress() {
        return sensorAddress;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getPressure() {
        return pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public AirQualityData getAirQuality() {
        return airQuality;
    }

    public int getOrientation() {
        return orientation;
    }

    public QuaternionData getQuaternion() {
        return quaternion;
    }

    public PedometerData getPedometer() {
        return pedometer;
    }

    public ThreeAxisData getAccelerometer() {
        return accelerometer;
    }

    public ThreeAxisData getGyroscope() {
        return gyroscope;
    }

    public ThreeAxisData getCompass() {
        return compass;
    }

    public EulerAngleData getEulerAngle() {
        return eulerAngle;
    }

    public byte[] getRotationMatrix() {
        return rotationMatrix;
    }

    public float getHeading() {
        return heading;
    }

    public ThreeAxisData getGravity() {
        return gravity;
    }

    public byte[] getMicrophone() {
        return microphone;
    }
}
