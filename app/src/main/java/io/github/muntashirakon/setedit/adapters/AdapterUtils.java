package io.github.muntashirakon.setedit.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.muntashirakon.setedit.R;

public class AdapterUtils {
    public static View inflateSetting(Context context, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_setting, viewGroup, false);
    }

    public static String getName(View view) {
        return ((TextView) view.findViewById(R.id.txtName)).getText().toString();
    }

    @SuppressLint("SetTextI18n")
    public static void setNameValue(View view, String name, String value) {
        ((TextView) view.findViewById(R.id.txtName)).setText(name);
        ((TextView) view.findViewById(R.id.txtValue)).setText(value);
    }

    public static String getValue(View view) {
        return ((TextView) view.findViewById(R.id.txtValue)).getText().toString();
    }
}
