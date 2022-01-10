package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.misc.Switcher;
import com.OnlyX.model.Source;
import com.OnlyX.presenter.SearchPresenter;
import com.OnlyX.ui.adapter.AutoCompleteAdapter;
import com.OnlyX.ui.fragment.dialog.MultiDialogFragment;
import com.OnlyX.ui.view.SearchView;
import com.OnlyX.utils.CollectionUtils;
import com.OnlyX.utils.HintUtils;
import com.OnlyX.utils.StringUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchActivity extends BackActivity implements SearchView, TextView.OnEditorActionListener {

    private final static int DIALOG_REQUEST_SOURCE = 0;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.search_text_layout)
    TextInputLayout mInputLayout;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.search_keyword_input)
    AppCompatAutoCompleteTextView mEditText;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.search_action_button)
    FloatingActionButton mActionButton;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.search_strict_checkbox)
    AppCompatCheckBox mCheckBox;

    private ArrayAdapter<String> mArrayAdapter;

    private SearchPresenter mPresenter;
    private List<Switcher<Source>> mSourceList;
    private boolean mAutoComplete;

    @Override
    protected SearchPresenter initPresenter() {
        mPresenter = new SearchPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        mAutoComplete = mPreference.getBoolean(PreferenceManager.PREF_SEARCH_AUTO_COMPLETE, false);
        mEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (mActionButton != null && !mActionButton.isShown()) {
                mActionButton.show();
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mInputLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mAutoComplete) {
                    String keyword = mEditText.getText().toString();
                    if (!StringUtils.isEmpty(keyword)) {
                        mPresenter.loadAutoComplete(keyword);
                    }
                }
            }
        });
        mEditText.setOnEditorActionListener(this);
        if (mAutoComplete) {
            mArrayAdapter = new AutoCompleteAdapter(this);
            mEditText.setAdapter(mArrayAdapter);
        }
        mSourceList = new ArrayList<>();


        Intent intent = getIntent();
        if (!intent.getStringExtra(Extra.EXTRA_PICKER_PATH).equals("CategoryActivity")) {
            mPresenter.loadSource();
        } else {
            int source = intent.getIntExtra(Extra.EXTRA_SOURCE, -1);
            mSourceList.add(new Switcher<>(mPresenter.loadSource(source), true));
        }
        hideProgressBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search_menu_source) {
            if (!mSourceList.isEmpty()) {
                int size = mSourceList.size();
                String[] arr1 = new String[size];
                boolean[] arr2 = new boolean[size];
                for (int i = 0; i < size; ++i) {
                    arr1[i] = mSourceList.get(i).getElement().getTitle();
                    arr2[i] = mSourceList.get(i).isEnable();
                }
                MultiDialogFragment fragment =
                        MultiDialogFragment.newInstance(this, R.string.search_source_select, arr1, arr2, DIALOG_REQUEST_SOURCE);
                fragment.show(getSupportFragmentManager(), null);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        if (requestCode == DIALOG_REQUEST_SOURCE) {
            boolean[] check = bundle.getBooleanArray(EXTRA_DIALOG_RESULT_VALUE);
            if (check != null) {
                int size = mSourceList.size();
                for (int i = 0; i < size; ++i) {
                    mSourceList.get(i).setEnable(check[i + 1]);
                }
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mActionButton.performClick();
            return true;
        }
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.search_action_button)
    void onSearchButtonClick() {
        String keyword = mEditText.getText().toString();
        boolean strictSearch = mCheckBox.isChecked();
        if (StringUtils.isEmpty(keyword)) {
            mInputLayout.setError(getString(R.string.search_keyword_empty));
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            for (Switcher<Source> switcher : mSourceList) {
                if (switcher.isEnable()) {
                    list.add(switcher.getElement().getType());
                }
            }
            if (list.isEmpty()) {
                HintUtils.showToast(this, R.string.search_source_none);
            } else {
                startActivity(ResultActivity.createIntent(this, keyword, strictSearch,
                        CollectionUtils.unbox(list), ResultActivity.LAUNCH_MODE_SEARCH));
            }
        }
    }

    @Override
    public void onAutoCompleteLoadSuccess(List<String> list) {
        mArrayAdapter.clear();
        mArrayAdapter.addAll(list);
    }

    @Override
    public void onSourceLoadSuccess(@NonNull List<Source> list) {
        for (Source source : list) {
            mSourceList.add(new Switcher<>(source, true));
        }
    }

    @Override
    public void onSourceLoadFail() {
        hideProgressBar();
        HintUtils.showToast(this, R.string.search_source_load_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.comic_search);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_search;
    }

    @Override
    protected boolean isNavTranslation() {
        return true;
    }

}
