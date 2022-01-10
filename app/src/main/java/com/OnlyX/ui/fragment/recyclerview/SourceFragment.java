package com.OnlyX.ui.fragment.recyclerview;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.OnlyX.R;
import com.OnlyX.global.Extra;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.model.Source;
import com.OnlyX.presenter.SourcePresenter;
import com.OnlyX.ui.activity.CategoryActivity;
import com.OnlyX.ui.activity.SearchActivity;
import com.OnlyX.ui.activity.SourceDetailActivity;
import com.OnlyX.ui.adapter.SourceAdapter;
import com.OnlyX.ui.view.SourceView;
import com.OnlyX.utils.HintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends RecyclerViewFragment implements SourceView, SourceAdapter.OnItemCheckedListener {

    private SourcePresenter mPresenter;
    private SourceAdapter mSourceAdapter;

    @Override
    protected SourcePresenter initPresenter() {
        mPresenter = new SourcePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        super.initView();
    }

    @Override
    protected SourceAdapter initAdapter() {
        mSourceAdapter = new SourceAdapter(getActivity(), new ArrayList<>());
        mSourceAdapter.setOnItemCheckedListener(this);
        return mSourceAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_source, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.comic_search) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra(Extra.EXTRA_PICKER_PATH,"SourceFragment");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Source source = mSourceAdapter.getItem(position);
        if (SourceManager.getInstance(this).getParser(source.getType()).getCategory() == null) {
            HintUtils.showToast(getActivity(), R.string.common_execute_fail);
        } else {
            Intent intent = CategoryActivity.createIntent(getActivity(), source.getType(), source.getTitle());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        Intent intent = SourceDetailActivity.createIntent(getActivity(), mSourceAdapter.getItem(position).getType());
        startActivity(intent);
        return true;
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mSourceAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        hideProgressBar();
        mSourceAdapter.addAll(list);
    }

    @Override
    public void onSourceLoadFail() {
        hideProgressBar();
        HintUtils.showToast(getActivity(), R.string.common_data_load_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mSourceAdapter.setColor(ContextCompat.getColor(requireActivity(), accent));
        mSourceAdapter.notifyDataSetChanged();
    }

}
