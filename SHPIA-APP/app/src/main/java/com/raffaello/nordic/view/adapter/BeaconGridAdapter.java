package com.raffaello.nordic.view.adapter;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.model.BeaconDevice;
import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.view.fragment.AmbientDetailFragmentDirections;
import com.raffaello.nordic.view.fragment.SensorAddFragmentDirections;

import org.altbeacon.beacon.Beacon;

public class BeaconGridAdapter extends RecyclerView.Adapter<BeaconGridAdapter.SensorViewHolder>{

    // Adapter for holding devices found through scanning.
    private ArrayList<Device> beaconDevices;
    private ArrayList<Beacon> beacons;
    private Ambient ambient;
    private boolean lockEdit;

    public BeaconGridAdapter(ArrayList<Device> devicesList, Ambient a, boolean lockEdit) {
        super();
        beaconDevices = devicesList;
        beacons=new ArrayList<>();
        ambient=a;
        this.lockEdit = lockEdit;
    }

    @NonNull
    @Override
    public BeaconGridAdapter.SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
        return new BeaconGridAdapter.SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        TextView name = holder.itemView.findViewById(R.id.sensorCardName);
        TextView desc = holder.itemView.findViewById(R.id.sensorCardDesc);
        TextView address = holder.itemView.findViewById(R.id.sensorCardAddress);
        ImageView check = holder.itemView.findViewById(R.id.sensorCardCheck);
        ConstraintLayout layout = holder.itemView.findViewById(R.id.sensorCardLayout);

        Device device = beaconDevices.get(position);
        name.setText(device.name);
        desc.setText(device.description);
        address.setText(device.address);

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

    public void addDevice(Beacon device) {
        if(!beaconDevices.contains(new BeaconDevice(ambient.id,""+device.getBluetoothAddress(),device.getBluetoothName(),(""+device.getDistance()).substring(0,4)+"m",1))){
            beacons.add(device);
            beaconDevices.add(new BeaconDevice(ambient.id,""+device.getBluetoothAddress(),device.getBluetoothName(),(""+device.getDistance()).substring(0,4)+"m",1));
            notifyDataSetChanged();
        }
    }

    public void clear() {
        beacons.clear();
        beaconDevices.clear();
        notifyDataSetChanged();
    }

    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }

    class SensorViewHolder extends RecyclerView.ViewHolder {

        public View itemView;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}