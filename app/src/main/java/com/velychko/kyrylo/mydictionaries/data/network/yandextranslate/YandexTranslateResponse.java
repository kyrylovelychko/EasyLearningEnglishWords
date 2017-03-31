package com.velychko.kyrylo.mydictionaries.data.network.yandextranslate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YandexTranslateResponse {
    @SerializedName("text")
    @Expose
    public List<String> text = null;
}