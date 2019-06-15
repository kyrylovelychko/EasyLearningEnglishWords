package com.velychko.kyrylo.mydictionaries.data.network.microsofttranslate;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class MicrosoftTranslateRetrofit {

    private static final String API_KEY = "fa6e937e0e0c48e0a62644bc1661ff52";

    private static final String BASE_URL =
            "https://api.cognitive.microsofttranslator.com/";

    private static ApiIntergace apiIntergace;

    interface ApiIntergace {
        @Headers({"Ocp-Apim-Subscription-Key: " + API_KEY,
                "Content-Type: application/json"})
        @POST("translate")
        Call<List<MicrosoftTranslateResponse>> translateText(@Query("api-version") String apiVersion,
                                                             @Query("from") String from,
                                                             @Query("to") String to,
                                                             @Body RequestBody body);
    }

    static {
        initialize();
    }

    private static void initialize() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiIntergace = retrofit.create(ApiIntergace.class);
    }

    public static Call<List<MicrosoftTranslateResponse>> translateText(String from, String to, String text) {
        RequestBody body = null;

        try {
            body = RequestBody.create(MediaType.parse("application/json"), new JSONArray(new TranslateBodyFormatted(text).toString()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return apiIntergace.translateText("3.0", from, to, body);
    }


    public static class TranslateBodyFormatted {
        final String text;

        TranslateBodyFormatted(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "[{\n\t\"Text\": \"" + text + "\"\n}]";
        }
    }

}
