package com.raffaello.nordic.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raffaello.nordic.R;
import com.raffaello.nordic.util.AuthManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginFragment extends Fragment {


    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;

    @BindView(R.id.loginUsername)
    EditText usernameView;

    @BindView(R.id.loginPassword)
    EditText passwordView;

    @BindView(R.id.loginButton)
    Button loginButton;

    @BindView(R.id.loginButton2)
    Button loginButton2;

    @BindView(R.id.loginProgressBar)
    ProgressBar progressBar;

    private AuthManager viewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(AuthManager.class);
        if(viewModel.checkAuthentication(getContext())){
            navigateToApp();
        }

        loginButton.setOnClickListener(v -> {
            String username = "pippo";
            String password = "pippo321";
            login(username, password);
        });

        loginButton2.setOnClickListener(v -> {
            String username = "mario";
            String password = "mario321";
            login(username, password);
        });

    }

    private void login(String username, String password) {

        viewModel.login(username, password, getContext());
        progressBar.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);

        viewModel.loginRequestSuccess.observe(getViewLifecycleOwner(), loginRequestSuccess -> {

            switch (loginRequestSuccess){
                case SUCCESS: {
                    navigateToApp();
                    break;
                }
                case LOGIN_FAILED: {
                    Log.i("messaggio", "Username or password invalid");
                    Toast.makeText(getContext(), "Username or password invalid", Toast.LENGTH_SHORT).show();
                    break;
                }
                case SERVER_ERROR: {
                    Log.i("messaggio", "Server is not available");
                    Toast.makeText(getContext(), "Server is not available", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            progressBar.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);

        });

    }

    private void navigateToApp(){
        NavDirections action = LoginFragmentDirections.loginAction();
        NavHostFragment.findNavController(this).navigate(action);
    }
}