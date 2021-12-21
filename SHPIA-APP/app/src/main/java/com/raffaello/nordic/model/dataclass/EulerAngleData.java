package com.raffaello.nordic.model.dataclass;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class EulerAngleData implements Cloneable{

    @JsonProperty("roll")
    @SerializedName("roll")
    public float roll;

    @JsonProperty("pitch")
    @SerializedName("pitch")
    public float pitch;

    @JsonProperty("yaw")
    @SerializedName("yaw")
    public float yaw;

    public EulerAngleData(float roll, float pitch, float yaw) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    // Jackson
    public EulerAngleData(){}

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
