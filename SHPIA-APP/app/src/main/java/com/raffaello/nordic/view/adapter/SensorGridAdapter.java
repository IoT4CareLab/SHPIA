package com.raffaello.nordic.view.adapter;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.NordicDevice;
import com.raffaello.nordic.view.activity.MainActivity;
import com.raffaello.nordic.view.fragment.AmbientDetailFragmentDirections;
import com.raffaello.nordic.view.fragment.LoginFragment;
import com.raffaello.nordic.view.fragment.SensorAddFragmentDirections;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.thingylib.ThingySdkManager;

public class SensorGridAdapter extends RecyclerView.Adapter<SensorGridAdapter.SensorViewHolder>{

    private ArrayList<NordicDevice> devices;
    private Ambient ambient;
    private boolean lockEdit;
    private List<String> connectedDevicesAddresses = new ArrayList<>();


    public SensorGridAdapter(ArrayList<NordicDevice> devicesList, Ambient ambient, boolean lockEdit){
        this.devices = devicesList;
        this.ambient = ambient;
        this.lockEdit = lockEdit;
    }

    public void updateAmbientList(List<NordicDevice> newDevicesList){
        devices.clear();
        devices.addAll(newDevicesList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
        List<BluetoothDevice> connectedDevices = ThingySdkManager.getInstance().getConnectedDevices();
        for(BluetoothDevice device : connectedDevices){
            connectedDevicesAddresses.add(device.getAddress());
        }
        return new SensorGridAdapter.SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        TextView name = holder.itemView.findViewById(R.id.sensorCardName);
        TextView desc = holder.itemView.findViewById(R.id.sensorCardDesc);
        TextView address = holder.itemView.findViewById(R.id.sensorCardAddress);
        ImageView check = holder.itemView.findViewById(R.id.sensorCardCheck);
        ConstraintLayout layout = holder.itemView.findViewById(R.id.sensorCardLayout);

        NordicDevice device = devices.get(position);
        name.setText(device.name);
        desc.setText(device.description);
        address.setText(device.address);

        // Check if connected
        if(connectedDevicesAddresses.contains(device.address))
            check.setVisibility(View.VISIBLE);
        else
            check.setVisibility(View.GONE);

        if(!lockEdit){
            layout.setOnClickListener(v -> {
                NavDirections action = SensorAddFragmentDirections.actionConfig(device, false);
                Navigation.findNavController(layout).navigate(action);
            });
        }
        else{
            layout.setOnLongClickListener(v -> {
                NavDirections action = AmbientDetailFragmentDirections.actionEdit(device, true);
                Navigation.findNavController(layout).navigate(action);
                return true;
            });

            layout.setOnClickListener(v ->{
                NavDirections action = AmbientDetailFragmentDirections.actionSensorDetail(device);
                Navigation.findNavController(layout).navigate(action);
            });
        }

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class SensorViewHolder extends RecyclerView.ViewHolder {

        public View itemView;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
