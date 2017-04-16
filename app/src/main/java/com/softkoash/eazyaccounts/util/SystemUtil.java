package com.softkoash.eazyaccounts.util;

import android.os.Build;

/**
 * Created by Deepak on 4/9/2017.
 */
public class SystemUtil {

    public static String getDeviceId() {
        String UniqueDeviceID = convertStringToNumber(Build.SERIAL);
        if (UniqueDeviceID.length() < 15) {
            UniqueDeviceID += convertStringToNumber(Build.ID);
        }
        if (UniqueDeviceID.length() < 15) {
            UniqueDeviceID += convertStringToNumber(Build.HARDWARE);
        }
        if (UniqueDeviceID.length() > 15) {
            UniqueDeviceID = UniqueDeviceID.substring(0, 15);
        }
        return UniqueDeviceID;
    }

    private static String convertStringToNumber(String StringToConvert) {
        String result = "";
        char[] buffer = StringToConvert.toCharArray();
        for (int i = buffer.length - 1; i > -1; i--) {
            result += (byte) buffer[i];
        }
        if (result.length() > 15) {
            return result.substring(0, 15);
        } else
            return result;
    }
}
