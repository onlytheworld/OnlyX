package com.OnlyX.ui.fragment.recyclerview;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.R;
import com.OnlyX.ui.adapter.BaseAdapter;
import com.OnlyX.ui.fragment.BaseFragment;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public abstract class RecyclerViewFragment<T> extends BaseFragment implements BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemLongClickListener {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view_content)
    protected RecyclerView mRecyclerView;

    @Override
    protected void initView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(initLayoutManager());
        BaseAdapter<T> adapter = initAdapter();
        if (adapter != null) {
            adapter.setOnItemClickListener(this);
            adapter.setOnItemLongClickListener(this);
            mRecyclerView.addItemDecoration(adapter.getItemDecoration());
            mRecyclerView.setAdapter(adapter);
        }
    }

    abstract protected BaseAdapter<T> initAdapter();

    protected abstract RecyclerView.LayoutManager initLayoutManager();

    @Override
    public void onItemClick(View view, int position) {
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_recycler_view;
    }

}
