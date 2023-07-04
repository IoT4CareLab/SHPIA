package com.raffaello.nordic.model;

import android.os.Parcel;

public class BeaconDevice extends Device{

    public BeaconDevice(long ambient, String address, String name, String description, int priority) {
        super(ambient, address, name, description, priority);
    }

    @Override
    public String toString() {
        return "BeaconDevice{" +
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

        if (object != null && object instanceof BeaconDevice)
        {
            String tmpAddr = ((BeaconDevice) object).address;
            isEqual = (this.address.equals(tmpAddr));
        }
        return isEqual;
    }


    // Jackson
    public BeaconDevice() {super();}

    public String getDocumentId(){
        return "document::device:" + address;
    }

    // Parcelable
    protected BeaconDevice(Parcel in) {
        super(in);
    }

    public static final Creator<BeaconDevice> CREATOR = new Creator<BeaconDevice>() {
        @Override
        public BeaconDevice createFromParcel(Parcel in) {
            return new BeaconDevice(in);
        }

        @Override
        public BeaconDevice[] newArray(int size) {
            return new BeaconDevice[size];
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
