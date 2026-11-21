package com.example.quanliphim.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class PreferenceManager {

    private static final String PREF_NAME = "moviebox_prefs";

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_BIO = "user_bio";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_FAV_GENRE = "favorite_genre";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getUserName(Context context) {
        return getPrefs(context).getString(KEY_USER_NAME, "Nguyễn Văn A");
    }

    public static void setUserName(Context context, String name) {
        getPrefs(context).edit().putString(KEY_USER_NAME, name).apply();
    }

    public static String getUserEmail(Context context) {
        return getPrefs(context).getString(KEY_USER_EMAIL, "user@example.com");
    }

    public static void setUserEmail(Context context, String email) {
        getPrefs(context).edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public static String getUserPhone(Context context) {
        return getPrefs(context).getString(KEY_USER_PHONE, "0987654321");
    }

    public static void setUserPhone(Context context, String phone) {
        getPrefs(context).edit().putString(KEY_USER_PHONE, phone).apply();
    }

    public static String getUserBio(Context context) {
        return getPrefs(context).getString(KEY_USER_BIO, "Người yêu thích điện ảnh và đam mê khám phá các bộ phim hay.");
    }

    public static void setUserBio(Context context, String bio) {
        getPrefs(context).edit().putString(KEY_USER_BIO, bio).apply();
    }

    public static String getUserAvatar(Context context) {
        return getPrefs(context).getString(KEY_USER_AVATAR, "");
    }

    public static void setUserAvatar(Context context, String avatar) {
        getPrefs(context).edit().putString(KEY_USER_AVATAR, avatar).apply();
    }

    public static String getFavoriteGenre(Context context) {
        return getPrefs(context).getString(KEY_FAV_GENRE, "Hành động");
    }

    public static void setFavoriteGenre(Context context, String genre) {
        getPrefs(context).edit().putString(KEY_FAV_GENRE, genre).apply();
    }

    public static int getThemeMode(Context context) {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_DARK);
    }

    public static void setThemeMode(Context context, int mode) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
        applyTheme(mode);
    }

    public static void applySavedTheme(Context context) {
        int mode = getThemeMode(context);
        applyTheme(mode);
    }

    private static void applyTheme(int mode) {
        if (mode == THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
