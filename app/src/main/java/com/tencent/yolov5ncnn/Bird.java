package com.tencent.yolov5ncnn;

import java.util.List;

public class Bird {
    private String id;
    private String blood;
    private List<Float> feature;
    float similarity = 0;

    // 构造方法(只用两个因为页面展示时只需要id和blood,feature用来计算就行）
    public Bird(String id, String blood) {
        this.id = id;
        this.blood = blood;
    }
    // 获取鸟的ID
    public String getId() {return id;}
    // 获取鸟的血型
    public String getBlood() {
        return blood;
    }
    public float getSimilarity(){return similarity;}
    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }
}
