package com.k.easylearningenglishwords.data.network.YandexTranslate;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YandexTranslateResponse {

    @SerializedName("code")
    @Expose
    public int code;
    @SerializedName("lang")
    @Expose
    public String lang;
    @SerializedName("text")
    @Expose
    public List<String> text = null;

}