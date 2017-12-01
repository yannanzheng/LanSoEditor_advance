package com.example.custom.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.custom.SyncPictureProcessTask;
import com.lansoeditor.demo.R;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static com.lansoeditor.demo.R.id.start_process_bt;

/**
 * 包含一些测试的东西，存档
 */
public class MutiTasksProcessTestActivity extends Activity {
    private static final String TAG="simulate_task";
    private Button button;
    private Handler handler;
    private Queue<Runnable> runnableTasksQueue;
    private int postCount = 0;
    private Thread anyTestThread;
    private Thread mainProcessThread;

    private Context context;
    private volatile boolean isExecuting;

    private int finishCopyCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti_tasks_process);

        button = (Button) findViewById(start_process_bt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchProcessPicture();
//                startMockProcessMutiPicture();//多图片处理
//                startProcessMutiPicture();
            }
        });
    }

    /**
     * 批量处理图片
     */
    private void batchProcessPicture() {
        runnableTasksQueue = new ArrayBlockingQueue<Runnable>(100);
        context = getApplicationContext();

        initProcessThread();
        anyTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    Log.d(TAG, "i = " + i);
                    addSyncPictureProcessTasks();//
                    notifyPostRunnable();
                }
            }
        });
    }

    private void addSyncPictureProcessTasks(){

        Log.d(TAG, "postCount = " + postCount++);
        SyncPictureProcessTask task = new SyncPictureProcessTask(getApplicationContext());
        task.setOnProcessListener(new SyncPictureProcessTask.OnProcessListener() {
            @Override
            public void onSucess() {
                Log.d(TAG, "onSucess，写出成功");
                isExecuting = false;
                notifyPostRunnable();
            }
        });

        runnableTasksQueue.offer(task);
    }

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

                for (int i = 0; i < 10; i++) {
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

        AsyncPictureProcessTask task = new AsyncPictureProcessTask(getApplicationContext());
        task.setOnProcessListener(new AsyncPictureProcessTask.OnProcessListener() {
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

        Log.d(TAG, "postCount = " + postCount++);
        SyncPictureProcessTask task = new SyncPictureProcessTask(getApplicationContext());
        task.setOnProcessListener(new SyncPictureProcessTask.OnProcessListener() {
            @Override
            public void onSucess() {
                isExecuting = false;
                Log.d(TAG, "完成一个copy" + finishCopyCount++);
                notifyPostRunnable();
            }
        });

        runnableTasksQueue.offer(task);
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
