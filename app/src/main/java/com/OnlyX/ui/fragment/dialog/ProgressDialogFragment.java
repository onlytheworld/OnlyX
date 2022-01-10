package com.OnlyX.ui.fragment.dialog;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.OnlyX.R;
import com.OnlyX.rx.RxBus;
import com.OnlyX.rx.RxEvent;
import com.OnlyX.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Hiroshi on 2016/10/14.
 */

public class ProgressDialogFragment extends DialogFragment {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dialog_progress_bar)
    ProgressBar mProgressBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dialog_progress_text)
    TextView mTextView;

    private Unbinder unbinder;
    private CompositeSubscription mCompositeSubscription;

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_progress, container, false);
        unbinder = ButterKnife.bind(this, view);
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        int resId = ThemeUtils.getResourceId(requireActivity(), R.attr.colorAccent);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(requireActivity(), resId), PorterDuff.Mode.SRC_ATOP);
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(RxBus.getInstance().toObservable(RxEvent.EVENT_DIALOG_PROGRESS).subscribe(rxEvent -> mTextView.setText((String) rxEvent.getData())));
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

}
