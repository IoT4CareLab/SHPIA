package com.raffaello.nordic.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.viewmodel.SensorConfigViewModel;
import com.raffaello.nordic.viewmodel.SensorsAddViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SensorConfigFragment extends BottomSheetDialogFragment {

    private SensorConfigViewModel viewModel;
    private NordicDevice sensor;
    private boolean lockEdit;

    @BindView(R.id.sensorConfigName)
    TextView sensorName;

    @BindView(R.id.sensorConfigDesc)
    TextView sensorDesc;

    //@BindView(R.id.sensorConfigPriority)
    //TextView sensorPriority;

    @BindView(R.id.sensorConfigButton)
    Button confirmButton;

    @BindView(R.id.sensorConfigDeleteButton)
    Button deleteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_config, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(SensorConfigViewModel.class);

        if(getArguments() != null){
            sensor = SensorConfigFragmentArgs.fromBundle(getArguments()).getSensor();
            lockEdit = SensorConfigFragmentArgs.fromBundle(getArguments()).getLockEdit();
        }

        // Set default value
        if(lockEdit){
            sensorName.setText(sensor.name);
            sensorDesc.setText(sensor.description);
            confirmButton.setVisibility(View.GONE);
            //sensorPriority.setText(String.valueOf(sensor.priority));
        }

        confirmButton.setOnClickListener(v -> {
            if(!sensorName.getText().toString().isEmpty())
                sensor.name = sensorName.getText().toString();
            if(!sensorDesc.getText().toString().isEmpty())
                sensor.description = sensorDesc.getText().toString();
            //if(!sensorPriority.getText().toString().isEmpty())
            //    sensor.priority = Integer.parseInt(sensorPriority.getText().toString());
            viewModel.addSensorToAmbient(sensor);
        });

        if(!lockEdit){
            deleteButton.setVisibility(View.GONE);
        }
        else {
            deleteButton.setOnClickListener(v -> {
                viewModel.deleteSensor(sensor);
            });
        }


        // Observe ViewModel
        observeViewModel();
    }

    private void observeViewModel(){
        viewModel.newSensorAdded.observe(getViewLifecycleOwner(), bool -> {
            if (bool != null && bool instanceof Boolean) {
                NavController navController = NavHostFragment.findNavController(this);
                navController.popBackStack(R.id.sensorConfigFragment, false);
                navController.popBackStack(R.id.sensorAddFragment, false);
                navController.navigateUp();
            }
        });
        viewModel.sensorDeleted.observe(getViewLifecycleOwner(), bool -> {
            if (bool != null && bool instanceof Boolean) {
                NavController navController = NavHostFragment.findNavController(this);
                // navController.popBackStack(R.id.sensorConfigFragment, false);
                NavHostFragment.findNavController(this).getPreviousBackStackEntry().getSavedStateHandle().set("sensorRemoved", true);
                navController.navigateUp();
            }
        });
    }
}