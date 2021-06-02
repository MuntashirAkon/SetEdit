package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import io.github.muntashirakon.setedit.R;

public class AdapterUtils {
    public static View inflateSetting(Context context, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_setting, viewGroup, false);
    }

    @NonNull
    public static String getName(@NonNull View view) {
        return ((TextView) view.findViewById(R.id.txtName)).getText().toString();
    }

    public static void setNameValue(View view, String name, String value) {
        ((TextView) view.findViewById(R.id.txtName)).setText(name);
        ((TextView) view.findViewById(R.id.txtValue)).setText(value);
    }

    @NonNull
    public static String getValue(@NonNull View view) {
        return ((TextView) view.findViewById(R.id.txtValue)).getText().toString();
    }
}
