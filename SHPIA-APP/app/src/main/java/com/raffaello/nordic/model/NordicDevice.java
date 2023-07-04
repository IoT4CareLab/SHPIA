package com.raffaello.nordic.model;

import android.os.Parcel;

public class NordicDevice extends Device {

    public NordicDevice(long ambient, String address, String name, String description, int priority) {
        super(ambient, address, name, description, priority);
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
    public NordicDevice() {super();}

    public String getDocumentId(){
        return "document::device:" + address;
    }


    // Parcelable
    protected NordicDevice(Parcel in) {
        super(in);
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
