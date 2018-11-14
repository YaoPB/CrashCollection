package com.yaopb.example.crashgather.jue;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.yaopb.example.crashreport.CrashReportManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JUE catcher
 */
public class JUECatcher implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;
    private static JUECatcher sInstance;
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    private JUECatcher() {

    }

    public static JUECatcher getInstance() {
        if (sInstance == null) {
            sInstance = new JUECatcher();
        }
        return sInstance;
    }

    public void init(Context context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            collectExceptionInfo(e);
        } catch (PackageManager.NameNotFoundException nameEx) {
            nameEx.printStackTrace();
        } catch (JSONException jsonEx) {
            jsonEx.printStackTrace();
        }
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(t, e);
        } else {
            // Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    private void collectExceptionInfo(Throwable ex) throws JSONException, PackageManager.NameNotFoundException {
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(current));
        JSONObject object = new JSONObject();
        object.put("Datetime", time);
        object.put("Environment:", dumpPhoneInfo());
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement ste : ex.getStackTrace()) {
            stringBuilder.append(ste.getLineNumber() + " " + ste.getMethodName() + " " + ste.getClassName() + "\r\n");
        }
        object.put("trace", stringBuilder.toString());

        uploadExceptionToServer(object);
    }

    private JSONObject dumpPhoneInfo() throws PackageManager.NameNotFoundException, JSONException {
        JSONObject jsonObject = new JSONObject();
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                PackageManager.GET_ACTIVITIES);
        jsonObject.put("App version:", pi.versionName + "_" + pi.versionCode);
        jsonObject.put("OS Version:", Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
        jsonObject.put("Vendor:", Build.MANUFACTURER);
        jsonObject.put("Model:", Build.MODEL);
        jsonObject.put("CPU ABI:", Build.CPU_ABI);
        return jsonObject;
    }

    private void uploadExceptionToServer(JSONObject log) {
        CrashReportManager.getInstance().inReportQueue(log.toString());
    }
}
