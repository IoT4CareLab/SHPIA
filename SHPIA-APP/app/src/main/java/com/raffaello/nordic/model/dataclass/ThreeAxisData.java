package com.raffaello.nordic.model.dataclass;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class ThreeAxisData implements Cloneable{

    @JsonProperty("x")
    @SerializedName("x")
    public float x;

    @JsonProperty("y")
    @SerializedName("y")
    public float y;

    @JsonProperty("z")
    @SerializedName("z")
    public float z;

    public ThreeAxisData(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Jackson
    public ThreeAxisData(){}

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
