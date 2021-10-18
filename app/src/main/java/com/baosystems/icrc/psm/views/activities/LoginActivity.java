package com.baosystems.icrc.psm.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivityLoginBinding;
import com.baosystems.icrc.psm.service.PreferenceProvider;
import com.baosystems.icrc.psm.service.SecurePreferenceProviderImpl;
import com.baosystems.icrc.psm.service.UserManager;
import com.baosystems.icrc.psm.service.UserManagerImpl;
import com.baosystems.icrc.psm.service.scheduler.BaseSchedulerProvider;
import com.baosystems.icrc.psm.service.scheduler.SchedulerProviderImpl;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.KeyboardUtils;
import com.baosystems.icrc.psm.utils.Sdk;
import com.baosystems.icrc.psm.viewmodels.login.LoginViewModel;
import com.baosystems.icrc.psm.viewmodels.login.LoginViewModelFactory;

import io.reactivex.disposables.CompositeDisposable;

public class LoginActivity extends BaseActivity {
    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    // TODO: See if the view model and the two-way binding can handle
    //  this alone without using a textwatcher or creating an infinite loop
    private final TextWatcher afterTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence,
                                      int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable editable) {
            Log.d("LoginActivity", "Text changed");
            loginViewModel.loginDataChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginViewModel = (LoginViewModel) getViewModel();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(loginViewModel);
        binding.setLifecycleOwner(this);

        // TODO: Ensure the user doesn't have to enter the server URL and username everytime.
        //  Save it in Preferences for reuse

        // TODO: Flag errors in URL field, if any
        binding.serverURLTextField.addTextChangedListener(afterTextChangedListener);
        binding.usernameTextField.addTextChangedListener(afterTextChangedListener);
        binding.passwordTextField.addTextChangedListener(afterTextChangedListener);
        binding.signInButton.setOnClickListener(view -> login());

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null)
                return;

            // TODO: Hide progress bar, if required

            if(loginResult.getError() != null) {
                // TODO: Show login error
                showLoginFailed(loginResult.getError());
            }

            if (loginResult.getUser() != null) {
                navigateToSyncScreen();
            }

            setResult(Activity.RESULT_OK);
        });
    }


    private void navigateToSyncScreen() {
        ActivityManager.startActivity(
                this,
                SyncActivity.getSyncActivityIntent(this),
                true
        );
    }

    private void showLoginFailed(String error) {
        KeyboardUtils.hideKeyboard(this);
        ActivityManager.showErrorMessage(binding.loginContainer, error);
    }

    private void login() {
        // TODO: Hide whatever component needs to be hidden,
        //  and show whichever one needs showing (e.g. progress bar)

        loginViewModel.login();
    }

    public static Intent getLoginActivityIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @NonNull
    @Override
    public ViewModel createViewModel(@NonNull CompositeDisposable disposable) {
        // TODO: Inject D2
        // TODO: Inject UserManager using DI
        UserManager userManager = new UserManagerImpl(Sdk.d2(this));

        // TODO: Inject SchedulerProvider using DI
        BaseSchedulerProvider schedulerProvider = new SchedulerProviderImpl();

        // TODO: Inject PreferenceProvider using DI
        PreferenceProvider preferenceProvider =
                new SecurePreferenceProviderImpl(getApplication());

        return new ViewModelProvider(this, new LoginViewModelFactory(
                getApplication(),
                schedulerProvider,
                preferenceProvider,
                userManager
        )).get(LoginViewModel.class);
    }
}