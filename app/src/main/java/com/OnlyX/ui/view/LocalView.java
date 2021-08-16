package com.OnlyX.ui.view;

import com.OnlyX.component.DialogCaller;
import com.OnlyX.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public interface LocalView extends GridView, DialogCaller {

    void onLocalDeleteSuccess(long id);

    void onLocalScanSuccess(List<MiniComic> list);

}
