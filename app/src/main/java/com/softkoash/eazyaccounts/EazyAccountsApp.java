package com.softkoash.eazyaccounts;

import android.app.Application;
import android.os.Environment;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.io.File;
import java.util.regex.Pattern;

import io.realm.Realm;

/** Created by Deepak on 3/27/2017.
 */
public class EazyAccountsApp extends Application {

    public static final String encryption = "softkoash0softkoash0softkoash0softkoash0softkoash0softkoash0soft";
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this)
//                                .withDefaultEncryptionKey(encryption.getBytes())
//                                .withFolder(new File(Environment.getExternalStorageDirectory()+"/softkoashdb/"))
//                                .withMetaTables()
                                .databaseNamePattern(Pattern.compile(".+\\.realm"))
                                .build())
                        .build());
    }
}
