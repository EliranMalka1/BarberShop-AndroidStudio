package com.example.navigtion_app.intarfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import java.util.Map;

public interface ApiService {
    @POST("exec") // נתיב הבקשה ל-Google Apps Script
    Call<ResponseBody> sendEmail(@Body Map<String, String> emailRequest);
}
