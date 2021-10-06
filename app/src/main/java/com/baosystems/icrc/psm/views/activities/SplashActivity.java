package com.baosystems.icrc.psm.views.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivitySplashBinding;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.viewmodels.SplashViewModel;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        ActivitySplashBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_splash);

        binding.setLifecycleOwner(this);

        viewModel.isLoggedIn().observe(this, loggedIn -> {
            if (loggedIn) {
                Log.i(TAG, "User is logged in");
                ActivityManager.startActivity(this,
                        HomeActivity.getHomeActivityIntent(this), true);
            }
            else {
                Log.i(TAG, "User is not logged in");
                ActivityManager.startActivity(this,
                        LoginActivity.getLoginActivityIntent(this), true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        viewModel.cleanUp();
        super.onDestroy();
    }
}