package com.adevani.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adevani.mytube.MainActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

/**
 * Created by ankitdevani on 10/17/15.
 */
public class FetchToken extends AsyncTask {

    public static final String TAG = FetchToken.class.getSimpleName();
    String email;
    Context context;
    private final static String YOUTUBE_API_SCOPE = "https://www.googleapis.com/auth/youtube";
    private final static String mScopes = "oauth2:" + YOUTUBE_API_SCOPE;

    public FetchToken(String email, Context context) {
        this.email = email;
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            MainActivity.FETCH_TOKEN = getFetchToken();
            Log.d(TAG, "Token " + MainActivity.FETCH_TOKEN);
        } catch (IOException e) {
            Log.e(TAG, "Exception" + e.getMessage());
        }
        return null;
    }

    private String getFetchToken() throws IOException {
        String token = "";
        try {
            token = GoogleAuthUtil.getToken(context, email, mScopes);
        } catch (UserRecoverableAuthException userRecoverableException) {
            Log.e(TAG, "Exception - " + userRecoverableException.getMessage());
        } catch (GoogleAuthException fatalException) {
            Log.e(TAG, "Exception - " + fatalException.getMessage());
        }
        return token;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        PlaylistRequests.listAllPlaylist(context);
    }
}