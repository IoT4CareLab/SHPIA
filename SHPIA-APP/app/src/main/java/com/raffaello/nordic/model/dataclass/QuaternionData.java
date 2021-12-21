package com.raffaello.nordic.model.dataclass;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class QuaternionData implements Cloneable{

    @JsonProperty("w")
    @SerializedName("w")
    public float w;

    @JsonProperty("x")
    @SerializedName("x")
    public float x;

    @JsonProperty("y")
    @SerializedName("y")
    public float y;

    @JsonProperty("z")
    @SerializedName("z")
    public float z;

    public QuaternionData(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Jackson
    public QuaternionData(){}

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
