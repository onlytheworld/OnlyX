package com.OnlyX.ui.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.OnlyX.R;
import com.OnlyX.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class MultiDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnMultiChoiceClickListener {

    private boolean[] mCheckArray;

    @NonNull
    public static MultiDialogFragment newInstance(Context cont, int title, String[] item, boolean[] check, int requestCode) {
        MultiDialogFragment fragment = new MultiDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        String[] itemList = new String[item.length + 1];
        itemList[0] = cont.getString(R.string.search_select_all);
        System.arraycopy(item, 0, itemList, 1, item.length);
        bundle.putStringArray(DialogCaller.EXTRA_DIALOG_ITEMS, itemList);
        boolean[] checkList = new boolean[check.length + 1];
        checkList[0] = true;
        System.arraycopy(check, 0, checkList, 1, check.length);
        bundle.putBooleanArray(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS, checkList);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        String[] item = requireArguments().getStringArray(DialogCaller.EXTRA_DIALOG_ITEMS);
        if (item == null) {
            item = new String[0];
        }
        initCheckArray(item.length);
        builder.setTitle(requireArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setMultiChoiceItems(item, mCheckArray, this)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which, boolean check) {
        if (which == 0) {
            ListView lv = ((AlertDialog) requireDialog()).getListView();
            for (int i = 1; i < mCheckArray.length; i++) {
                mCheckArray[i] = check;
                lv.setItemChecked(i, check);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = requireArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putBooleanArray(DialogCaller.EXTRA_DIALOG_RESULT_VALUE, mCheckArray);
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : requireActivity());
        target.onDialogResult(requestCode, bundle);
    }

    private void initCheckArray(int length) {
        mCheckArray = requireArguments().getBooleanArray(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS);
        if (mCheckArray == null) {
            mCheckArray = new boolean[length];
            for (int i = 0; i != length; ++i) {
                mCheckArray[i] = false;
            }
        }
    }

}
