package io.github.muntashirakon.setedit;

import androidx.annotation.Keep;

@Keep
public interface PropertyCallback {
    void handleProperty(String key, String value);
}
