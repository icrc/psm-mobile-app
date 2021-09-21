package com.baosystems.icrc.psm.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivitySplashBinding;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.viewmodels.SplashViewModel;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        ActivitySplashBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_splash);

        binding.setLifecycleOwner(this);

        viewModel.isLoggedIn().observe(this, loggedIn -> {
            if (loggedIn) {
                Log.i("Splash", "User is logged in");
                ActivityManager.startActivity(this,
                        HomeActivity.getHomeActivityIntent(this), true);
            }
            else {
                Log.i("Splash", "User is not logged in");
                ActivityManager.startActivity(this,
                        LoginActivity.getLoginActivityIntent(this), true);
            }
        });
    }
}