package com.softkoash.eazyaccounts;

import android.Manifest;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.softkoash.eazyaccounts.migration.MigrationListener;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.service.MigrationService;
import com.softkoash.eazyaccounts.util.FileUtil;
import com.softkoash.eazyaccounts.util.UiUtil;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    //request codes
    private final int REQUEST_FILE_CHOOSER = 1;

    //widgets
    private Button openFileButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
    }

    private void initWidgets() {
        this.openFileButton = (Button)findViewById(R.id.btnOpenFile);
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
        if (requestCode == REQUEST_FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            String filePath = FileUtil.getPath(this, selectedFile);
            MigrationService migrationService = new MigrationService(filePath);
            UiUtil.createProgressDialog(this);
            migrationService.executeDBMigration(this);
        }
    }
}
