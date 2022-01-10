package com.OnlyX.ui.activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.widget.AppCompatSpinner;

import com.OnlyX.R;
import com.OnlyX.component.DialogCaller;
import com.OnlyX.core.Manga;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Source;
import com.OnlyX.parser.Category;
import com.OnlyX.parser.Parser;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.ui.adapter.CategoryAdapter;
import com.OnlyX.ui.fragment.dialog.LoginDialogFragment;

import java.util.List;

import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/12/11.
 */

public class CategoryActivity extends BackActivity implements
        AdapterView.OnItemSelectedListener, DialogCaller {
    private final static int DIALOG_REQUEST_SOURCE = 0;

    @BindViews({R.id.category_spinner_subject, R.id.category_spinner_area,
            R.id.category_spinner_reader, R.id.category_spinner_year,
            R.id.category_spinner_progress, R.id.category_spinner_order})
    List<AppCompatSpinner> mSpinnerList;
    @BindViews({R.id.category_subject, R.id.category_area, R.id.category_reader,
            R.id.category_year, R.id.category_progress, R.id.category_order})
    List<View> mCategoryView;

    private Category mCategory;
    private int mSource;

    public static Intent createIntent(Context context, int source, String title) {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra(Extra.EXTRA_SOURCE, source);
        intent.putExtra(Extra.EXTRA_KEYWORD, title);
        return intent;
    }

    @Override
    protected void initView() {
        mSource = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        if (mToolbar != null) {
            mToolbar.setTitle(getIntent().getStringExtra(Extra.EXTRA_KEYWORD));
        }
        mCategory = SourceManager.getInstance(this).getParser(mSource).getCategory();
        initSpinner(mSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.comic_search:
                    Intent intent = new Intent(this, SearchActivity.class);
                    intent.putExtra(Extra.EXTRA_PICKER_PATH, "CategoryActivity");
                    intent.putExtra(Extra.EXTRA_SOURCE, mSource);
                    startActivity(intent);
                    break;
                case R.id.comic_login:
                    Source source = SourceManager.getInstance(this).load(mSource);
//                    String username = source.getUsername();
//                    String passwd = source.getPasswd();
                    LoginDialogFragment fragment =
                            LoginDialogFragment.newInstance(R.string.user_login_item,
                                    R.string.user_login_username,
                                    R.string.user_login_passwd,
                                    null,
                                    DIALOG_REQUEST_SOURCE);
                    fragment.show(getSupportFragmentManager(), null);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        if (requestCode == DIALOG_REQUEST_SOURCE) {
            String username = bundle.getString(EXTRA_DIALOG_ITEMS);
            String password = bundle.getString(EXTRA_DIALOG_CONTENT);
            Parser source = SourceManager.getInstance(this).getParser(mSource);
            Manga.getLogin(source, username, password, true);
//            SourceManager.getInstance(this).rememberLogin(username, password, mSource);
        }
    }


    private void initSpinner(int source) {
//        List<Pair<String, List<Pair<String, String>>>> cList = Manga.getCategoryList(SourceManager.getInstance(this).getParser(source));
//        for (int i = 0; i < Objects.requireNonNull(cList).size(); i++) {
//            mCategoryView.get(i).setVisibility(View.VISIBLE);
//            if (!mCategory.isComposite()) {
//                mSpinnerList.get(i).setOnItemSelectedListener(this);
//            }
//            mSpinnerList.get(i).setAdapter(new CategoryAdapter(this, cList.get(i).second));
//        }
        int[] type = new int[]{Category.CATEGORY_SUBJECT, Category.CATEGORY_AREA, Category.CATEGORY_READER,
                Category.CATEGORY_YEAR, Category.CATEGORY_PROGRESS, Category.CATEGORY_ORDER};
        for (int i = 0; i != type.length; ++i) {
            if (mCategory.hasAttribute(type[i])) {
                mCategoryView.get(i).setVisibility(View.VISIBLE);
                if (!mCategory.isComposite()) {
                    mSpinnerList.get(i).setOnItemSelectedListener(this);
                }
                mSpinnerList.get(i).setAdapter(new CategoryAdapter(this, mCategory.getAttrList(type[i])));
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        for (AppCompatSpinner spinner : mSpinnerList) {
            if (position == 0) {
                spinner.setEnabled(true);
            } else if (!parent.equals(spinner)) {
                spinner.setEnabled(false);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.category_action_button)
    void onActionButtonClick() {
        String[] args = new String[mSpinnerList.size()];
        for (int i = 0; i != args.length; ++i) {
            args[i] = getSpinnerValue(mSpinnerList.get(i));
        }
        int source = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        String format = mCategory.getFormat(args);
        Intent intent = ResultActivity.createIntent(this, format, source, ResultActivity.LAUNCH_MODE_CATEGORY);
        startActivity(intent);
    }

    private String getSpinnerValue(AppCompatSpinner spinner) {
        if (!spinner.isShown()) {
            return null;
        }
        return ((CategoryAdapter) spinner.getAdapter()).getValue(spinner.getSelectedItemPosition());
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.category);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_category;
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }
}
