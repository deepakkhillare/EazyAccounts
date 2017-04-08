package com.softkoash.eazyaccounts.migration;

/**
 * Created by Deepak on 4/9/2017.
 */
public class MigrationException extends Exception {

    public MigrationException(Exception e) {
        super("Unknown Error migrating data", e);
    }

    public MigrationException(String error, Exception e) {
        super(error, e);
    }
}
