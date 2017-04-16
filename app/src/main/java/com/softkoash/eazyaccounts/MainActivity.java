package com.softkoash.eazyaccounts;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.migration.service.MigrationService;
import com.softkoash.eazyaccounts.util.Constants;
import com.softkoash.eazyaccounts.util.FileUtil;
import com.softkoash.eazyaccounts.util.UiUtil;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();
    private Context mContext;
    //request codes
    private final int REQUEST_FILE_CHOOSER = 1;
    private final int WRITE_PERMISSION_REQUEST_CODE = 999;
    private final int READ_PERMISSION_REQUEST_CODE = 998;
    //widgets
    private Button openFileButton = null;
    private String exportDBFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                initWidgets();
            } else {
                requestPermission(); // Code for permission
            }
        } else {
            initWidgets();
        }

    }

    private void initWidgets() {
        this.openFileButton = (Button) findViewById(R.id.btnOpenFile);
        this.openFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select DB"), REQUEST_FILE_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mContext = this.getApplication().getApplicationContext();
        if (requestCode == REQUEST_FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            String filePath = FileUtil.getPath(this, selectedFile);
            Intent serviceIntent = new Intent(mContext, MigrationService.class);
            serviceIntent.putExtra("receiver", new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    super.onReceiveResult(resultCode, resultData);
                    if (resultCode == Constants.RESULT_PROGRESS_UPDATE) {
                        UiUtil.updateProgressDialog(resultData.getString(Constants.BUNDLE_PROGRESS_MESSAGE),
                                resultData.getInt(Constants.BUNDLE_PROGRESS_PERCENT));
                    } else {
                        UiUtil.dismissProgressDialog();
                        if (resultCode == Constants.RESULT_SUCCESS) {
                            MigrationStats stats = (MigrationStats)resultData.getParcelable(Constants.BUNDLE_MIGRATION_STATS);
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Migration completed successfully: " + stats)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Migration failed with error: " + resultData.getString(Constants.BUNDLE_ERROR_MESSAGE))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                }
            });
            serviceIntent.putExtra("DB_FILE_PATH", filePath);
            mContext.startService(serviceIntent);
            UiUtil.createProgressDialog(this);
        }
    }

    private boolean checkPermission() {
        int writePermissionResult = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionResult = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if ((writePermissionResult == PackageManager.PERMISSION_GRANTED) && (readPermissionResult == PackageManager.PERMISSION_GRANTED)) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST_CODE);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Read External Storage permission allows us to do read images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive for write.");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive for write.");
                }
                break;
            case READ_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive for read.");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive for read.");
                }
                break;
        }
    }
}
