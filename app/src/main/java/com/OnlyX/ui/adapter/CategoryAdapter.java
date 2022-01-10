package com.OnlyX.ui.adapter;

import android.content.Context;
import android.util.Pair;
import android.widget.ArrayAdapter;

import com.OnlyX.R;
import com.OnlyX.utils.CollectionUtils;

import java.util.List;


/**
 * Created by Hiroshi on 2018/2/13.
 */

public class CategoryAdapter extends ArrayAdapter<String> {

    private final List<Pair<String, String>> mCategoryList;

    public CategoryAdapter(Context context, List<Pair<String, String>> list) {
        super(context, R.layout.item_spinner, CollectionUtils.map(list, pair -> pair.first));
        mCategoryList = list;
    }

    public String getValue(int position) {
        return mCategoryList.get(position).second;
    }

}
