package com.example.custom.dependency;


import android.os.Parcel;
import android.os.Parcelable;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Resource implements Parcelable,Cloneable{

    private String name = "";
    private String extension;
    private int resourceType;
    private int storageType;
    private int numberOfGroupItems;

    private String time;
    private Date creationTime;
    private long order;
    private String dir;
    private String groupKey;
    private String downloadUrl;
    private String thumbPath;
    private String videoUrl;
    private boolean selected;
    private boolean downloaded;
    private int downloadProgress;
    private String serialNumber;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd ");
    private String resolution;

    /**
     * MediaEdit
     * 滤镜、美颜、logo的参数
     */
    private int filterType;
    private float beautyLevel;
    private int logoNumber;
    /**
     * Resource更新的version
     */
    private int version;

    /**
     * 水平矫正
     */
    private String skewCorrectionParameter;//水平矫正参数
    private boolean isAutoSkewCorrectionOpen;//是否开启自动水平矫正

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String shareUploadFieldId;

    public Resource() {
    }

    public Resource(File file) {
        this.name = file.getName();
        this.downloadUrl = file.getAbsolutePath();
        this.storageType = ResourceStorageType.Local;
        this.extension = FilenameUtils.getExtension(this.getName());
        if (this.getGroupKey() == null) {
            this.setGroupKey(dateFormat.format(new Date(file.lastModified())));
        }
        if (this.getExtension().equals("mp4")) {
            this.setDir("video");
            this.setVideoUrl(file.getAbsolutePath());
            this.setResourceType(ResourceType.Video);
        }
        if (this.getExtension().equals("jpg")) {
            this.setDir("picture");
            this.setResourceType(ResourceType.Picture);
        }
    }

    public static String getBundleKey() {
        return "Resource";
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getStorageType() {
        return storageType;
    }

    public void setStorageType(int storageType) {
        this.storageType = storageType;
    }

    public int getNumberOfGroupItems() {
        return numberOfGroupItems;
    }

    public void setNumberOfGroupItems(int numberOfGroupItems) {
        this.numberOfGroupItems = numberOfGroupItems;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() {
        return resolution;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.extension);
        dest.writeInt(this.resourceType);
        dest.writeInt(this.storageType);
        dest.writeInt(this.numberOfGroupItems);
        dest.writeLong(this.creationTime != null ? this.creationTime.getTime() : -1);
        dest.writeLong(this.order);
        dest.writeString(this.dir);
        dest.writeString(this.groupKey);
        dest.writeString(this.downloadUrl);
        dest.writeString(this.videoUrl);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.downloaded ? (byte) 1 : (byte) 0);
        dest.writeInt(this.downloadProgress);
        dest.writeString(this.serialNumber);
        dest.writeString(this.resolution);

        dest.writeInt(filterType);
        dest.writeFloat(beautyLevel);
        dest.writeInt(logoNumber);

        dest.writeInt(version);

        dest.writeString(skewCorrectionParameter);
        dest.writeByte((byte) (isAutoSkewCorrectionOpen ? 1 : 0));
    }

    protected Resource(Parcel in) {
        this.name = in.readString();
        this.extension = in.readString();
        this.resourceType = in.readInt();
        this.storageType = in.readInt();
        this.numberOfGroupItems = in.readInt();
        long tmpCreationTime = in.readLong();
        this.creationTime = tmpCreationTime == -1 ? null : new Date(tmpCreationTime);
        this.order = in.readLong();
        this.dir = in.readString();
        this.groupKey = in.readString();
        this.downloadUrl = in.readString();
        this.videoUrl = in.readString();
        this.selected = in.readByte() != 0;
        this.downloaded = in.readByte() != 0;
        this.downloadProgress = in.readInt();
        this.serialNumber = in.readString();
        this.resolution = in.readString();

        this.filterType = in.readInt();
        this.beautyLevel = in.readFloat();
        this.logoNumber = in.readInt();

        this.version = in.readInt();

        this.skewCorrectionParameter = in.readString();
        this.isAutoSkewCorrectionOpen = in.readByte() == 1 ? true : false;
    }

    public static final Creator<Resource> CREATOR = new Creator<Resource>() {
        @Override
        public Resource createFromParcel(Parcel source) {
            return new Resource(source);
        }

        @Override
        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };


    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof Resource) && this.getName().equals(((Resource) obj).getName());
    }

    @Override
    public Resource clone() throws CloneNotSupportedException {
        return (Resource) super.clone();
    }

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public float getBeautyLevel() {
        return beautyLevel;
    }

    public void setBeautyLevel(float beautyLevel) {
        this.beautyLevel = beautyLevel;
    }

    public int getLogoNumber() {
        return logoNumber;
    }

    public void setLogoNumber(int logoNumber) {
        this.logoNumber = logoNumber;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getSkewCorrectionParameter() {
        return skewCorrectionParameter;
    }

    public void setSkewCorrectionParameter(String skewCorrectionParameter) {
        this.skewCorrectionParameter = skewCorrectionParameter;
    }

    public boolean isAutoSkewCorrectionOpen() {
        return isAutoSkewCorrectionOpen;
    }

    public void setAutoSkewCorrectionOpen(boolean autoSkewCorrectionOpen) {
        isAutoSkewCorrectionOpen = autoSkewCorrectionOpen;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", resourceType=" + resourceType +
                ", storageType=" + storageType +
                ", numberOfGroupItems=" + numberOfGroupItems +
                ", time='" + time + '\'' +
                ", creationTime=" + creationTime +
                ", order=" + order +
                ", dir='" + dir + '\'' +
                ", groupKey='" + groupKey + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", thumbPath='" + thumbPath + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", selected=" + selected +
                ", downloaded=" + downloaded +
                ", downloadProgress=" + downloadProgress +
                ", serialNumber='" + serialNumber + '\'' +
                ", resolution='" + resolution + '\'' +
                ", filterType=" + filterType +
                ", beautyLevel=" + beautyLevel +
                ", logoNumber=" + logoNumber +
                ", version=" + version +
                ", skewCorrectionParameter='" + skewCorrectionParameter + '\'' +
                ", isAutoSkewCorrectionOpen=" + isAutoSkewCorrectionOpen +
                ", shareUploadFieldId='" + shareUploadFieldId + '\'' +
                '}';
    }
}
