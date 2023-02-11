package io.github.ferreol.seteditplus.Utils;

public class ShortcutEditItemModel {
    private String Name;
    private String Detail;

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
