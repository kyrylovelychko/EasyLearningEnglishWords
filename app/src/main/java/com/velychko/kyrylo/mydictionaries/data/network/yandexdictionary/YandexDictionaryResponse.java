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
//        @SerializedName("pos")
//        @Expose
//        public String pos;
        @SerializedName("tr")
        @Expose
        public List<Tr> tr = null;
        @SerializedName("ts")
        @Expose
        public String ts;

    }

    public class Tr {

        @SerializedName("text")
        @Expose
        public String text;
//        @SerializedName("pos")
//        @Expose
//        public String pos;
        @SerializedName("syn")
        @Expose
        public List<Syn> syn = null;
//        @SerializedName("mean")
//        @Expose
//        public List<Mean> mean = null;
//        @SerializedName("ex")
//        @Expose
//        public List<Ex> ex = null;

    }

//    public class Ex {
//
//        @SerializedName("text")
//        @Expose
//        public String text;
//        @SerializedName("tr")
//        @Expose
//        public List<Tr_> tr = null;
//
//    }

//    public class Mean {
//
//        @SerializedName("text")
//        @Expose
//        public String text;
//
//    }

    public class Syn {

        @SerializedName("text")
        @Expose
        public String text;

    }

//    public class Tr_ {
//
//        @SerializedName("text")
//        @Expose
//        public String text;
//
//    }
}
