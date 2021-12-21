package com.raffaello.nordic.model;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {

    @SerializedName("username")
    public String username;

    @SerializedName("password")
    public String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
