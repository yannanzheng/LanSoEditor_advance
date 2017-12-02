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

import com.example.custom.dependency.MediaEditType;
import com.lansoeditor.demo.R;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static com.lansoeditor.demo.R.id.start_process_bt;

public class MutiTasksProcessActivity extends Activity {
    private static final String TAG="simulate_task";
    private Button button;
    private Handler handler;
    private Queue<Runnable> runnableTasksQueue;
    private int postCount = 0;
    private Thread anyTestThread;
    private Thread processThread;

    private Context context;
    private volatile boolean isExecuting;

    private int finishCopyCount = 0;

    private File sourcePictureFile;
    private Button testVideoEditButton;
    private Button mixProcessButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muti_tasks_process);
        sourcePictureFile = new File(Environment.getExternalStorageDirectory(), "ASourceFile/Test1.jpg");

        button = (Button) findViewById(start_process_bt);
        testVideoEditButton = (Button) findViewById(R.id.start_process_video_bt);
        mixProcessButton = (Button) findViewById(R.id.start_process_media_bt);

        initProcessThread();
        runnableTasksQueue = new ArrayBlockingQueue<Runnable>(100);
        context = getApplicationContext();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batchProcessPicture();
            }
        });

        testVideoEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processVideo();
            }
        });

        mixProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processMedia();
            }
        });
    }

    private void processMedia() {
        addPictureTasks(3);
        addVideoTask(1);
        addPictureTasks(2);
        addVideoTask(2);
        notifyPostRunnable();
    }

    private void processVideo() {
        Log.d(TAG, "processVideo");
        addVideoTasks();
        notifyPostRunnable();
    }

    private void addVideoTask(int i) {
        File sourceVideoFile = new File(Environment.getExternalStorageDirectory(), "ASourceFile/V1.mp4");
        final File cacheDir = new File(Environment.getExternalStorageDirectory(), "abcLansonTest");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File destFile = new File(cacheDir, "vid_" + i + ".mp4");

        VideoProcessExportRunnable videoProcessExportRunnable = new VideoProcessExportRunnable(context, sourceVideoFile.getAbsolutePath(), destFile.getAbsolutePath(), MediaEditType.FaceBeauty.LEVEL_4, MediaEditType.Filter.Filter_LanSongSepia);
        videoProcessExportRunnable.setOnProcessListener(new VideoProcessExportRunnable.OnProcessListener() {
            @Override
            public void onSucess(String exportedFilePath) {
                Log.d(TAG, "视频onSucess, exportedFilePath = "+exportedFilePath);
                isExecuting = false;
                notifyPostRunnable();
            }

            @Override
            public void onProgress(double progress) {
                Log.d(TAG, "视频progress = "+progress);
            }

            @Override
            public void onFail() {
                Log.d(TAG, "视频fail");
            }
        });
        runnableTasksQueue.offer(videoProcessExportRunnable);
    }

    private void addVideoTasks() {
        for (int i = 0; i < 5; i++) {
            addVideoTask(i);
        }
    }

    /**
     * 批量处理图片
     */
    private void batchProcessPicture() {
        anyTestThread = new Thread(new Runnable() {//添加任务都在这个子线程李进行，模拟项目环境
            @Override
            public void run() {
                addPictureTasks(100);
            }
        });
        anyTestThread.start();
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

        PictureProcessExportRunnable task = new PictureProcessExportRunnable(getApplicationContext(), sourcePictureFile.getAbsolutePath(), destFile.getAbsolutePath(), MediaEditType.FaceBeauty.LEVEL_4, MediaEditType.Filter.Filter_LanSongSepia);

        task.setOnProcessListener(new PictureProcessExportRunnable.OnProcessListener() {
            @Override
            public void onSucess(String exportedFilePath ) {
                Log.d(TAG, "onSucess，图片处理成功");
                isExecuting = false;
                notifyPostRunnable();
            }

            @Override
            public void onFail() {
                Log.e(TAG, "onSucess，图片处理失败");
                notifyPostRunnable();
            }
        });

        runnableTasksQueue.offer(task);
    }

    private void initProcessThread() {
        //保证处理线程已经启动
        processThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
//                anyTestThread.start();//保证处理线程已经启动
                Looper.loop();
            }
        });

        processThread.start();
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
