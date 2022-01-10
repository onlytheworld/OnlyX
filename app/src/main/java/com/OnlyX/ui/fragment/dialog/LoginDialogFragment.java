package com.OnlyX.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.OnlyX.R;
import com.OnlyX.component.DialogCaller;

public class LoginDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    protected EditText mUserNameEditText;
    protected EditText mPassWdEditText;

    public static LoginDialogFragment newInstance(int title, int username, int passwd, String un, int requestCode) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        fragment.setArguments(createBundle(title, username, passwd, un, requestCode));
        return fragment;
    }

    protected static Bundle createBundle(int title, int username, int passwd, String un, int requestCode) {
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_CONTENT, passwd);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        if (un == null)
            bundle.putInt(DialogCaller.EXTRA_DIALOG_ITEMS, username);
        else
            bundle.putString(DialogCaller.EXTRA_DIALOG_ITEMS, un);
        return bundle;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_login, null);
        mUserNameEditText = view.findViewById(R.id.fragment_login_username);
        mUserNameEditText.setText(requireArguments().getString(DialogCaller.EXTRA_DIALOG_ITEMS));
        mPassWdEditText = view.findViewById(R.id.fragment_login_password);
        mPassWdEditText.setText(requireArguments().getString(DialogCaller.EXTRA_DIALOG_CONTENT));
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
        bundle.putString(DialogCaller.EXTRA_DIALOG_ITEMS, mUserNameEditText.getText().toString());
        bundle.putString(DialogCaller.EXTRA_DIALOG_CONTENT, mPassWdEditText.getText().toString());
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : requireActivity());
        target.onDialogResult(requestCode, bundle);
    }
}
