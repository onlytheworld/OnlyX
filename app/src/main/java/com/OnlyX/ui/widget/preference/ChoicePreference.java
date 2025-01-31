package com.OnlyX.ui.widget.preference;

import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.ui.fragment.BaseFragment;
import com.OnlyX.ui.fragment.dialog.ChoiceDialogFragment;
import com.OnlyX.ui.widget.Option;

/**
 * Created by Hiroshi on 2017/1/10.
 */

public class ChoicePreference extends Option implements View.OnClickListener {

    private final PreferenceManager mPreferenceManager;
    private FragmentManager mFragmentManager;
    private BaseFragment mTargetFragment;
    private String mPreferenceKey;
    private String[] mItems;
    private int mChoice;
    private int mRequestCode;

    public ChoicePreference(Context context) {
        this(context, null);
    }

    public ChoicePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChoicePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.custom_option, this);

        mPreferenceManager = App.getPreferenceManager();

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mFragmentManager != null) {
            ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.dialog_choice,
                    mItems, mChoice, mRequestCode);
            if (mTargetFragment != null) {
                fragment.setTargetFragment(mTargetFragment, 0);
            }
            fragment.show(mFragmentManager, null);
        }
    }

    public void bindPreference(FragmentManager manager, String key, int def, int item, int request) {
        bindPreference(manager, null, key, def, item, request);
    }

    public void bindPreference(FragmentManager manager, BaseFragment fragment, String key, int def, int item, int request) {
        mFragmentManager = manager;
        mTargetFragment = fragment;
        mPreferenceKey = key;
        mChoice = mPreferenceManager.getInt(key, def);
        mItems = getResources().getStringArray(item);
        mRequestCode = request;
        mSummaryView.setText(mItems[mChoice]);
    }

    public int getValue() {
        return mChoice;
    }

    public void setValue(int choice) {
        mPreferenceManager.putInt(mPreferenceKey, choice);
        mChoice = choice;
        mSummaryView.setText(mItems[mChoice]);
    }

}
