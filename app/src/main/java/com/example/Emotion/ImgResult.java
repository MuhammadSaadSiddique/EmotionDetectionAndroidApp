package com.example.Emotion;

import com.google.gson.annotations.SerializedName;

public class ImgResult {
    @SerializedName("confidence")
    int confidence;
    @SerializedName("class")
    String classres;
}
