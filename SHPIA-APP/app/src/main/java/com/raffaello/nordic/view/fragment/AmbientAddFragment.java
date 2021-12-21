package com.raffaello.nordic.view.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.viewmodel.AmbientsAddViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AmbientAddFragment extends BottomSheetDialogFragment {

    private AmbientsAddViewModel viewModel;
    private Ambient parentAmbient;

    @BindView(R.id.ambientAddButton)
    Button addButton;

    @BindView(R.id.ambientAddFromKeyButton)
    Button addFromKeyButton;

    @BindView(R.id.ambientAddText)
    EditText ambientName;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ambient_add, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null){
            parentAmbient = AmbientAddFragmentArgs.fromBundle(getArguments()).getAmbient();
        }

        viewModel = ViewModelProviders.of(this).get(AmbientsAddViewModel.class);

        addButton.setOnClickListener(v -> {
            viewModel.addAmbientFromName(ambientName.getText().toString(), parentAmbient);
        });

        if(parentAmbient != null) {
            addFromKeyButton.setVisibility(View.GONE);
            ambientName.setHint("Nome");
        }
        else{
            addFromKeyButton.setOnClickListener(v -> {
                viewModel.addAmbientFromKey(ambientName.getText().toString(), parentAmbient);
            });
        }

        ambientName.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ambientName.getWindowToken(), 0);

            }
            return true;
        });

        observeViewModel();

    }

    private void observeViewModel(){

        viewModel.created.observe(this, created -> {
            if(created != null && created instanceof Boolean && created){
                NavHostFragment.findNavController(this).getPreviousBackStackEntry().getSavedStateHandle().set("newAmbientAdded", true);
                NavHostFragment.findNavController(this).navigateUp();
            }
            if(created != null && created instanceof Boolean && !created){
                NavHostFragment.findNavController(this).getPreviousBackStackEntry().getSavedStateHandle().set("newAmbientAddedFailed", true);
                NavHostFragment.findNavController(this).navigateUp();
            }
        });

        viewModel.isOnline.observe(getViewLifecycleOwner(), isOnline -> {
            if(isOnline != null && isOnline instanceof Boolean && !isOnline){
                NavHostFragment.findNavController(this).getPreviousBackStackEntry().getSavedStateHandle().set("deviceIsOffline", true);
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }
}