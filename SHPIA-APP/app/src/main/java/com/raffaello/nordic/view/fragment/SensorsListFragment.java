package com.raffaello.nordic.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.view.adapter.SensorGridAdapter;
import com.raffaello.nordic.viewmodel.SensorsListViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SensorsListFragment extends Fragment {

    private Ambient ambient;
    private SensorGridAdapter sensorGridAdapter;
    private SensorsListViewModel viewModel;

    @BindView(R.id.sensorsListError)
    TextView error;

    @BindView(R.id.sensorsListProgressBar)
    ProgressBar progressBar;

    @BindView(R.id.sensorsListEmpty)
    TextView listEmpty;

    @BindView(R.id.sensorsListRecyclerView)
    RecyclerView sensorsList;

    @BindView(R.id.sensorsListFTB)
    FloatingActionButton ftb;

    @BindView(R.id.sensorsListSwipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    public SensorsListFragment(Ambient ambient){
        this.ambient = ambient;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensors_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(SensorsListViewModel.class);
        viewModel.setAmbient(ambient);

        //((TextView) view.findViewById(R.id.sensorsListText)).setText("Sensori dell'ambiente " + ambient.id);

        // Recycler view setup
        sensorGridAdapter = new SensorGridAdapter(new ArrayList<>(), ambient, true);
        sensorsList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        sensorsList.setAdapter(sensorGridAdapter);

        viewModel.refresh();

        // Event handlers
        swipeRefreshLayout.setOnRefreshListener(() -> {
            sensorsList.setVisibility(View.GONE);
            error.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            viewModel.forceRefresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        ftb.setOnClickListener(v -> {
            NavDirections action = AmbientDetailFragmentDirections.actionSensorAdd(ambient);
            Navigation.findNavController(ftb).navigate(action);
        });

        // Data from other fragments
        NavController navController = NavHostFragment.findNavController(this);
        MutableLiveData<Boolean> liveData = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("sensorRemoved");

        liveData.observe(getViewLifecycleOwner(), bool -> {
            if (bool != null && bool) {
                viewModel.forceRefresh();
            }
        });

        // Observe ViewModel
        observeViewModel();

    }

    private void observeViewModel(){
        viewModel.liveDevices.observe(getViewLifecycleOwner(), sensors -> {
            if (sensors != null && sensors instanceof List) {
                sensorsList.setVisibility(View.VISIBLE);
                sensorGridAdapter.updateAmbientList(sensors);
            }
        });
        viewModel.loadError.observe(getViewLifecycleOwner(), bool -> {
            if (bool != null && bool instanceof Boolean) {
                error.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.isLoading.observe(getViewLifecycleOwner(), bool -> {
            if (bool != null && bool instanceof Boolean) {
                progressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.isEmpty.observe(getViewLifecycleOwner(), bool -> {
            if (bool != null && bool instanceof Boolean) {
                listEmpty.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
    }
}