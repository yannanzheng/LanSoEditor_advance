package com.example.custom;

/**
 * Created by jfyang on 11/22/17.
 */

public class MediaEditType {

    public static class Filter{
        public static final int Filter_NULL = 0;
        public static final int Filter_IFHudson = 1;
        public static final int Filter_IFLomofi = 2;
        public static final int Filter_IFSierra = 3;
        public static final int Filter_IFRise = 4;
        public static final int Filter_IFAmaro = 5;
        public static final int Filter_IFWalden = 6;
        public static final int Filter_IFNashville = 7;
        public static final int Filter_IFBrannan = 8;
        public static final int Filter_IFInkwell = 9;
        public static final int Filter_IFToaster = 10;
        public static final int Filter_IF1977 = 11;
        public static final int Filter_LanSongSepia = 12;
    }

    public static class FaceBeauty{
        public static final float LEVEL_0 = 0f;
        public static final float LEVEL_1 = 0.2f;
        public static final float LEVEL_2 = 0.4f;
        public static final float LEVEL_3 = 0.6f;
        public static final float LEVEL_4 = 0.8f;
        public static final float LEVEL_5 = 1f;
    }

    /**
     * 需求：
     *  1.  其中图片的命名方式已按照排序位置命名，效果如下图，注意：无水印排在第一位；
     *  2.  请按照命名按序排在无水印后面；
     *  3.  其中命名为3的水印，为默认水印；
     *
     * 说明
     * logo_0.webg对应需求说明1.png
     * logo_2.webg为默认水印
     */
    public static class Logo{
        public static final int LOGO_0 = 0;
        public static final int LOGO_1 = 1;
        public static final int LOGO_2 = 2;
        public static final int LOGO_3 = 3;
        public static final int LOGO_4 = 4;
        public static final int LOGO_5 = 5;
        public static final int LOGO_6 = 6;
        public static final int LOGO_7 = 7;
        public static final int LOGO_8 = 8;
        public static final int LOGO_9 = 9;
        public static final int LOGO_10 = 10;
        public static final int LOGO_11 = 11;
    }
}
