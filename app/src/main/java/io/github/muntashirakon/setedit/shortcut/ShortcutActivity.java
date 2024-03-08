package io.github.muntashirakon.setedit.shortcut;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.boot.ActionItem;
import io.github.muntashirakon.setedit.boot.BootUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;

public class ShortcutActivity extends AppCompatActivity {
    private TextView mTextView;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        setSupportActionBar(findViewById(R.id.toolbar));
        String[] flattenedActionItems = getIntent().getStringArrayExtra(ShortcutItem.EXTRA_LIST);
        if (flattenedActionItems == null) {
            finishAndRemoveTask();
            return;
        }
        mTextView = findViewById(R.id.txt);
        mExecutor.submit(() -> runActions(flattenedActionItems));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String[] flattenedActionItems = intent.getStringArrayExtra(ShortcutItem.EXTRA_LIST);
        if (flattenedActionItems != null) {
            mExecutor.submit(() -> runActions(flattenedActionItems));
        }
    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdownNow();
        super.onDestroy();
    }

    @WorkerThread
    private void runActions(@NonNull String[] flattenedActionItems) {
        runOnUiThread(() -> {
            mTextView.setText("");
            mTextView.append("Executing shortcut...\n");
        });
        for (String flattenedActionItem : flattenedActionItems) {
            try {
                ActionItem actionItem = ActionItem.unflattenFromString(flattenedActionItem);
                ActionResult result = BootUtils.executeAction(getApplicationContext(), actionItem);
                runOnUiThread(() -> {
                    if (result.successful) {
                        mTextView.append("Success! " + actionItem.flattenToString() + "\n");
                    } else {
                        mTextView.append("Failed! " + result.getLogs() + " " + actionItem.flattenToString() + "\n");
                    }
                });
            } catch (Throwable th) {
                runOnUiThread(() -> mTextView.append("Failed! " + th.getMessage() + " " + flattenedActionItem + "\n"));
            }
        }
    }
}
