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
    private static Set<ActionItem> sActionItems;

    public static void add(@NonNull Context context, @NonNull ActionItem actionItem) {
        getBootItems(context).remove(actionItem);
        getBootItems(context).add(actionItem);
        persistBootItems(context);
    }

    public static void delete(@NonNull Context context, @NonNull String table, @NonNull String name) {
        ActionItem actionItem = new ActionItem(ActionResult.TYPE_DELETE, table, name, null);
        getBootItems(context).remove(actionItem);
        persistBootItems(context);
    }

    @NonNull
    public static Set<ActionItem> getBootItems(@NonNull Context context) {
        if (sActionItems != null) {
            return sActionItems;
        }
        sActionItems = Collections.synchronizedSet(new HashSet<>());
        File propertyFile = getBootItemsFile(context);
        if (!propertyFile.exists()) {
            return sActionItems;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(propertyFile)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                 sActionItems.add(ActionItem.unflattenFromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sActionItems;
    }

    public static void persistBootItems(@NonNull Context context) {
        File propertyFile = getBootItemsFile(context);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertyFile)))) {
            for (ActionItem actionItem : sActionItems) {
                writer.write(actionItem.flattenToString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean runBootActions(@NonNull Context context) {
        File logFile = getLogFile(context);
        Set<ActionItem> actionItems = getBootItems(context);
        boolean isSuccess = true;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)))) {
            writer.write("==> Run at " + System.currentTimeMillis() + "ms\n");
            for (ActionItem actionItem : actionItems) {
                try {
                    ActionResult result = executeAction(context, actionItem);
                    if (result.successful) {
                        writer.write("Success! " + actionItem.flattenToString() + "\n");
                    } else {
                        isSuccess = false;
                        writer.write("Failed! " + result.getLogs() + " " + actionItem.flattenToString() + "\n");
                    }
                } catch (Throwable th) {
                    isSuccess = false;
                    writer.write("Failed! " + th.getMessage() + " " + actionItem.flattenToString() + "\n");
                }
            }
        } catch (IOException e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static ActionResult executeAction(@NonNull Context context, @NonNull ActionItem actionItem)
            throws UnsupportedOperationException, AssertionError {
        if (actionItem.table.equals(TableType.TABLE_PROPERTIES)) {
            assert actionItem.action == ActionResult.TYPE_UPDATE && actionItem.value != null;
            return AndroidPropertyUtils.update(actionItem.name, actionItem.value);
        } else {
            switch (actionItem.action) {
                case ActionResult.TYPE_UPDATE:
                    assert actionItem.value != null;
                    return SettingsUtils.update(context, actionItem.table, actionItem.name, actionItem.value);
                case ActionResult.TYPE_CREATE:
                    assert actionItem.value != null;
                    return SettingsUtils.create(context, actionItem.table, actionItem.name, actionItem.value);
                case ActionResult.TYPE_DELETE:
                    return SettingsUtils.delete(context, actionItem.table, actionItem.name);
                default:
                    throw new UnsupportedOperationException("Invalid action " + actionItem.action);
            }
        }
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
