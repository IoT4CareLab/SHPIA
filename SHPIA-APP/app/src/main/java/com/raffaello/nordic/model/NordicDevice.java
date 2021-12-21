package com.raffaello.nordic.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;


public class NordicDevice implements Parcelable {

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


    public NordicDevice(long ambient, String address, String name, String description, int priority) {
        this.ambient = ambient;
        this.address = address;
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "NordicDevice{" +
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

        if (object != null && object instanceof NordicDevice)
        {
            String tmpAddr = ((NordicDevice) object).address;
            isEqual = (this.address.equals(tmpAddr));
        }

        return isEqual;
    }


    // Jackson
    public NordicDevice() {}

    public String getDocumentId(){
        return "document::device:" + address;
    }


    // Parcelable
    protected NordicDevice(Parcel in) {
        ambient = in.readLong();
        address = in.readString();
        name = in.readString();
        description = in.readString();
        priority = in.readInt();
    }

    public static final Creator<NordicDevice> CREATOR = new Creator<NordicDevice>() {
        @Override
        public NordicDevice createFromParcel(Parcel in) {
            return new NordicDevice(in);
        }

        @Override
        public NordicDevice[] newArray(int size) {
            return new NordicDevice[size];
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

    // Parcelable

}
