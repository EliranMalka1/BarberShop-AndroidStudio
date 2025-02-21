package com.example.navigtion_app.intarfaces;

import com.example.navigtion_app.models.EmailRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("exec") // שביל הבקשה
    Call<Void> sendEmail(@Body EmailRequest emailRequest);
}

