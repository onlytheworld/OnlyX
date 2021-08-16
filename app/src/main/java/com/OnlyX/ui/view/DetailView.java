package com.OnlyX.ui.view;

import com.OnlyX.model.Chapter;
import com.OnlyX.model.Comic;
import com.OnlyX.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface DetailView extends BaseView {

    void onComicLoadSuccess(Comic comic);

    void onChapterLoadSuccess(List<Chapter> list);

    void onLastChange(String chapter);

    void onParseError();

    void onTaskAddSuccess(ArrayList<Task> list);

    void onTaskAddFail();

}
