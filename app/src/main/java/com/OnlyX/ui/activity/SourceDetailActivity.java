package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.presenter.SourceDetailPresenter;
import com.OnlyX.ui.view.SourceDetailView;
import com.OnlyX.ui.widget.Option;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class SourceDetailActivity extends BackActivity implements SourceDetailView {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.source_detail_type)
    Option mSourceType;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.source_detail_title)
    Option mSourceTitle;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.source_detail_favorite)
    Option mSourceFavorite;
    private SourceDetailPresenter mPresenter;

    public static Intent createIntent(Context context, int type) {
        Intent intent = new Intent(context, SourceDetailActivity.class);
        intent.putExtra(Extra.EXTRA_SOURCE, type);
        return intent;
    }

    @Override
    protected SourceDetailPresenter initPresenter() {
        mPresenter = new SourceDetailPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        mPresenter.load(getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1));
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.source_detail_favorite)
    void onSourceFavoriteClick() {
        // TODO 显示这个图源的漫画
    }

    @Override
    public void onSourceLoadSuccess(int type, String title, long count) {
        mSourceType.setSummary(String.valueOf(type));
        mSourceTitle.setSummary(title);
        mSourceFavorite.setSummary(String.valueOf(count));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_source_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.source_detail);
    }

}
