package com.OnlyX.component;

import android.os.Bundle;

/**
 * Created by Hiroshi on 2016/12/4.
 */

public interface DialogCaller {

    String EXTRA_DIALOG_RESULT_INDEX = "onlyx.intent.extra.EXTRA_DIALOG_RESULT_INDEX";
    String EXTRA_DIALOG_RESULT_VALUE = "onlyx.intent.extra.EXTRA_DIALOG_RESULT_VALUE";
    String EXTRA_DIALOG_REQUEST_CODE = "onlyx.intent.extra.EXTRA_DIALOG_REQUEST_CODE";
    String EXTRA_DIALOG_TITLE = "onlyx.intent.extra.EXTRA_DIALOG_TITLE";
    String EXTRA_DIALOG_ITEMS = "onlyx.intent.extra.EXTRA_DIALOG_ITEMS";
    String EXTRA_DIALOG_CONTENT = "onlyx.intent.extra.EXTRA_DIALOG_CONTENT";
    String EXTRA_DIALOG_CONTENT_TEXT = "onlyx.intent.extra.EXTRA_DIALOG_CONTENT_TEXT";
    String EXTRA_DIALOG_NEGATIVE = "onlyx.intent.extra.EXTRA_DIALOG_NEGATIVE";
    String EXTRA_DIALOG_CHOICE_ITEMS = "onlyx.intent.extra.EXTRA_DIALOG_CHOICE_ITEMS";

    void onDialogResult(int requestCode, Bundle bundle);

}
