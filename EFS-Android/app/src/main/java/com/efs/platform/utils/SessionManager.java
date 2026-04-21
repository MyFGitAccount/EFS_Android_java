package com.efs.platform.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.efs.platform.models.Models;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "EFS_Session";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "user_data";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Gson gson;
    
    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }
    
    public void saveAuthToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    public String getAuthToken() {
        return pref.getString(KEY_TOKEN, null);
    }
    
    public void saveUser(Models.User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER, userJson);
        editor.apply();
    }
    
    public Models.User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, Models.User.class);
        }
        return null;
    }
    
    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }
    
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
    
    public String getAuthHeader() {
        String token = getAuthToken();
        if (token != null) {
            return "Bearer " + token;
        }
        return null;
    }
}
