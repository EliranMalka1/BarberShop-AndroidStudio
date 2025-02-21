package com.example.navigtion_app.intarfaces;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServiceFirebase {
    @POST("v1/projects/{projectId}/accounts:delete")
    Call<Void> deleteUser(
            @Path("projectId") String projectId,
            @Body Map<String, String> requestBody,
            @Header("Authorization") String bearerToken
    );

}

