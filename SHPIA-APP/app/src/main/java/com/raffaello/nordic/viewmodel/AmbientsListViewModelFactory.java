package com.raffaello.nordic.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.raffaello.nordic.model.Ambient;

public class AmbientsListViewModelFactory implements ViewModelProvider.Factory {

    private Application application;
    private Ambient parentAmbient;

    public AmbientsListViewModelFactory(Application application, Ambient parentAmbient) {
        this.application = application;
        this.parentAmbient = parentAmbient;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AmbientsListViewModel(application, parentAmbient);
    }
}
