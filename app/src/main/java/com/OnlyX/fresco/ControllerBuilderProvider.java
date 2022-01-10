package com.OnlyX.fresco;

import android.content.Context;
import android.util.SparseArray;

import com.OnlyX.manager.SourceManager;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;

/**
 * Created by Hiroshi on 2016/9/5.
 */
public class ControllerBuilderProvider {

    private final Context mContext;
    private final boolean mCover;
    private final SourceManager.SMGetter mSMGetter;
    private final SparseArray<ImagePipeline> mPipelineArray;
    private final SparseArray<PipelineDraweeControllerBuilderSupplier> mSupplierArray;

    public ControllerBuilderProvider(Context context, SourceManager.SMGetter getter, boolean cover) {
        mSupplierArray = new SparseArray<>();
        mPipelineArray = new SparseArray<>();
        mContext = context;
        mSMGetter = getter;
        mCover = cover;
    }

    public PipelineDraweeControllerBuilder get(int type) {
        PipelineDraweeControllerBuilderSupplier supplier = mSupplierArray.get(type);
        if (supplier == null) {
            ImagePipelineFactory factory = ImagePipelineFactoryBuilder
                    .build(mContext, mSMGetter.getHeader(type), mCover);
            supplier = ControllerBuilderSupplierFactory.get(mContext, factory);
            mSupplierArray.put(type, supplier);
            mPipelineArray.put(type, factory.getImagePipeline());
        }
        return supplier.get();
    }

    public void pause() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).pause();
        }
    }

    public void resume() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).resume();
        }
    }

    public void clear() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).clearMemoryCaches();
        }
    }

}
