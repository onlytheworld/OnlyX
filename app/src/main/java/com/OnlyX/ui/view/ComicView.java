package com.OnlyX.ui.view;

import com.OnlyX.component.DialogCaller;
import com.OnlyX.component.ThemeResponsive;
import com.OnlyX.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface ComicView extends BaseView, ThemeResponsive, DialogCaller {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

}
