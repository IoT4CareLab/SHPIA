package com.raffaello.nordic.view.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.view.fragment.AmbientListFragment;
import com.raffaello.nordic.view.fragment.LevelsListFragment;
import com.raffaello.nordic.view.fragment.SensorsListFragment;

public class AmbientDetailPagerAdapter extends FragmentStateAdapter {

    private Ambient ambient;

    public AmbientDetailPagerAdapter(Fragment fragment, Ambient ambient){
        super(fragment);
        this.ambient = ambient;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        switch (position){
            case 1: fragment = new LevelsListFragment(ambient); break;
            default: fragment = new SensorsListFragment(ambient); break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
