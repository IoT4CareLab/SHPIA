package com.raffaello.nordic.view.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

// TODO: merge with ambientlistfragment

public class LevelsListFragment extends Fragment {

    private Ambient ambient;

    private AmbientsListViewModel viewModel;
    private AmbientsListAdapter ambientListAdapter;

    @BindView(R.id.levelsList)
    RecyclerView levelsList;

    @BindView(R.id.levelsListError)
    TextView error;

    @BindView(R.id.levelsListEmpty)
    TextView listEmpty;

    @BindView(R.id.levelsListProgressBar)
    ProgressBar progressBar;

    @BindView(R.id.levelsListFTB)
    FloatingActionButton ftb;

    @BindView(R.id.levelsListSwipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    public LevelsListFragment(Ambient ambient) {
        this.ambient = ambient;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_levels_list, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this,
                new AmbientsListViewModelFactory(getActivity().getApplication(), ambient))
                .get(AmbientsListViewModel.class);
        viewModel.refresh();

        ambientListAdapter = new AmbientsListAdapter(new ArrayList<>());
        levelsList.setLayoutManager(new LinearLayoutManager(getContext()));
        levelsList.setAdapter(ambientListAdapter);

        swipeHelper.attachToRecyclerView(levelsList);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            levelsList.setVisibility(View.GONE);
            error.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            viewModel.forceRefresh();
            swipeRefreshLayout.setRefreshing(false);
        });


        ftb.setOnClickListener(v -> {
            NavDirections action = AmbientDetailFragmentDirections.actionNestedAdd(ambient);
            Navigation.findNavController(ftb).navigate(action);
        });

        observeViewModel();
        observeAddAction();
    }

    private SwipeHelper swipeHelper = new SwipeHelper(getContext()) {

        private Ambient tempRemovedAmbient;
        private boolean delete = true;
        private boolean isDeleting = false;

        @Override
        public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
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
                            Snackbar snackbar = Snackbar.make(swipeRefreshLayout, "Ambiente rimosso", Snackbar.LENGTH_LONG);

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
                Snackbar.make(swipeRefreshLayout, "Ambiente aggiunto", Snackbar.LENGTH_SHORT).show();
                viewModel.forceRefresh();
                newAmbientAdded.setValue(false);
            }
        });
        newAmbientAddedFailed.observe(getViewLifecycleOwner(), s -> {
            if(s){
                Snackbar.make(swipeRefreshLayout, "Errore. Controlla i dati inseriti", Snackbar.LENGTH_SHORT).show();
                newAmbientAddedFailed.setValue(false);
            }
        });
        deviceIsOffline.observe(getViewLifecycleOwner(), s -> {
            if (s) {
                Snackbar.make(swipeRefreshLayout, "Il server è offline", Snackbar.LENGTH_SHORT).show();
                deviceIsOffline.setValue(false);
            }
        });
    }

    private void observeViewModel(){
        viewModel.ambients.observe(getViewLifecycleOwner(), ambients -> {
            if(ambients != null && ambients instanceof List){
                levelsList.setVisibility(View.VISIBLE);
                ambientListAdapter.updateAmbientList(ambients);
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

    public void refreshViewModel(){
        viewModel.forceRefresh();
    }
}