package com.softkoash.eazyaccounts.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class UiUtil {

    private static ProgressDialog progressDialog;

    public static void showToast(Context context, String messages) {
        Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
    }

    public static void createProgressDialog(Context activityContext) {
        progressDialog = new ProgressDialog(activityContext);
        progressDialog.setMessage("Loading SQLLite data dump...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void updateProgressDialog(String message, int progressNumber) {
        progressDialog.setMessage(message);
        progressDialog.setProgress(progressNumber);
    }

    public static void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public static void showDialog(Context activityContext, String message) {
        new AlertDialog.Builder(activityContext)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
