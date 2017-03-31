package com.velychko.kyrylo.mydictionaries.data.network.yandexdictionary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YandexDictionaryResponse {
    @SerializedName("def")
    @Expose
    public List<Def> def = null;

    public class Def {
        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("tr")
        @Expose
        public List<Tr> tr = null;
    }

    public class Tr {
        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("syn")
        @Expose
        public List<Syn> syn = null;
    }

    public class Syn {
        @SerializedName("text")
        @Expose
        public String text;
    }
}
