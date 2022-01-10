package com.OnlyX.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.App;
import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.manager.TagManager;
import com.OnlyX.model.MiniComic;
import com.OnlyX.presenter.PartFavoritePresenter;
import com.OnlyX.ui.adapter.BaseAdapter;
import com.OnlyX.ui.adapter.GridAdapter;
import com.OnlyX.ui.fragment.dialog.MessageDialogFragment;
import com.OnlyX.ui.fragment.dialog.MultiDialogFragment;
import com.OnlyX.ui.view.PartFavoriteView;
import com.OnlyX.utils.HintUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class PartFavoriteActivity extends BackActivity implements PartFavoriteView, BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemLongClickListener {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_ADD = 1;

    @BindView(R.id.part_favorite_recycler_view)
    RecyclerView mRecyclerView;

    private PartFavoritePresenter mPresenter;
    private GridAdapter mGridAdapter;

    private MiniComic mSavedComic;
    private boolean isDeletable;

    public static Intent createIntent(Context context, long id, String title) {
        Intent intent = new Intent(context, PartFavoriteActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        intent.putExtra(Extra.EXTRA_KEYWORD, title);
        return intent;
    }

    @Override
    protected PartFavoritePresenter initPresenter() {
        mPresenter = new PartFavoritePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mGridAdapter = new GridAdapter(this, new LinkedList<>());
        mGridAdapter.setSymbol(true);
        mGridAdapter.setProvider(((App) getApplication()).getBuilderProvider());
        mGridAdapter.setSMGetter(SourceManager.getInstance(this).new SMGetter());
        mGridAdapter.setOnItemClickListener(this);
        mGridAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.addItemDecoration(mGridAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mGridAdapter);
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        isDeletable = id != TagManager.TAG_CONTINUE && id != TagManager.TAG_FINISH;
        mPresenter.load(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getLongExtra(Extra.EXTRA_ID, -1) >= 0) {
            getMenuInflater().inflate(R.menu.menu_part_favorite, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.part_favorite_add) {
            showProgressDialog();
            mPresenter.loadComicTitle(mGridAdapter.getDateSet());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(this, comic.getId(), -1, null);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        if (isDeletable) {
            mSavedComic = mGridAdapter.getItem(position);
            MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                    R.string.part_favorite_delete_confirm, true, DIALOG_REQUEST_DELETE);
            fragment.show(getSupportFragmentManager(), null);
            return true;
        }
        return false;
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                long id = mSavedComic.getId();
                mPresenter.delete(id);
                mGridAdapter.remove(mSavedComic);
                HintUtils.showToast(this, R.string.common_execute_success);
                break;
            case DIALOG_REQUEST_ADD:
                showProgressDialog();
                boolean[] check = bundle.getBooleanArray(EXTRA_DIALOG_RESULT_VALUE);
                mPresenter.insert(check);
                break;
        }
    }

    @Override
    public void onComicLoadFail() {
        hideProgressBar();
        HintUtils.showToast(this, R.string.common_data_load_fail);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        hideProgressBar();
        mGridAdapter.addAll(list);
    }

    @Override
    public void onComicTitleLoadSuccess(List<String> list) {
        hideProgressDialog();
        MultiDialogFragment fragment = MultiDialogFragment.newInstance(this, R.string.part_favorite_select,
                list.toArray(new String[0]), null, DIALOG_REQUEST_ADD);
        fragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onComicTitleLoadFail() {
        hideProgressDialog();
        HintUtils.showToast(this, R.string.common_data_load_fail);
    }

    @Override
    public void onComicInsertSuccess(List<MiniComic> list) {
        hideProgressDialog();
        mGridAdapter.addAll(list);
        HintUtils.showToast(this, R.string.common_execute_success);
    }

    @Override
    public void onComicInsertFail() {
        hideProgressDialog();
        HintUtils.showToast(this, R.string.common_execute_fail);
    }

    @Override
    public void onHighlightCancel(MiniComic comic) {
        mGridAdapter.moveItemTop(comic);
    }

    @Override
    public void onComicRead(MiniComic comic) {
        mGridAdapter.moveItemTop(comic);
    }

    @Override
    public void onComicRemove(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicAdd(MiniComic comic) {
        if (!mGridAdapter.contains(comic)) {
            mGridAdapter.add(0, comic);
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getIntent().getStringExtra(Extra.EXTRA_KEYWORD);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_part_favorite;
    }

    @Override
    protected boolean isNavTranslation() {
        return true;
    }

}
