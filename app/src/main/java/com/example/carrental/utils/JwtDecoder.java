package com.example.carrental.utils;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to decode JWT token and extract user information
 */
public class JwtDecoder {

    /**
     * Decode JWT token and extract user ID
     * @param token JWT token string
     * @return User ID or null if token is invalid
     */
    public static Long getUserId(String token) {
        try {
            JSONObject payload = getPayload(token);
            if (payload != null && payload.has("id")) {
                return payload.getLong("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decode JWT token and extract roles
     * @param token JWT token string
     * @return List of roles or empty list if token is invalid
     */
    public static List<String> getRoles(String token) {
        List<String> roles = new ArrayList<>();
        try {
            JSONObject payload = getPayload(token);
            if (payload != null && payload.has("roles")) {
                JSONArray rolesArray = payload.getJSONArray("roles");
                for (int i = 0; i < rolesArray.length(); i++) {
                    roles.add(rolesArray.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Check if user has RENTER role
     */
    public static boolean hasRenterRole(String token) {
        return getRoles(token).contains("RENTER");
    }

    /**
     * Check if user has OWNER role
     */
    public static boolean hasOwnerRole(String token) {
        return getRoles(token).contains("OWNER");
    }

    /**
     * Check if user has both RENTER and OWNER roles
     */
    public static boolean hasBothRoles(String token) {
        List<String> roles = getRoles(token);
        return roles.contains("RENTER") && roles.contains("OWNER");
    }

    /**
     * Get username from token
     */
    public static String getUsername(String token) {
        try {
            JSONObject payload = getPayload(token);
            if (payload != null && payload.has("sub")) {
                return payload.getString("sub");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decode JWT token and get payload as JSONObject
     * JWT format: header.payload.signature
     */
    private static JSONObject getPayload(String token) {
        try {
            // Split token into parts
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            // Decode payload (second part)
            String payload = parts[1];

            // Base64 decode
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            String decodedString = new String(decodedBytes, "UTF-8");

            // Parse JSON
            return new JSONObject(decodedString);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if token is valid (not null and has proper format)
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3; // JWT has 3 parts: header.payload.signature
    }
}