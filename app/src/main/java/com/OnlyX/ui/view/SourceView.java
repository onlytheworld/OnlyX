package com.OnlyX.ui.view;

import com.OnlyX.component.ThemeResponsive;
import com.OnlyX.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SourceView extends BaseView, ThemeResponsive {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

}
