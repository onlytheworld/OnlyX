package com.OnlyX.ui.fragment.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.OnlyX.R;
import com.OnlyX.component.DialogCaller;
import com.OnlyX.global.ClickEvents;
import com.OnlyX.manager.PreferenceManager;
import com.OnlyX.ui.activity.settings.EventSettingsActivity;
import com.OnlyX.ui.fragment.BaseFragment;
import com.OnlyX.ui.fragment.dialog.ChoiceDialogFragment;
import com.OnlyX.ui.widget.preference.CheckBoxPreference;
import com.OnlyX.ui.widget.preference.ChoicePreference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class StreamConfigFragment extends BaseFragment implements DialogCaller {

    private static final int DIALOG_REQUEST_ORIENTATION = 0;
    private static final int DIALOG_REQUEST_TURN = 1;
    private static final int DIALOG_REQUEST_OPERATION = 3;

    private static final int OPERATION_VOLUME_UP = 0;
    private static final int OPERATION_VOLUME_DOWN = 1;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_interval)
    CheckBoxPreference mReaderInterval;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_load_prev)
    CheckBoxPreference mReaderLoadPrev;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_load_next)
    CheckBoxPreference mReaderLoadNext;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_orientation)
    ChoicePreference mReaderOrientation;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.settings_reader_turn)
    ChoicePreference mReaderTurn;

//    @BindView(R.id.settings_reader_volume_click_event) View mReaderVolumeEvent;

    @Override
    protected void initView() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            mReaderVolumeEvent.setVisibility(View.VISIBLE);
//        } else {
//            mReaderVolumeEvent.setVisibility(View.GONE);
//        }
        mReaderInterval.bindPreference(PreferenceManager.PREF_READER_STREAM_INTERVAL, false);
        mReaderLoadPrev.bindPreference(PreferenceManager.PREF_READER_STREAM_LOAD_PREV, false);
        mReaderLoadNext.bindPreference(PreferenceManager.PREF_READER_STREAM_LOAD_NEXT, true);
        mReaderOrientation.bindPreference(requireActivity().getSupportFragmentManager(), this, PreferenceManager.PREF_READER_STREAM_ORIENTATION,
                PreferenceManager.READER_ORIENTATION_AUTO, R.array.reader_orientation_items, DIALOG_REQUEST_ORIENTATION);
        mReaderTurn.bindPreference(requireActivity().getSupportFragmentManager(), this, PreferenceManager.PREF_READER_STREAM_TURN,
                PreferenceManager.READER_TURN_LTR, R.array.reader_turn_items, DIALOG_REQUEST_TURN);
    }

    @OnClick({R.id.settings_reader_click_event, R.id.settings_reader_long_click_event})
    void onReaderEventClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event;
        Intent intent = EventSettingsActivity.createIntent(getActivity(), isLong,
                mReaderOrientation.getValue(), true);
        startActivity(intent);
    }

//    @OnClick(R.id.settings_reader_volume_click_event)
//    void onReaderVolumeEventClick() {
//        String[] items = {"音量上键", "音量下键"};
//        ItemDialogFragment fragment = ItemDialogFragment.newInstance(R.string.common_operation_select,
//                items, DIALOG_REQUEST_OPERATION);
//        fragment.setTargetFragment(this, 0);
//        fragment.show(getFragmentManager(), null);
//    }

    private void showEventList(int index) {
        int[] mChoiceArray = ClickEvents.getStreamClickEventChoice(mPreference);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Context context = this.getContext();
            ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.event_select,
                    ClickEvents.getEventTitleArray(context), mChoiceArray[index], index);
            fragment.show(requireActivity().getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_ORIENTATION:
                mReaderOrientation.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_TURN:
                mReaderTurn.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_OPERATION:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                switch (index) {
                    case OPERATION_VOLUME_UP:
                        showEventList(5);
                        break;
                    case OPERATION_VOLUME_DOWN:
                        showEventList(6);
                        break;
                }
                break;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_stream_config;
    }

}
