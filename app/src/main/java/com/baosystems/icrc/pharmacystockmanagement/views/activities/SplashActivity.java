package com.baosystems.icrc.pharmacystockmanagement.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;

import com.baosystems.icrc.pharmacystockmanagement.R;
import com.baosystems.icrc.pharmacystockmanagement.databinding.ActivitySplashBinding;
import com.baosystems.icrc.pharmacystockmanagement.viewmodels.SplashViewModel;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        ActivitySplashBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_splash);

        binding.setLifecycleOwner(this);

        viewModel.isLoggedIn().observe(this, loggedIn -> {
            if (loggedIn)
                Log.i("Splash", "User is logged in");
            else
                Log.i("Splash", "User is not logged in");
        });
    }
}