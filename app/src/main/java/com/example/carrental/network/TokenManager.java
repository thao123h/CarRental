package com.example.carrental.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TokenManager {

    private static final String PREF_NAME = "APP_PREFS";

    private static final String TOKEN_KEY = "JWT_TOKEN";
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN";
    private static final String USERNAME_KEY = "USERNAME";
    private static final String EMAIL_KEY = "EMAIL";
    private static final String ROLES_KEY = "ROLES"; // lưu dạng comma-separated

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ==== Token ====
    public void saveToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(REFRESH_TOKEN_KEY, refreshToken).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(REFRESH_TOKEN_KEY, null);
    }

    // ==== User info ====
    public void saveUsername(String username) {
        prefs.edit().putString(USERNAME_KEY, username).apply();
    }

    public String getUsername() {
        return prefs.getString(USERNAME_KEY, null);
    }

    public void saveEmail(String email) {
        prefs.edit().putString(EMAIL_KEY, email).apply();
    }

    public String getEmail() {
        return prefs.getString(EMAIL_KEY, null);
    }

    // ==== Roles ====
    public void saveRoles(Set<String> roles) {
        String rolesString = String.join(",", roles);
        prefs.edit().putString(ROLES_KEY, rolesString).apply();
    }

    public Set<String> getRoles() {
        String rolesString = prefs.getString(ROLES_KEY, "");
        if (rolesString.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(rolesString.split(",")));
    }

    // ==== Helpers ====
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
