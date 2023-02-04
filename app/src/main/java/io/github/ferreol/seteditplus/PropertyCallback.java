package io.github.ferreol.seteditplus;

import androidx.annotation.Keep;

@Keep
public interface PropertyCallback {
    void handleProperty(String key, String value);
}
