package com.velychko.kyrylo.mydictionaries.data.network.yandextranslate;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class YandexTranslateRetrofit {

    private static final String API_KEY = "trnsl.1.1.20170312T185630Z.999885ba056dff88.c6b112b4cab8c824c4616ba1deb5f3e8ca6c1f1e";
    private static final String BASE_URL = "https://translate.yandex.net/api/";

    private static ApiInterface apiInterface;

    interface ApiInterface {
        @POST("v1.5/tr.json/translate")
        Call<YandexTranslateResponse> translateText(@Query("key") String key,
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

    public static Call<YandexTranslateResponse> translateText(String text, String lang) {
        return apiInterface.translateText(API_KEY, text, lang);
    }

}
