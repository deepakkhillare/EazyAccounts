package com.softkoash.eazyaccounts;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.migration.service.MigrationService;
import com.softkoash.eazyaccounts.util.Constants;
import com.softkoash.eazyaccounts.util.DirectoryChooserDialog;
import com.softkoash.eazyaccounts.util.FileUtil;
import com.softkoash.eazyaccounts.util.UiUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    //request codes
    private final int REQUEST_FILE_CHOOSER = 1;
    private final int WRITE_PERMISSION_REQUEST_CODE = 999;
    private final int READ_PERMISSION_REQUEST_CODE = 998;

    //widgets
    private Button openFileButton = null;
    private EditText passwordText = null;
    private RadioGroup fileTypeRadio = null;
    private Button migrateButton = null;
    private EditText selectedFilePathText = null;
    private ListView lvMigrationResults = null;
    private ArrayAdapter<MigrationStats> migrationStatsArrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_main);
        this.openFileButton = (Button) findViewById(R.id.btnOpenFile);
        this.passwordText = (EditText) findViewById(R.id.tv_password);
        this.fileTypeRadio = (RadioGroup) findViewById(R.id.rgFileType);
        this.migrateButton = (Button) findViewById(R.id.btnMigrate);
        this.selectedFilePathText = (EditText) findViewById(R.id.tv_selected_file);
        this.lvMigrationResults = (ListView) findViewById(R.id.lvMigrationResults);
        migrationStatsArrayAdapter = new ArrayAdapter<MigrationStats>(MainActivity.this, R.layout.migration_result_layout, R.id.tvMigrationResult, new ArrayList<MigrationStats>());
        lvMigrationResults.setAdapter(migrationStatsArrayAdapter);
        this.selectedFilePathText.setText(getExternalMediaDirs()[0].getPath());

        this.openFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedFilePathText.setError(null);
                RadioButton selectedFileType = (RadioButton) findViewById(fileTypeRadio.getCheckedRadioButtonId());
                if ("File".equals(selectedFileType.getText())) {
                    openFileChooser();
                } else {
                    openDirectoryChooser();
                }
            }
        });

        this.migrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                migrationStatsArrayAdapter.clear();
                if (null != passwordText.getText() && passwordText.getText().toString().trim().isEmpty()) {
                    passwordText.setError(getString(R.string.invalid_password));
                } else if ((null != selectedFilePathText.getText() && selectedFilePathText.getText().toString().trim().isEmpty())
                        || (!FileUtil.hasFileExtension(selectedFilePathText.getText().toString(), ".db"))) {
                    selectedFilePathText.setError(getString(R.string.invalid_file_path));
                } else {
                    migrateSqliteToRealm(passwordText.getText().toString(), selectedFilePathText.getText().toString());
                }
            }
        });
    }

    private void openFileChooser() {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT), REQUEST_FILE_CHOOSER);
//        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select DB"), REQUEST_FILE_CHOOSER);
    }

    private void openDirectoryChooser() {
//        DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(MainActivity.this, new DirectoryChooserDialog.ChosenDirectoryListener() {
//            @Override
//            public void onChosenDir(String chosenDir) {
//                selectedFilePathText.setText(chosenDir);
////                writeToDirectory(Uri.fromFile(new File(chosenDir)), "testdir.txt");
//            }
//        });
//        directoryChooserDialog.setNewFolderEnabled(false);
//        directoryChooserDialog.chooseDirectory();
//        try {
//            StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
//            Method getVolumeListMethod = StorageManager.class.getDeclaredMethod("getVolumeList");
//            Object[] storageVolumeList = (Object[]) getVolumeListMethod.invoke(storageManager);
//            for (Object sv : storageVolumeList) {
//                Log.i(TAG, "sv: " + sv);
//            }
//        } catch(Exception e) {
//            Log.e(TAG, "StorageVolume Error", e);
//        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_FILE_CHOOSER);
    }

    private void migrateSqliteToRealm(String realmPassword, String selectedFilePath) {
        Intent serviceIntent = new Intent(MainActivity.this, MigrationService.class);
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
                        MigrationStats stats = (MigrationStats) resultData.getParcelable(Constants.BUNDLE_MIGRATION_STATS);
                        UiUtil.showDialog(MainActivity.this, "Migration for company: "+ stats.getCompanyName() + " completed successfully: " + stats);
                        migrationStatsArrayAdapter.add(stats);
                    } else {
                        UiUtil.showDialog(MainActivity.this, "Migration failed with error: " + resultData.getString(Constants.BUNDLE_ERROR_MESSAGE));
                    }
                }
            }
        });
        serviceIntent.putExtra("DB_FILE_PATH", selectedFilePath);
        serviceIntent.putExtra("REALM_PASSWORD", realmPassword);
        startService(serviceIntent);
        UiUtil.createProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri selectedFileUri = data.getData();
//            try {
//                StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
//                for(StorageVolume sv: storageManager.getStorageVolumes()) {
//                    Log.i(TAG, "sv: " + sv);
//                }
//                StorageVolume storageVolume = storageManager.getStorageVolume(new File(FileUtil.getFullPathFromTreeUri(selectedFileUri, this)));
//                Method accessIntentMethod = StorageVolume.class.getDeclaredMethod("createAccessIntent");
//                Intent intent = (Intent)accessIntentMethod.invoke(storageVolume, null);
//                Intent intent = storageVolume.createAccessIntent(null);
//                Method getVolumeListMethod = StorageManager.class.getDeclaredMethod("getVolumeList");
//                Object[] storageVolumeList = (Object[]) getVolumeListMethod.invoke(storageManager);
//                for (Object sv : storageVolumeList) {
//                    Log.i(TAG, "sv: " + sv);
//                }

//                startActivityForResult(intent, 999);
//            } catch(Exception e) {
//                Log.e(TAG, "Error with storage volume access", e);
//            } catch(Error e) {
//                Log.e(TAG, "Error with storage volume access", e);
//            }
////            FileUtil.getRealPathFromURI_API19(this, selectedFileUri);
//        } else if (requestCode == 999 && resultCode == RESULT_OK) {
            setSelectedFile(data);
        }
//        Uri treeUri = data.getData();
//        writeToDirectory(treeUri, "test.txt");
    }

    private void setSelectedFile(Intent data) {
        Uri selectedFileUri = data.getData();
        //            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, selectedFileUri);
        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        grantUriPermission(getPackageName(), selectedFileUri, takeFlags);
        getContentResolver().takePersistableUriPermission(selectedFileUri, takeFlags);
//            getContentResolver().takePersistableUriPermission(selectedFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
        writeToDirectory(selectedFileUri, "testfile.txt");
//        String selectedFilePath = selectedFileUri.getPath();//FileUtil.getPath(this, selectedFileUri);
        String selectedFilePath = FileUtil.getFullPathFromTreeUri(selectedFileUri, this);
        Log.i(TAG, "Can write: "+ new File(selectedFilePath).canWrite());
        selectedFilePathText.setText(selectedFilePath);
    }

    private void writeToDirectory(Uri treeUri, String filename) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
        grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
        DocumentFile file = pickedDir.createFile("text/plain", filename);
        try {
            OutputStream os = getContentResolver().openOutputStream(file.getUri());
            os.write("test".getBytes());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
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
                    initWidgets();
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
