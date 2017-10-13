package com.openproject.xiaojie.android_nfc.nfc;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * 单位转换类
 * Created by xxj on 06/23.
 */
public final class Utils {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public static void init(Context context) {
        Utils.context = context;
    }

    public static Context getContext() {
        return context;
    }
}
