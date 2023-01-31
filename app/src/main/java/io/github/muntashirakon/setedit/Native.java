package io.github.muntashirakon.setedit;

import android.os.Build;

import androidx.annotation.Keep;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Native implements PropertyCallback, Comparator<String[]> {
    private final List<String[]> propertyList;

    static {
        try {
            System.loadLibrary("native-lib");
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private Native(List<String[]> list) {
        propertyList = list;
    }

    public static void setPropertyList(List<String[]> list) {
        try {
            Native nativeObj = new Native(list);
            if (Build.VERSION.SDK_INT >= 26) {
                readAndroidPropertiesPost26(nativeObj);
            } else {
                int i = 0;
                while (true) {
                    String[] property = new String[2];
                    if (!readAndroidPropertyPre26(i, property)) {
                        break;
                    }
                    list.add(property);
                    i++;
                }
            }
            Collections.sort(list, nativeObj);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    @Keep
    public static native void readAndroidPropertiesPost26(PropertyCallback propertyCallback);

    @Keep
    public static native boolean readAndroidPropertyPre26(int n, String[] property);

    @Override
    public int compare(String[] prop1, String[] prop2) {
        return prop1[0].compareToIgnoreCase(prop2[0]);
    }

    @Override
    public void handleProperty(String key, String value) {
        this.propertyList.add(new String[]{key, value});
    }
}
