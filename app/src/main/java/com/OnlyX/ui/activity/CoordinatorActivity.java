package com.OnlyX.ui.activity;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.R;
import com.OnlyX.ui.adapter.BaseAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/1.
 */

public abstract class CoordinatorActivity extends BackActivity implements
        BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.coordinator_action_button)
    FloatingActionButton mActionButton;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.coordinator_action_button2)
    FloatingActionButton mActionButton2;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.coordinator_recycler_view)
    RecyclerView mRecyclerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mLayoutView;

    @Override
    protected void initView() {
        super.initView();
        mRecyclerView.setLayoutManager(initLayoutManager());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        BaseAdapter adapter = initAdapter();
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        RecyclerView.ItemDecoration decoration = adapter.getItemDecoration();
        if (decoration != null) {
            mRecyclerView.addItemDecoration(adapter.getItemDecoration());
        }
        mRecyclerView.setAdapter(adapter);
        initActionButton();
    }

    protected abstract BaseAdapter initAdapter();

    protected void initActionButton() {
    }

    protected RecyclerView.LayoutManager initLayoutManager() {
        return new LinearLayoutManager(this);
    }

    @Override
    public void onItemClick(View view, int position) {
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_coordinator;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

}
