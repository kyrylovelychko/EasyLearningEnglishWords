package com.velychko.kyrylo.mydictionaries.data.network.microsofttranslate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MicrosoftTranslateResponse {


        @SerializedName("translations")
        @Expose
        public List<Translation> translations = null;

    public class Translation {

        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("to")
        @Expose
        public String to;

    }
}
