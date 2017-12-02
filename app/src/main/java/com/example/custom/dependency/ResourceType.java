package com.example.custom.dependency;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ResourceType {

    @IntDef({Video, Picture, UnSplice, Group, Empty, ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResourceTypeMode {
    }

    public static final int Video = 1 << 1;
    public static final int Picture = 1 << 2;
    public static final int UnSplice = 1 << 3;
    public static final int Group = 1 << 4;
    public static final int Empty = 1 << 5;
    public static final int ALL = Video | Picture | UnSplice | Group;
}
