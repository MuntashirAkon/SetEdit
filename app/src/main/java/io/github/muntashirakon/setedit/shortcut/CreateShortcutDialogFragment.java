package io.github.muntashirakon.setedit.shortcut;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import io.github.muntashirakon.lifecycle.SoftInputLifeCycleObserver;
import io.github.muntashirakon.setedit.R;
import io.github.muntashirakon.setedit.boot.ActionItem;

public class CreateShortcutDialogFragment extends DialogFragment {
    public static final String TAG = CreateShortcutDialogFragment.class.getSimpleName();

    private static final String ARG_ACTION_ITEM = "act";

    @NonNull
    public static CreateShortcutDialogFragment getInstance(@NonNull ActionItem actionItem) {
        CreateShortcutDialogFragment dialog = new CreateShortcutDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACTION_ITEM, actionItem.flattenToString());
        dialog.setArguments(args);
        return dialog;
    }

    private boolean mValidName = true;
    private ActionItem mActionItem;
    private View mDialogView;
    private TextInputEditText mShortcutNameField;
    private TextInputEditText mShortcutIconField;
    private TextInputLayout mShortcutIconLayout;
    private ShapeableImageView mShortcutIconPreview;
    private MaterialTextView mShortcutNamePreview;
    @Nullable
    private String mShortcutName;
    public ActivityResultLauncher<Intent> mOpenIconPikerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            Drawable drawable = getDrawable(uri);
                            mShortcutIconField.setText(uri.toString());
                            mShortcutIconPreview.setImageDrawable(drawable);
                        }
                    }
                }
            });

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mActionItem = ActionItem.unflattenFromString(requireArguments().getString(ARG_ACTION_ITEM));
        mDialogView = View.inflate(requireActivity(), R.layout.dialog_create_shortcut, null);
        mShortcutNameField = mDialogView.findViewById(R.id.shortcut_name);
        mShortcutIconField = mDialogView.findViewById(R.id.insert_icon);
        mShortcutIconLayout = (TextInputLayout) mShortcutIconField.getParent().getParent();
        mShortcutIconPreview = mDialogView.findViewById(R.id.icon);
        mShortcutNamePreview = mDialogView.findViewById(R.id.name);

        mShortcutNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    mValidName = true;
                    mShortcutName = s.toString();
                    mShortcutNamePreview.setText(s);
                } else mValidName = false;
            }
        });
        mShortcutIconField.setKeyListener(null);
        mShortcutIconLayout.setEndIconOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.pick_icon));
            mOpenIconPikerResultLauncher.launch(chooserIntent);
        });
        mShortcutNameField.setText(mActionItem.name);
        mShortcutNamePreview.setText(mActionItem.name);

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.create_shortcut)
                .setView(mDialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (mValidName) {
                        String name = mShortcutName != null ? mShortcutName : mActionItem.name;
                        CharSequence iconUri = mShortcutIconField.getText();
                        IconCompat icon = !TextUtils.isEmpty(iconUri)
                                ? IconCompat.createWithContentUri(iconUri.toString())
                                : IconCompat.createWithResource(requireContext(), R.drawable.ic_launcher_foreground);
                        ShortcutItem shortcutItem = new ShortcutItem(name, icon);
                        shortcutItem.addActionItem(mActionItem);
                        ShortcutUtils.createShortcut(requireContext(), shortcutItem);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mDialogView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getLifecycle().addObserver(new SoftInputLifeCycleObserver(new WeakReference<>(mShortcutNameField)));
    }


    @Nullable
    private Drawable getDrawable(@Nullable Uri uri) {
        if (uri == null) {
            return null;
        }
        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
            Drawable drawable = Drawable.createFromStream(is, uri.toString());
            if (drawable != null) {
                return drawable;
            }
        } catch (IOException ignore) {
        }
        return null;
    }
}