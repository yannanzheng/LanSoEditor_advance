package com.example.custom;

/**
 * Created by jfyang on 12/2/17.
 */

public class MediaProcessManager {

    private int processingCount = 0;

    private  MediaProcessManager() {

    }
    public static MediaProcessManager instance;
    public static MediaProcessManager getInstance(){
        if (null == instance) {
            return new MediaProcessManager();
        }
        return instance;
    }




}
