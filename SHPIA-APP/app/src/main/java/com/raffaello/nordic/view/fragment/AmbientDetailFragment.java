package com.raffaello.nordic.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.view.adapter.AmbientDetailPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AmbientDetailFragment extends CommonFragment {

    private Ambient ambient;

    public AmbientDetailFragment() {}

    private AmbientDetailPagerAdapter adapter;

    @BindView(R.id.ambientDetailTabs)
    TabLayout tabLayout;

    @BindView(R.id.ambientDetailPager)
    ViewPager2 pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ambient_detail, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pager.setUserInputEnabled(false);

        if(getArguments() != null){
            ambient = AmbientDetailFragmentArgs.fromBundle(getArguments()).getAmbient();
        }

        adapter = new AmbientDetailPagerAdapter(this, ambient);
        pager.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, pager,
                (tab, position) -> {
                    switch (position){
                        case 0: tab.setText("SENSORS"); break;
                        case 1: tab.setText("SUBLEVEL"); break;
                    }
                }
        ).attach();

        NavController navController = NavHostFragment.findNavController(this);
        MutableLiveData<Boolean> liveData = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("newAmbientAdded");
        liveData.observe(getViewLifecycleOwner(), s -> {
            if (s){
                LevelsListFragment fragment = (LevelsListFragment) getChildFragmentManager().getFragments().get(1);
                fragment.refreshViewModel();
            }
        });
    }

}