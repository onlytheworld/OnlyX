package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.misc.Switcher;
import com.OnlyX.model.Tag;
import com.OnlyX.presenter.BasePresenter;
import com.OnlyX.presenter.TagEditorPresenter;
import com.OnlyX.ui.adapter.BaseAdapter;
import com.OnlyX.ui.adapter.TagEditorAdapter;
import com.OnlyX.ui.view.TagEditorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorActivity extends CoordinatorActivity implements TagEditorView {

    private TagEditorPresenter mPresenter;
    private TagEditorAdapter mTagAdapter;

    public static Intent createIntent(Context context, long id) {
        Intent intent = new Intent(context, TagEditorActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        return intent;
    }

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new TagEditorPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected BaseAdapter initAdapter() {
        mTagAdapter = new TagEditorAdapter(this, new ArrayList<>());
        return mTagAdapter;
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(R.drawable.ic_done_white_24dp);
        mActionButton.show();
        hideProgressBar();
    }

    @Override
    protected void initView() {
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        mPresenter.load(id);
    }

    @Override
    public void onTagLoadSuccess(List<Switcher<Tag>> list) {
        hideProgressBar();
        mTagAdapter.addAll(list);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onTagUpdateSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onTagUpdateFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onItemClick(View view, int position) {
        Switcher<Tag> switcher = mTagAdapter.getItem(position);
        switcher.switchEnable();
        mTagAdapter.notifyItemChanged(position);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.coordinator_action_button)
    void onActionButtonClick() {
        showProgressDialog();
        List<Long> list = new ArrayList<>();
        for (Switcher<Tag> switcher : mTagAdapter.getDateSet()) {
            if (switcher.isEnable()) {
                list.add(switcher.getElement().getId());
            }
        }
        mPresenter.updateRef(list);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.tag_editor);
    }

}
