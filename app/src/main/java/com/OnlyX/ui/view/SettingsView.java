package com.OnlyX.ui.view;

import com.OnlyX.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SettingsView extends BaseView, DialogCaller {

    void onFileMoveSuccess();

    void onExecuteSuccess();

    void onExecuteFail();

}
