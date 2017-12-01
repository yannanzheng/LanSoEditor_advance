package com.example.custom;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lansoeditor.demo.R;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.lansongsdk.gpuimage.LanSongBeautyFilter;

import static com.lansoeditor.demo.R.id.start_process_bt;

public class MutiTasksProcessActivity extends Activity {
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

    private File sourceFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti_tasks_process);
        sourceFile = new File(Environment.getExternalStorageDirectory(), "ASourceFile/Test1.jpg");

        button = (Button) findViewById(start_process_bt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchProcessPicture();
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
        anyTestThread = new Thread(new Runnable() {//添加任务都在这个子线程李进行，模拟项目环境
            @Override
            public void run() {
                addPictureTasks(100);
            }
        });
    }

    /**
     * 添加多个图片处理任务
     * @param number 任务数量
     */
    private void addPictureTasks(int number) {//添加任务
        for (int i = 0; i < number; i++) {
            Log.d(TAG, "i = " + i);
            addSyncPictureProcessTask();//添加任务
        }
        notifyPostRunnable();//提醒任务开始
    }

    /**
     * 添加一个任务
     */
    private void addSyncPictureProcessTask(){

        Log.d(TAG, "postCount = " + postCount);
        final File cacheDir = new File(Environment.getExternalStorageDirectory(), "abcLansonTest");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File destFile = new File(cacheDir, "img_" + postCount + ".jpg");
        postCount++;

        LanSongBeautyFilter beautyFilter = new LanSongBeautyFilter();//美颜
        beautyFilter.setBeautyLevel(0.8f);

        PictureProcessExportRunnable task = new PictureProcessExportRunnable(getApplicationContext(), sourceFile, destFile, beautyFilter, new GPUImageSepiaFilter());

        task.setOnProcessListener(new PictureProcessExportRunnable.OnProcessListener() {
            @Override
            public void onSucess() {
                Log.d(TAG, "onSucess，处理成功");
                isExecuting = false;
                notifyPostRunnable();
            }

            @Override
            public void onFail() {
                Log.e(TAG, "onSucess，处理失败");
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
        handler.post(runnableTasksQueue.poll());
        Log.d(TAG,"开始处理啦！剩余任务数量taskNum = " +runnableTasksQueue.size());
    }

}
