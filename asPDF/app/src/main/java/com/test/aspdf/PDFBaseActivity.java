package com.test.aspdf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.artifex.mupdfdemo.AsyncTask;
import com.artifex.mupdfdemo.MuPDFAlert;
import com.artifex.mupdfdemo.MuPDFCore;

import java.util.concurrent.Executor;

/**
 * Created by jcman on 16-9-7.
 */
public class PDFBaseActivity extends Activity{

    protected MuPDFCore core;
    protected AlertDialog.Builder mAlertBuilder  ;
    protected boolean mAlertsActive= false;
    protected AsyncTask<Void,Void,MuPDFAlert> mAlertTask;
    protected AlertDialog mAlertDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlertBuilder = new AlertDialog.Builder(this);
    }

    public void createAlertWaiter() {
        mAlertsActive = true;
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void,Void,MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for(int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1: index=0; break;
                                case AlertDialog.BUTTON2: index=1; break;
                                case AlertDialog.BUTTON3: index=2; break;
                            }
                            result.buttonPressed = pressed[index];
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType)
                {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType)
                {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, "Cancel", listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, "Okay", listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, "Cancel", listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, "Yes", listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, "No", listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    public void onDestroy(){
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;
        super.onDestroy();
    }

    protected void onStart() {
        if (core != null)
        {
            core.startAlerts();
            createAlertWaiter();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (core != null)
        {
            destroyAlertWaiter();
            core.stopAlerts();
        }

        super.onStop();
    }
}

class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}
