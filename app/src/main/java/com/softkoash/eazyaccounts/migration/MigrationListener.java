package com.softkoash.eazyaccounts.migration;

/**
 * Created by Deepak on 3/28/2017.
 */
public interface MigrationListener {
    void onSuccess(MigrationStats stats);

    void onFail(String message, Exception e);
}
