package com.OnlyX.ui.view;

import com.OnlyX.misc.Switcher;
import com.OnlyX.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public interface TagEditorView extends BaseView {

    void onTagLoadSuccess(List<Switcher<Tag>> list);

    void onTagLoadFail();

    void onTagUpdateSuccess();

    void onTagUpdateFail();

}
