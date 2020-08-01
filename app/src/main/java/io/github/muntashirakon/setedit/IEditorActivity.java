package io.github.muntashirakon.setedit;

public interface IEditorActivity {
    void setMessage(CharSequence charSequence);

    void displaySettingEditor(String name, String value);

    void displayNewSettingEditor();
}
