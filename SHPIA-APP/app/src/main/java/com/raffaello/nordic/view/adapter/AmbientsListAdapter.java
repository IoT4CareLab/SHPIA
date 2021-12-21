package com.raffaello.nordic.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.view.fragment.AmbientDetailFragmentDirections;
import com.raffaello.nordic.view.fragment.AmbientListFragment;
import com.raffaello.nordic.view.fragment.AmbientListFragmentDirections;

import java.util.ArrayList;
import java.util.List;

public class AmbientsListAdapter extends RecyclerView.Adapter<AmbientsListAdapter.AmbientViewHolder> {

    private ArrayList<Ambient> ambientsList;

    public AmbientsListAdapter(ArrayList<Ambient> ambientsList){
        this.ambientsList = ambientsList;
    }

    public void updateAmbientList(List<Ambient> newAmbientsList){
        ambientsList.clear();
        ambientsList.addAll(newAmbientsList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AmbientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ambient, parent, false);
        return new AmbientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmbientViewHolder holder, int position) {
        TextView name = holder.itemView.findViewById(R.id.ambientListCardName);
        TextView levels = holder.itemView.findViewById(R.id.ambientListCardLevels);
        TextView shared = holder.itemView.findViewById(R.id.ambientListCardShared);
        Chip chip = holder.itemView.findViewById(R.id.ambientListCardSensors);
        LinearLayout layout = holder.itemView.findViewById(R.id.ambientCardLayout);

        Ambient ambient = ambientsList.get(position);

        layout.setOnClickListener(v -> {

            if(ambient.isRoot()){

                AmbientListFragmentDirections.ActionDetail action2 = AmbientListFragmentDirections.actionDetail(ambient);
                action2.setTitle(name.getText().toString());
                Navigation.findNavController(layout).navigate(action2);
            }

            else {

                AmbientDetailFragmentDirections.ActionNestedDetail action2 = AmbientDetailFragmentDirections.actionNestedDetail(ambient);
                action2.setTitle(name.getText().toString());
                Navigation.findNavController(layout).navigate(action2);
            }


        });

        name.setText(ambient.name);
        if(ambient.levels != null)
            levels.setText("Structured in " + ambient.levels.size() + " levels");
        else
            levels.setText("Structured in null levels");

        if(ambient.users.size() == 1)
            shared.setText("Private ambient");
        else
            shared.setText("Ambient shared between " + ambient.usersString());

        if(ambient.parent != null)
            shared.setVisibility(View.GONE);

        chip.setText(String.valueOf(ambient.sensors.size()));
    }

    @Override
    public int getItemCount() {
        return ambientsList.size();
    }

    public void removeItem(int position) {
        ambientsList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Ambient item, int position) {
        ambientsList.add(position, item);
        notifyItemInserted(position);
    }

    public Ambient getAmbient(int pos){
        return ambientsList.get(pos);
    }

    class AmbientViewHolder extends RecyclerView.ViewHolder {

        public View itemView;

        public AmbientViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
