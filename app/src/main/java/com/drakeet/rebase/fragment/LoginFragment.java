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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.drakeet.rebase.R;
import com.drakeet.rebase.Stores;
import com.drakeet.rebase.activity.AdminActivity;
import com.drakeet.rebase.api.RebaseRetrofit;
import com.drakeet.rebase.api.Retrofits;
import com.drakeet.rebase.api.type.Auth;
import com.drakeet.rebase.api.type.Login;
import com.drakeet.rebase.tool.BlackBoxes;
import com.drakeet.rebase.tool.Toasts;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

import static com.drakeet.rebase.tool.ErrorHandlers.displayErrorAction;
import static com.drakeet.rebase.tool.Strings.isNullOrEmpty;

/**
 * @author drakeet
 */
public class LoginFragment extends DialogFragment {

    @BindView(R.id.username) AutoCompleteTextView username;
    @BindView(R.id.password) EditText password;
    @BindView(android.R.id.progress) View progressBar;

    private Context context;


    public LoginFragment() {}


    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override public void onStart() {
        super.onStart();
        final AlertDialog dialog = (AlertDialog) getDialog();
        final Button login = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        login.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onLogin(dialog);
            }
        });
    }


    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
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
        Login login = new Login();
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
            .doOnNext(new Action1<Auth>() {
                @Override public void call(Auth auth) {
                    RebaseRetrofit.setAuth(auth);
                    final String token = auth.accessToken;
                    String encrypted = BlackBoxes.encrypt(context, token);
                    Stores.token(context).save(encrypted);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate(new Action0() {
                @Override public void call() {
                    showProgress(false);
                }
            })
            .subscribe(new Action1<Auth>() {
                @Override public void call(Auth auth) {
                    Toasts.showShort(R.string.login_successfully);
                    dialog.dismiss();
                    AdminActivity.start(context);
                }
            }, displayErrorAction(context));
    }


    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
}
