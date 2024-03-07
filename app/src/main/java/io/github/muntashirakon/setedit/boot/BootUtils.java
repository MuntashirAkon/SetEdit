package io.github.muntashirakon.setedit.boot;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.muntashirakon.setedit.TableType;
import io.github.muntashirakon.setedit.utils.ActionResult;
import io.github.muntashirakon.setedit.utils.AndroidPropertyUtils;
import io.github.muntashirakon.setedit.utils.SettingsUtils;

public final class BootUtils {
    private static Set<BootItem> sBootItems;

    public static void add(@NonNull Context context, @NonNull BootItem bootItem) {
        getBootItems(context).remove(bootItem);
        getBootItems(context).add(bootItem);
        persistBootItems(context);
    }

    public static void delete(@NonNull Context context, @NonNull String table, @NonNull String name) {
        BootItem bootItem = new BootItem(ActionResult.TYPE_DELETE, table, name, null);
        getBootItems(context).remove(bootItem);
        persistBootItems(context);
    }

    @NonNull
    public static Set<BootItem> getBootItems(@NonNull Context context) {
        if (sBootItems != null) {
            return sBootItems;
        }
        sBootItems = Collections.synchronizedSet(new HashSet<>());
        File propertyFile = getBootItemsFile(context);
        if (!propertyFile.exists()) {
            return sBootItems;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(propertyFile)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                 sBootItems.add(BootItem.unflattenFromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sBootItems;
    }

    public static void persistBootItems(@NonNull Context context) {
        File propertyFile = getBootItemsFile(context);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertyFile)))) {
            for (BootItem bootItem : sBootItems) {
                writer.write(bootItem.flattenToString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean runBootActions(@NonNull Context context) {
        File logFile = getLogFile(context);
        Set<BootItem> bootItems = getBootItems(context);
        boolean isSuccess = true;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)))) {
            writer.write("==> Run at " + System.currentTimeMillis() + "ms\n");
            for (BootItem bootItem : bootItems) {
                try {
                    ActionResult result;
                    if (bootItem.table.equals(TableType.TABLE_PROPERTIES)) {
                        assert bootItem.action == ActionResult.TYPE_UPDATE && bootItem.value != null;
                        result = AndroidPropertyUtils.update(bootItem.name, bootItem.value);
                    } else {
                        switch (bootItem.action) {
                            case ActionResult.TYPE_UPDATE:
                                assert bootItem.value != null;
                                result = SettingsUtils.update(context, bootItem.table, bootItem.name, bootItem.value);
                                break;
                            case ActionResult.TYPE_CREATE:
                                assert bootItem.value != null;
                                result = SettingsUtils.create(context, bootItem.table, bootItem.name, bootItem.value);
                                break;
                            case ActionResult.TYPE_DELETE:
                                result = SettingsUtils.delete(context, bootItem.table, bootItem.name);
                                break;
                            default:
                                throw new UnsupportedOperationException("Invalid action " + bootItem.action);
                        }
                    }
                    if (result.successful) {
                        writer.write("Success! " + bootItem.flattenToString() + "\n");
                    } else {
                        isSuccess = false;
                        writer.write("Failed! " + result.getLogs() + " " + bootItem.flattenToString() + "\n");
                    }
                } catch (Throwable th) {
                    isSuccess = false;
                    writer.write("Failed! " + th.getMessage() + " " + bootItem.flattenToString() + "\n");
                }
            }
        } catch (IOException e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    @NonNull
    public static List<String> getLogs(@NonNull Context context) {
        File file = getLogFile(context);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @NonNull
    private static File getBootItemsFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "boot_items.tsv");
    }

    @NonNull
    private static File getLogFile(@NonNull Context context) {
        return new File(context.getCacheDir(), "boot.log");
    }
}
