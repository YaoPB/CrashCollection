package com.yaopb.example.crashreport;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CrashReportManager {
    private final int SLEEP_TIME = 20 * 1000;
    static CrashReportManager instance;
    static ConcurrentLinkedQueue<String> reportQueue;

    public static CrashReportManager getInstance() {
        if (instance != null) {
            instance = new CrashReportManager();
        }
        reportQueue = new ConcurrentLinkedQueue<>();
        return instance;
    }

    public void inReportQueue(String crashLog) {
        if (reportQueue == null) {
            reportQueue = new ConcurrentLinkedQueue<>();
        }
        reportQueue.add(crashLog);
        startReport();
    }

    private void startReport() {
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (reportQueue != null && !reportQueue.isEmpty()) {
                    String crashLog = reportQueue.poll();
                    send(crashLog);
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private final String url = "http://";

    private void send(String crashLog) {
        if (TextUtils.isEmpty(crashLog)) {
            return;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("crash", crashLog).build();
        //创建一个Request
        Request request = new Request.Builder()
                .post(body)
                .url(url)
                .build();
        //通过client发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // String str = response.body().string();
                }

            }
        });
    }
}
