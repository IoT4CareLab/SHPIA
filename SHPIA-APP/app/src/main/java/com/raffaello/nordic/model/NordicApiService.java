package com.raffaello.nordic.model;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NordicApiService {

    private static NordicApiService instance;
    private static NordicApi api;


    public static NordicApiService getInstance(){
        if(instance == null){
            instance = new NordicApiService();
        }
        return instance;
    }


    public NordicApi getApi(){

        if(api == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.8:8000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            api = retrofit.create(NordicApi.class);
        }


        return api;
    }
}
