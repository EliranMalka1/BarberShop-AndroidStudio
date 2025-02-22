package com.example.navigtion_app.models;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.Executors;

public class FirebaseAdminHelper {

    private static FirebaseAdminHelper instance;
    private AccessToken token;
    private long tokenExpirationTime;
    private final Context context;

    public interface TokenCallback {
        void onTokenReady(String token);
    }

    // בנאי פרטי למניעת יצירת מופעים נוספים (Singleton)
    private FirebaseAdminHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    // יצירת אינסטנס יחיד של המחלקה (Singleton)
    public static synchronized FirebaseAdminHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAdminHelper(context);
        }
        return instance;
    }

    // קבלת טוקן, תוך בדיקה אם צריך לרענן
    public void getAdminToken(TokenCallback callback) {
        if (token == null || System.currentTimeMillis() > tokenExpirationTime) {
            refreshToken(callback);
        } else {
            if (callback != null) {
                callback.onTokenReady("Bearer " + token.getTokenValue());
            }
        }
    }

    // רענון הטוקן אם פג תוקפו
    private void refreshToken(TokenCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // בדיקה אם הקובץ קיים
                InputStream serviceAccount = context.getAssets().open("app-data-bd40e-559adbf7994f.json");

                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/identitytoolkit")); // ✅ שונה ל-Scope הנכון

                credentials.refreshIfExpired();
                token = credentials.getAccessToken();

                if (token == null) {
                    Log.e("FirebaseAdminHelper", "🚨 Failed to obtain access token.");
                    callback.onTokenReady(null);
                    return;
                }

                // ✅ הדפסת ה-Admin Token כדי לוודא שהוא מופק
                Log.d("FirebaseAdminHelper", "✅ Generated Admin Token: " + token.getTokenValue());

                // שליחת ה-Token חזרה לקריאה
                callback.onTokenReady(token.getTokenValue());

            } catch (IOException e) {
                Log.e("FirebaseAdminHelper", "🚨 Service account JSON file not found!", e);
                callback.onTokenReady(null);
            }
        });
    }




}

