package io.github.muntashirakon.setedit;

import androidx.annotation.Nullable;

public interface IEditorActivity {
    void setMessage(CharSequence charSequence);

    void displaySettingEditor(@Nullable String name, @Nullable String value);
}
