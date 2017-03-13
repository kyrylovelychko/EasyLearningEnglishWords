package com.k.easylearningenglishwords.data.network.YandexDictionary;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class YandexDictionaryRetrofit {

    private static final String API_KEY = "dict.1.1.20170313T104123Z.ba7a60a85e24935f.145ee166e540f050b6eb0ca5960fc79f13f849e9";
    private static final String BASE_URL = "https://dictionary.yandex.net/api/";

    private static ApiInterface apiInterface;

    interface ApiInterface {
        @POST("v1/dicservice.json/lookup")
        Call<YandexDictionaryResponse> translateWord(@Query("key") String key,
                                                     @Query("text") String text,
                                                     @Query("lang") String lang);
    }

    static {
        initialize();
    }

    private static void initialize() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static Call<YandexDictionaryResponse> translateWord(String text, String lang) {
        return apiInterface.translateWord(API_KEY, text, lang);
    }

}
