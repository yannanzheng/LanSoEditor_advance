package com.example.custom;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lansoeditor.demo.R;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static com.lansoeditor.demo.R.id.start_process_bt;

public class MutiTasksProcessActivity extends Activity {
    private static final String TAG="simulate_task";
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti_tasks_process);

        button = (Button) findViewById(start_process_bt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startMockProcessMutiPicture();
                startProcessMutiPicture();
            }
        });
    }

    private Handler handler;
    private Queue<Runnable> runnableTasksQueue;
    private int postCount = 0;
    private Thread anyTestThread;
    private Thread mainProcessThread;

    private Context context;
    private volatile boolean isExecuting;
    private void startMockProcessMutiPicture() {
        runnableTasksQueue = new ArrayBlockingQueue<Runnable>(100);
        context = getApplicationContext();

        initProcessThread();
        anyTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    Log.d(TAG, "i = " + i);
                    addTask();
                    notifyPostRunnable();
//                    handler.post(runnableTasksQueue.poll());
                }
            }
        });
    }

    private void startProcessMutiPicture() {
        runnableTasksQueue = new ArrayBlockingQueue<Runnable>(100);
        context = getApplicationContext();

        anyTestThread = new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 100; i++) {
                    Log.d(TAG, "i = " + i);

                    addPictureProcessTask();
                    notifyPostRunnable();
//                    handler.post(runnableTasksQueue.poll());
                }
            }
        });
        initProcessThread();
    }

    private void addPictureProcessTask() {

        Log.d(TAG, "postCount = " + postCount++);

        PictureProcessTask task = new PictureProcessTask(getApplicationContext());
        task.setOnProcessListener(new PictureProcessTask.OnProcessListener() {
            @Override
            public void onSucess() {
                Log.d(TAG, "onSucess，写出成功");
                isExecuting = false;
                notifyPostRunnable();
            }
        });

        runnableTasksQueue.offer(task);
    }

    private void addTask() {
        runnableTasksQueue.offer(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "postCount = " + postCount++);

                ProcessTask task = new ProcessTask(getApplicationContext());
                task.setOnProcessListener(new ProcessTask.OnProcessListener() {
                    @Override
                    public void onSucess() {
                        isExecuting = false;
                        notifyPostRunnable();
                    }
                });

//                new Thread(task).start();
            }
        });
    }

    private void initProcessThread() {
        //保证处理线程已经启动
        mainProcessThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                anyTestThread.start();//保证处理线程已经启动
                Looper.loop();
            }
        });

        mainProcessThread.start();
    }

    private void notifyPostRunnable() {
        Log.d(TAG,"notifyPostRunnable" );
        if (isExecuting || null == runnableTasksQueue || runnableTasksQueue.size() == 0) {
            Log.d(TAG, "notifyPostRunnable, return");
            return;
        }
        isExecuting = true;
        Log.d(TAG,"开始处理啦！" );
        handler.post(runnableTasksQueue.poll());
    }

}
