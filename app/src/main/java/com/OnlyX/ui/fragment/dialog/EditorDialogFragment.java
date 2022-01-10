package com.OnlyX.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import androidx.appcompat.app.AlertDialog;

import com.OnlyX.R;
import com.OnlyX.component.DialogCaller;


/**
 * Created by Hiroshi on 2016/10/15.
 */

public class EditorDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    protected EditText mEditText;

    public static EditorDialogFragment newInstance(int title, String content, int requestCode) {
        EditorDialogFragment fragment = new EditorDialogFragment();
        fragment.setArguments(createBundle(title, content, requestCode));
        return fragment;
    }

    protected static Bundle createBundle(int title, String content, int requestCode) {
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putString(DialogCaller.EXTRA_DIALOG_CONTENT, content);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        return bundle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_editor, null);
        mEditText = view.findViewById(R.id.dialog_editor_text);
        mEditText.setText(requireArguments().getString(DialogCaller.EXTRA_DIALOG_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(requireArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setView(view)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = requireArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putString(DialogCaller.EXTRA_DIALOG_RESULT_VALUE, mEditText.getText().toString());
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : requireActivity());
        target.onDialogResult(requestCode, bundle);
    }

}
