package com.raffaello.nordic.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.view.fragment.SensorDetailMotionFragment;
import com.raffaello.nordic.view.fragment.SensorDetailEnvFragment;

public class SensorDetailPageAdapter extends FragmentStateAdapter {
    
    private Device sensor;
    
    public SensorDetailPageAdapter(Fragment fragment, Device sensor){
        super(fragment);
        this.sensor = sensor;
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;


        switch (position){
            case 1: fragment = new SensorDetailMotionFragment(sensor); break;
            default: fragment = new SensorDetailEnvFragment(sensor); break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
