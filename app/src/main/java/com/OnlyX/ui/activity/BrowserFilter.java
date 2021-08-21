package com.OnlyX.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.OnlyX.R;
import com.OnlyX.manager.SourceManager;
import com.OnlyX.source.Animx2;
import com.OnlyX.source.BaiNian;
import com.OnlyX.source.BuKa;
import com.OnlyX.source.Cartoonmad;
import com.OnlyX.source.ChuiXue;
import com.OnlyX.source.DM5;
import com.OnlyX.source.Dmzjv2;
import com.OnlyX.source.Hhxxee;
import com.OnlyX.source.IKanman;
import com.OnlyX.source.MH50;
import com.OnlyX.source.MH517;
import com.OnlyX.source.MH57;
import com.OnlyX.source.ManHuaDB;
import com.OnlyX.source.MiGu;
import com.OnlyX.source.NetEase;
import com.OnlyX.source.PuFei;
import com.OnlyX.source.Tencent;
import com.OnlyX.source.TuHao;
import com.OnlyX.source.U17;
import com.OnlyX.source.JMTT;

import java.util.ArrayList;
import java.util.List;

public class BrowserFilter extends BaseActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_browser_filter;
    }

    @Override
    protected String getDefaultTitle() {
        return "jumping...";
    }

//    private Parser parser;
//    private SourceManager mSourceManager;

    public void openDetailActivity(int source, String comicId) {
        Intent intent = DetailActivity.createIntent(this, null, source, comicId);
        startActivity(intent);
    }

//    public void openReaderActivity(int source,String comicId) {
//        Intent intent = DetailActivity.createIntent(this, null, source, comicId);
//        startActivity(intent);
//    }

    private List<Integer> registUrlListener() {
        List<Integer> list = new ArrayList<>();

        list.add(Dmzjv2.TYPE);
        list.add(JMTT.TYPE);
        list.add(BuKa.TYPE);
        list.add(PuFei.TYPE);
        list.add(Cartoonmad.TYPE);
        list.add(Animx2.TYPE);
        list.add(MH517.TYPE);
        list.add(BaiNian.TYPE);
        list.add(MiGu.TYPE);
        list.add(Tencent.TYPE);
        list.add(U17.TYPE);
        list.add(MH57.TYPE);
        list.add(MH50.TYPE);
        list.add(DM5.TYPE);
        list.add(IKanman.TYPE);
        list.add(NetEase.TYPE);
        list.add(Hhxxee.TYPE);
        list.add(BaiNian.TYPE);
        list.add(ChuiXue.TYPE);
        list.add(ManHuaDB.TYPE);
        list.add(TuHao.TYPE);

        return list;
    }

    private void openReader(Uri uri) {
        SourceManager mSourceManager = SourceManager.getInstance(this);
        String comicId;

        for (int i : registUrlListener()) {
            if (mSourceManager.getParser(i).isHere(uri)
                    && ((comicId = mSourceManager.getParser(i).getComicId(uri)) != null)) {
                openDetailActivity(i, comicId);
            }
        }
    }

    private void openReaderByIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        //来自url
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                openReader(uri);
            } else {
                Toast.makeText(this, "url不合法", Toast.LENGTH_SHORT);
            }
        }

        //来自分享
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            try {
                openReader(Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT)));
            } catch (Exception ex) {
                Toast.makeText(this, "url不合法", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_filter);

        openReaderByIntent(getIntent());

        finish();
    }
}
