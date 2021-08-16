package com.OnlyX.ui.view;

import com.OnlyX.component.DialogCaller;
import com.OnlyX.component.ThemeResponsive;
import com.OnlyX.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/30.
 */

public interface GridView extends BaseView, DialogCaller, ThemeResponsive {

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onExecuteFail();

}
