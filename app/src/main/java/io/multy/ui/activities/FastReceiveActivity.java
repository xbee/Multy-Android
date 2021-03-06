/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.MyReceiveFragment;
import io.multy.ui.fragments.receive.AmountChooserFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetRequestViewModel;

public class FastReceiveActivity extends BaseActivity {

    public static final int REQUEST_BLUETOOTH = 1;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2;

    private AssetRequestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fast_receive);
        viewModel = ViewModelProviders.of(this).get(AssetRequestViewModel.class);

        long id = getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID);
        double amount = getIntent().getExtras().getDouble(Constants.EXTRA_AMOUNT);

        viewModel.setWallet(RealmManager.getAssetsDao().getWalletById(id));
        viewModel.setAmount(amount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23 && checkPermissions()) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container_main);
            if (fragment == null) {
                start();
            }
        }
    }

    private void start() {
        getSupportFragmentManager().beginTransaction().add(R.id.container_main, new MyReceiveFragment()).commit();
    }

    public void showAmountChooser() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container_main, new AmountChooserFragment()).addToBackStack("amount").commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.i("wise", "permission not granted: " + permissions[i]);
                        finish();
                    }
                }
                Log.i("wise", "all permissions granted");
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                String message = getString(R.string.permissions_for_access_nearby_devices);
                showDialog(message,
                        (dialog, which) -> requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS));
                return false;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        }

        return true;
    }

    private void showDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), okListener)
                .setNegativeButton(getString(R.string.cancel), (dialog, whitch) -> onBackPressed())
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }
}
