package io.github.muntashirakon.setedit.Utils.Shortcut;

public class ShortcutEditItemModel {
    private final String shortcutName;
    private final String shortcutContent;

    public ShortcutEditItemModel(String name, String detail) {
        this.shortcutName = name;
        this.shortcutContent = detail;
    }

    public String getSettingsType() {
        return shortcutName;
    }

    public String getShortcutContent() {
        return shortcutContent;
    }

}
