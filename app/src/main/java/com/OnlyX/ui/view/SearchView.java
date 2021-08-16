package com.OnlyX.ui.view;

import com.OnlyX.component.DialogCaller;
import com.OnlyX.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface SearchView extends BaseView, DialogCaller {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

    void onAutoCompleteLoadSuccess(List<String> list);

}
