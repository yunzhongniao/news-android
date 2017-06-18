/*
 * Copyright (C) 2017 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of rebase-android
 *
 * rebase-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rebase-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rebase-android. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drakeet.rebase.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import bz.tsung.android.objectify.NoSuchPreferenceFoundException;
import com.drakeet.rebase.R;
import com.drakeet.rebase.Stores;
import com.drakeet.rebase.activity.AdminActivity;
import com.drakeet.rebase.api.RebaseRetrofit;
import com.drakeet.rebase.api.Retrofits;
import com.drakeet.rebase.api.type.Auth;
import com.drakeet.rebase.api.type.Login;
import com.drakeet.rebase.tool.BlackBoxes;
import com.drakeet.rebase.tool.Toasts;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.drakeet.rebase.tool.Strings.isNullOrEmpty;

/**
 * @author drakeet
 */
public class LoginFragment extends DialogFragment {

    private static final String TAG = LoginFragment.class.getSimpleName();

    @BindView(R.id.username) AutoCompleteTextView username;
    @BindView(R.id.password) EditText password;
    @BindView(android.R.id.progress) View progressBar;

    private Context context;


    public LoginFragment() {}


    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog) getDialog();
        final Button loginButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin(dialog);
            }
        });

        try {
            Login login = Stores.login(context).load();
            username.setText(login.username);
            password.setText(BlackBoxes.decrypt(context, login.password));
            onLogin(getDialog());
        } catch (NoSuchPreferenceFoundException e) {
            Log.e(TAG, "NoSuchPreferenceFoundException: login");
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getContext();
        return new AlertDialog.Builder(context)
            .setView(onCreateDialogContentView(savedInstanceState))
            .setTitle(R.string.title_login_in)
            .setPositiveButton(R.string.action_login, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }


    @SuppressLint("InflateParams")
    private View onCreateDialogContentView(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.view_login, null);
        ButterKnife.bind(this, root);
        return root;
    }


    private void onLogin(final DialogInterface dialog) {
        final Login login = new Login();
        login.username = username.getText().toString();
        login.password = password.getText().toString();
        EditText focusView = null;
        if (isNullOrEmpty(login.password)) {
            focusView = password;
        }
        if (isNullOrEmpty(login.username)) {
            focusView = username;
        }
        if (focusView != null) {
            showProgress(false);
            focusView.requestFocus();
            focusView.setError(getString(R.string.error_field_required));
            return;
        }

        showProgress(true);
        Retrofits.rebase().login(login.username, login.password)
            .doOnNext(new Consumer<Auth>() {
                @Override
                public void accept(@NonNull Auth auth) {
                    RebaseRetrofit.setAuth(auth);
                    saveLogin(login);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(new Action() {
                @Override
                public void run() {
                    showProgress(false);
                }
            })
            .subscribe(new Consumer<Auth>() {
                @Override
                public void accept(@NonNull Auth auth) {
                    Toasts.showShort(R.string.login_successfully);
                    dialog.dismiss();
                    AdminActivity.start(context);
                }
            });
    }


    private void saveLogin(Login login) {
        String encrypted = BlackBoxes.encrypt(context, login.password);
        Login encryptedLogin = new Login(login.username, encrypted);
        Stores.login(context).save(encryptedLogin);
    }


    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
