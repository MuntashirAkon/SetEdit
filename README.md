# SetEdit

The open source version of **4A Settings Database Editor**.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80" />](https://f-droid.org/packages/io.github.muntashirakon.setedit)

**WARNING** I do not guarantee that problems caused by the improper use of this utility can be fixed, and we are unable to help you with any such problems. We support only this app, not your device's system software. Settings Database Editor (SetEdit) is invaluable if you need it, but if you're not careful you're very likely to mess something up.

By default, for your protection, Android prevents you from modifying the **SECURE** and **GLOBAL** tables. If you have Android Jellybean or later, you can remove this protection from an ADB shell using the following command
```shell
pm grant io.github.muntashirakon.setedit android.permission.WRITE_SECURE_SETTINGS
```
On earlier versions, you can only remove this protection on a rooted device by installing SetEdit to the system partition.

Both `WRITE_SETTINGS` and `WRITE_SECURE_SETTINGS` permissions are optional if you do not need to edit any items.

### License

GNU General Public License v3.0

### Credits

[SetEdit by 4A](https://play.google.com/store/apps/details?id=by4a.setedit22)
