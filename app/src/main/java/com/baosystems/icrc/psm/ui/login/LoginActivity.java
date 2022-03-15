package com.baosystems.icrc.psm.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.data.OperationState;
import com.baosystems.icrc.psm.databinding.ActivityLoginBinding;
import com.baosystems.icrc.psm.ui.base.BaseActivity;
import com.baosystems.icrc.psm.ui.sync.SyncActivity;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.KeyboardUtils;
import com.baosystems.icrc.psm.utils.NetworkUtils;

import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

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

        binding.serverUrlTextview.setText(loginViewModel.getServerUrl());
        binding.usernameTextField.addTextChangedListener(afterTextChangedListener);
        binding.passwordTextField.addTextChangedListener(afterTextChangedListener);
        binding.signInButton.setOnClickListener(view -> login());
        binding.oidcBtn.setOnClickListener(view -> loginWithOpenId());

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

        observeOpenIdLoginActivity();
    }

    private void observeOpenIdLoginActivity() {
        loginViewModel.getOpenIdResult().observe(this, operationState -> {
            if (operationState == null) {
                return;
            }

            if (operationState.getClass() == OperationState.Error.class) {
                displayOpenIdAuthError(operationState);
                return;
            }

            if (operationState.getClass() == OperationState.Success.class) {
                launchOpenIdActivity(((OperationState.Success<IntentWithRequestCode>) operationState).getResult());
            }
        });
    }

    private void launchOpenIdActivity(IntentWithRequestCode result) {
        startActivityForResult(result.getIntent(), result.getRequestCode());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            loginViewModel.handleOpenIdAuthResponseData(data, requestCode);
        } else {
            Timber.e("The result code obtained from the auth activity is in error state " +
                    "(OpenID flow probably encountered an error or was cancelled). Result code = %d",
                    resultCode);

            displayError(binding.getRoot(), R.string.openid_authentication_not_successful);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displayOpenIdAuthError(OperationState<IntentWithRequestCode> state) {
        displayError(binding.getRoot(), ((OperationState.Error) state).getErrorStringRes());
    }


    private void navigateToSyncScreen() {
        ActivityManager.startActivity(
                this,
                SyncActivity.getSyncActivityIntent(this),
                true
        );
    }

    private void showLoginFailed(Integer errorRes) {
        KeyboardUtils.hideKeyboard(this);
        ActivityManager.showErrorMessage(binding.loginContainer, getString(errorRes));
    }

    private void login() {
        // TODO: Hide whatever component needs to be hidden,
        //  and show whichever one needs showing (e.g. progress bar)
        if (isConnectedToNetwork()) {
            loginViewModel.login();
        }
    }

    private boolean isConnectedToNetwork() {
        boolean networkIsAvailable = NetworkUtils.isOnline(this);
        if (!networkIsAvailable) {
            displayError(binding.getRoot(), R.string.no_network_available);
        }

        return networkIsAvailable;
    }

    private void loginWithOpenId() {
        if (isConnectedToNetwork()) {
            loginViewModel.openIdLogin();
        }
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