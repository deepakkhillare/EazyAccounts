package com.softkoash.eazyaccounts.util;

import java.text.SimpleDateFormat;

/**
 * Created by Deepak on 4/10/2017.
 */
public interface Constants {
    int RESULT_PROGRESS_UPDATE = 101;
    int RESULT_SUCCESS = 102;
    int RESULT_ERROR = 103;

    String BUNDLE_PROGRESS_PERCENT = "progressPercent";
    String BUNDLE_PROGRESS_MESSAGE = "progressMessage";
    String BUNDLE_MIGRATION_STATS = "migrationStats";
    String BUNDLE_ERROR_MESSAGE = "errorMessage";

    SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
    SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
}
