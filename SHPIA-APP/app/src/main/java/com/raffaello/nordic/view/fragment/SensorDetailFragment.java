package com.raffaello.nordic.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.view.adapter.SensorDetailPageAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SensorDetailFragment extends CommonFragment {

    private NordicDevice sensor;

    public SensorDetailFragment() {};

    private SensorDetailPageAdapter adapter;

    @BindView(R.id.sensorDetailTabs)
    TabLayout tabLayout;

    @BindView(R.id.sensorDetailPager)
    ViewPager2 pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_detail, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pager.setUserInputEnabled(false);

        // Getting arguments
        if(getArguments() != null){
            sensor = SensorDetailFragmentArgs.fromBundle(getArguments()).getSensor();
        }

        adapter = new SensorDetailPageAdapter(this, sensor);
        pager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, pager,
                (tab, position) -> {
                    switch (position){
                        case 0: tab.setText("ENVIRONMENT"); break;
                        case 1: tab.setText("MOTION"); break;
                    }
                }
        ).attach();

    }


}