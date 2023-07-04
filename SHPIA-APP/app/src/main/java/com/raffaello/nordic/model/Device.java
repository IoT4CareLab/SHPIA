package com.raffaello.nordic.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

//super class for all the kinds of sensors
public class Device implements Parcelable{
    @JsonProperty("ambient")
    @SerializedName("ambient")
    public long ambient;

    @JsonProperty("address")
    @SerializedName("address")
    public String address;

    @JsonProperty("name")
    @SerializedName("name")
    public String name;

    @JsonProperty("description")
    @SerializedName("description")
    public String description;

    @JsonProperty("priority")
    @SerializedName("priority")
    public int priority;


    public Device(long ambient, String address, String name, String description, int priority) {
        this.ambient = ambient;
        this.address = address;
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ambient=" + ambient +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object)
    {
        boolean isEqual= false;

        if (object != null && object instanceof Device)
        {
            String tmpAddr = ((Device) object).address;
            isEqual = (this.address.equals(tmpAddr));
        }

        return isEqual;
    }

    // Jackson
    public Device() {}

    public String getDocumentId(){
        return "document::device:" + address;
    }

    // Parcelable
    protected Device(Parcel in) {
        ambient = in.readLong();
        address = in.readString();
        name = in.readString();
        description = in.readString();
        priority = in.readInt();
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(ambient);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(priority);
    }

}
