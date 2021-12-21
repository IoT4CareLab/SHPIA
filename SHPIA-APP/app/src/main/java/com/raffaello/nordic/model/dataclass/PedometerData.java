package com.raffaello.nordic.model.dataclass;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class PedometerData implements Cloneable{

    @JsonProperty("steps")
    @SerializedName("steps")
    public int steps;

    @JsonProperty("duration")
    @SerializedName("duration")
    public long duration;

    public PedometerData(int steps, long duration) {
        this.steps = steps;
        this.duration = duration;
    }

    // Jackson
    public PedometerData(){}

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
