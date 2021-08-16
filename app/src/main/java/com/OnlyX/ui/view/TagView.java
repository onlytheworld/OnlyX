package com.OnlyX.ui.view;

import com.OnlyX.component.DialogCaller;
import com.OnlyX.component.ThemeResponsive;
import com.OnlyX.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public interface TagView extends BaseView, ThemeResponsive, DialogCaller {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

    void onTagDeleteSuccess(Tag tag);

    void onTagDeleteFail();

    void onTagRestore(List<Tag> list);

}
