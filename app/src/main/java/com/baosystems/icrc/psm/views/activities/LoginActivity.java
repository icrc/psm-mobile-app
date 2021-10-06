package com.baosystems.icrc.psm.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.baosystems.icrc.psm.R;
import com.baosystems.icrc.psm.databinding.ActivityLoginBinding;
import com.baosystems.icrc.psm.utils.ActivityManager;
import com.baosystems.icrc.psm.utils.KeyboardUtils;
import com.baosystems.icrc.psm.viewmodels.LoginModel;

public class LoginActivity extends AppCompatActivity {
    private LoginModel loginModel;
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
            loginModel.loginDataChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginModel = new ViewModelProvider(this).get(LoginModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setViewModel(loginModel);
        binding.setLifecycleOwner(this);

        // TODO: Ensure the user doesn't have to enter the server URL and username everytime.
        //  Save it in Preferences for reuse

        // TODO: Flag errors in URL field, if any
        binding.serverURLTextField.addTextChangedListener(afterTextChangedListener);
        binding.usernameTextField.addTextChangedListener(afterTextChangedListener);
        binding.passwordTextField.addTextChangedListener(afterTextChangedListener);
        binding.signInButton.setOnClickListener(view -> login());

        loginModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null)
                return;

            // TODO: Hide progress bar, if required

            if(loginResult.getError() != null) {
                // TODO: Show login error
                showLoginFailed(loginResult.getError());
            }

            if (loginResult.getUser() != null) {
                navigateToHomeScreen();
            }

            setResult(Activity.RESULT_OK);
        });
    }

    private void navigateToHomeScreen() {
        ActivityManager.startActivity(this,
                HomeActivity.getHomeActivityIntent(this), true);
    }

    private void showLoginFailed(String error) {
        KeyboardUtils.hideKeyboard(this);
        ActivityManager.showErrorMessage(binding.loginContainer, error);
    }

    private void login() {
        // TODO: Hide whatever component needs to be hidden,
        //  and show whichever one needs showing (e.g. progress bar)

        loginModel.login();
    }

    public static Intent getLoginActivityIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginModel.cleanUp();
    }
}