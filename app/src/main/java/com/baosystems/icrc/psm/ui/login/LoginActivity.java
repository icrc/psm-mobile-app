package com.baosystems.icrc.psm.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivityLoginBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.sync.SyncActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.KeyboardUtils;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
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
        public void afterTextChanged(Editable editable) { loginViewModel.loginDataChanged(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginViewModel = (LoginViewModel) getViewModel();

        binding = (ActivityLoginBinding) getViewBinding();
        binding.setViewModel(loginViewModel);
        binding.setLifecycleOwner(this);

        // TODO: Ensure the user doesn't have to enter the server URL and username everytime.
        //  Save it in Preferences for reuse

        // TODO: Flag errors in URL field, if any
        binding.serverUrlTextField.addTextChangedListener(afterTextChangedListener);
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
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @NonNull
    @Override
    public ViewDataBinding createViewBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_login);
    }
}