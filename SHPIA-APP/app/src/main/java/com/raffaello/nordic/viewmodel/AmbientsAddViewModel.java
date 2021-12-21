package com.raffaello.nordic.viewmodel;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.snackbar.Snackbar;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.util.DocumentType;
import com.raffaello.nordic.util.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AmbientsAddViewModel extends AndroidViewModel {

    // Backend
    private NordicApiService nordicApiService = NordicApiService.getInstance();

    // Live data
    public MutableLiveData<Boolean> created = new MutableLiveData<>();
    public MutableLiveData<Boolean> isOnline = new MutableLiveData<>();

    public AmbientsAddViewModel(@NonNull Application application) {
        super(application);
    }


    public void addAmbientFromName(String name, Ambient parent){
        Ambient ambient = new Ambient(name, false);
        addNewAmbient(ambient, parent);

    }
    public void addAmbientFromKey(String key, Ambient parent){
        Ambient ambient = new Ambient(key, true);
        addNewAmbient(ambient, parent);
    }

    private void addNewAmbient(Ambient ambient, Ambient parent){

        ambient.parent = parent == null ? null : parent.id;
        NordicApi api = nordicApiService.getApi();

        String header = "Token " +  SharedPreferencesHelper.getInstance(getApplication()).getAuthToken();
        Call<Ambient> call = api.addAmbient(header, ambient);

        call.enqueue(
                new Callback<Ambient>() {
                    @Override
                    public void onResponse(Call<Ambient> call, Response<Ambient> response) {
                        if(!response.isSuccessful()){
                            created.setValue(false);
                        }
                        else {
                            created.setValue(true);
                            SharedPreferencesHelper.getInstance(getApplication()).saveUpdateTime(0, parent == null ? "null" : String.valueOf(parent.parent), DocumentType.AMBIENT);
                        }

                    }

                    @Override
                    public void onFailure(Call<Ambient> call, Throwable t) {
                        t.printStackTrace();
                        isOnline.setValue(false);
                    }
                }
        );


    }

}
