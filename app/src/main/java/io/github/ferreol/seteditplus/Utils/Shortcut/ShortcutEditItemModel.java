package io.github.ferreol.seteditplus.Utils.Shortcut;

public class ShortcutEditItemModel {
    private final String Name;
    private final String Detail;

    public ShortcutEditItemModel(String name, String detail) {
        Name = name;
        Detail = detail;
    }

    public String getSettingsType() {
        return Name;
    }

    public String getDetail() {
        return Detail;
    }

}
