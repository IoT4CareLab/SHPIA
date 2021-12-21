package com.raffaello.nordic.view.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.raffaello.nordic.R;
import com.raffaello.nordic.model.Ambient;
import com.raffaello.nordic.util.SwipeHelper;
import com.raffaello.nordic.view.adapter.AmbientsListAdapter;
import com.raffaello.nordic.viewmodel.AmbientsListViewModel;
import com.raffaello.nordic.viewmodel.AmbientsListViewModelFactory;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AmbientListFragment extends CommonFragment {

    private AmbientsListViewModel viewModel;
    private AmbientsListAdapter ambientListAdapter;

    @BindView(R.id.ambientsList)
    RecyclerView ambientsList;

    @BindView(R.id.ambientListError)
    TextView error;

    @BindView(R.id.ambientListEmpty)
    TextView listEmpty;

    @BindView(R.id.ambientListProgressBar)
    ProgressBar progressBar;

    @BindView(R.id.ambientListFTB)
    FloatingActionButton ftb;

    @BindView(R.id.ambientListSwipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    public AmbientListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ambient_list, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this,
                new AmbientsListViewModelFactory(getActivity().getApplication(), null))
                .get(AmbientsListViewModel.class);

        ambientListAdapter = new AmbientsListAdapter(new ArrayList<>());
        ambientsList.setLayoutManager(new LinearLayoutManager(getContext()));
        ambientsList.setAdapter(ambientListAdapter);

        swipeHelper.attachToRecyclerView(ambientsList);

        viewModel.refresh();


        swipeRefreshLayout.setOnRefreshListener(() -> {
            ambientsList.setVisibility(View.GONE);
            error.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            viewModel.forceRefresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        ftb.setOnClickListener(v -> {
            NavDirections action = AmbientListFragmentDirections.actionAdd(null);
            Navigation.findNavController(ftb).navigate(action);
        });

        observeAddAction();
        observeViewModel();
    }

    private final SwipeHelper swipeHelper = new SwipeHelper(getContext()) {

        private Ambient tempRemovedAmbient;
        private boolean delete = true;
        private boolean isDeleting = false;

        @Override
        public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
            underlayButtons.add(new SwipeHelper.UnderlayButton(
                    "Share",
                    0,
                    Color.parseColor("#FF9502"),
                    pos -> {
                        Ambient ambient = ambientListAdapter.getAmbient(pos);
                        Snackbar.make(swipeRefreshLayout, "Share key copied to clipboard", Snackbar.LENGTH_LONG).show();

                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("nordic", ambient.key);
                        clipboard.setPrimaryClip(clip);
                    }
            ));
            underlayButtons.add(new SwipeHelper.UnderlayButton(
                    "Delete",
                    0,
                    Color.parseColor("#FF3C30"),
                    pos -> {
                        if(!isDeleting) {
                            isDeleting = true;
                            Ambient ambient = ambientListAdapter.getAmbient(pos);
                            tempRemovedAmbient = ambient;
                            ambientListAdapter.removeItem(pos);
                            Snackbar snackbar = Snackbar.make(swipeRefreshLayout, "Ambient removed", Snackbar.LENGTH_LONG);

                            snackbar.setAction("UNDO", v -> {
                                ambientListAdapter.restoreItem(tempRemovedAmbient, pos);
                                delete = false;
                            });

                            snackbar.addCallback(new Snackbar.Callback() {

                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    if (delete)
                                        viewModel.deleteAmbient(pos);
                                    else
                                        delete = true;

                                    isDeleting = false;
                                }

                            });
                            snackbar.show();
                        }
                    }
            ));
        }
    };

    // Observers
    private void observeAddAction(){
        NavController navController = NavHostFragment.findNavController(this);
        MutableLiveData<Boolean> newAmbientAdded = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("newAmbientAdded");
        MutableLiveData<Boolean> newAmbientAddedFailed = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("newAmbientAddedFailed");
        MutableLiveData<Boolean> deviceIsOffline = navController.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("deviceIsOffline");
        newAmbientAdded.observe(getViewLifecycleOwner(), s -> {
            if (s) {
                Snackbar.make(swipeRefreshLayout, "Ambient added", Snackbar.LENGTH_SHORT).show();
                viewModel.forceRefresh();
                newAmbientAdded.setValue(false);
            }
        });
        newAmbientAddedFailed.observe(getViewLifecycleOwner(), s -> {
            if(s){
                Snackbar.make(swipeRefreshLayout, "Error. Check your data", Snackbar.LENGTH_SHORT).show();
                newAmbientAddedFailed.setValue(false);
            }
        });
        deviceIsOffline.observe(getViewLifecycleOwner(), s -> {
            if (s) {
                Snackbar.make(swipeRefreshLayout, "Server is offline", Snackbar.LENGTH_SHORT).show();
                deviceIsOffline.setValue(false);
            }
        });
    }

    private void observeViewModel() {

        viewModel.ambients.observe(getViewLifecycleOwner(), ambients -> {
            if (ambients instanceof List) {
                ambientsList.setVisibility(View.VISIBLE);
                ambientListAdapter.updateAmbientList(ambients);
            }
        });
        viewModel.loadError.observe(getViewLifecycleOwner(), bool -> {
            if (bool instanceof Boolean) {
                error.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.isLoading.observe(getViewLifecycleOwner(), bool -> {
            if (bool instanceof Boolean) {
                progressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
        viewModel.isEmpty.observe(getViewLifecycleOwner(), bool -> {
            if (bool instanceof Boolean) {
                listEmpty.setVisibility(bool ? View.VISIBLE : View.GONE);
            }
        });
    }


}