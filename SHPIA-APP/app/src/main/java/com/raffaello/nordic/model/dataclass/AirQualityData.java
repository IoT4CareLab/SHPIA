package com.raffaello.nordic.model.dataclass;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class AirQualityData implements Cloneable{

    @JsonProperty("eco2")
    @SerializedName("eco2")
    public int eco2;

    @JsonProperty("tvoc")
    @SerializedName("tvoc")
    public int tvoc;

    public AirQualityData(int eco2, int tvoc) {
        this.eco2 = eco2;
        this.tvoc = tvoc;
    }

    // Jackson
    public AirQualityData() {}

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
