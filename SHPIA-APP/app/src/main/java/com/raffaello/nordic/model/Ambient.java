package com.raffaello.nordic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.exceptions.Exceptions;
import io.reactivex.exceptions.OnErrorNotImplementedException;


public class Ambient implements Parcelable {

    @JsonProperty("id")
    @SerializedName("id")
    public long id;

    @JsonProperty("key")
    @SerializedName("key")
    public String key;

    @JsonProperty("name")
    @SerializedName("name")
    public String name;

    @SerializedName("parent")
    @JsonProperty("parent")
    public Long parent;

    @SerializedName("master")
    @JsonProperty("master")
    public Long master;

    @SerializedName("levels")
    @JsonProperty("levels")
    public List<Long> levels;

    @SerializedName("sensors")
    @JsonProperty("sensors")
    public List<String> sensors;

    @SerializedName("users")
    @JsonProperty("users")
    public List<String> users;

    public Ambient(String s, boolean isKey) {

        if(isKey)
            this.key = s;
        else
            this.name = s;

        this.levels = new ArrayList<>();
    }

    @JsonIgnore
    public boolean isRoot(){
        if(parent == null)
            return true;
        return false;
    }

    @JsonIgnore
    public String usersString(){

        return users.stream().map(Object::toString).collect(Collectors.joining(", "));

    }


    @Override
    public String toString() {
        return "Ambient{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", parent=" + parent +
                ", master=" + master +
                ", levels=" + levels +
                ", sensors=" + sensors +
                ", users=" + users +
                '}';
    }

    // Jackson
    public Ambient() {}

    public String getDocumentId(){
        return "document::ambient:" + id;
    }


    // Parcelable
    protected Ambient(Parcel in) {
        id = in.readLong();
        key = in.readString();
        name = in.readString();
        if (in.readByte() == 0) {
            parent = null;
        } else {
            parent = in.readLong();
        }
        if (in.readByte() == 0) {
            master = null;
        } else {
            master = in.readLong();
        }
        sensors = in.createStringArrayList();
        users = in.createStringArrayList();
    }

    public static final Creator<Ambient> CREATOR = new Creator<Ambient>() {
        @Override
        public Ambient createFromParcel(Parcel in) {
            return new Ambient(in);
        }

        @Override
        public Ambient[] newArray(int size) {
            return new Ambient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(key);
        dest.writeString(name);
        if (parent == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(parent);
        }
        if (master == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(master);
        }
        dest.writeStringList(sensors);
        dest.writeStringList(users);
    }



}
