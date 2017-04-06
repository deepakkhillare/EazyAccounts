package com.softkoash.eazyaccounts.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Nirav on 03-04-2017.
 */

public class UiUtil {

    private static ProgressDialog progressDialog;

    public static void showToast(Context context, String messages) {
        Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
    }

    public static void createProgressDialog(Context activityContext) {
        progressDialog = new ProgressDialog(activityContext);
        progressDialog.setMessage("Loading SQLLite data dump...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    public static void updateProgressDialog(int progressNumber) {
        progressDialog.setProgress(progressNumber);
    }

    public static void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
