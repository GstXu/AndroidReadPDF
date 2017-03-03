package com.test.aspdf;

import android.os.Bundle;
import android.os.Environment;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;

public class ReadPDFActivity extends PDFBaseActivity {
    private MuPDFReaderView mDocView;
    private MuPDFCore openFile(String path){
        try {
            core = new MuPDFCore(this, path);
        }catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        return core;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (core == null) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AAAA/pdf5.pdf";
            core = openFile(path);
            if (core != null && core.countPages() == 0)
            {
                core = null;
            }
        }
        if (core == null)
        {
            Toast.makeText(ReadPDFActivity.this,"pdf文件损坏无法打开",Toast.LENGTH_SHORT).show();
            return;
        }
        setContentView(R.layout.activity_readpdf);
        createUI();
    }

    public void createUI() {
        if (core == null)
            return;
        mDocView = new MuPDFReaderView(this);
        mDocView.setAdapter(new MuPDFPageAdapter(this, core));
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_read);
        layout.addView(mDocView);
    }
}
