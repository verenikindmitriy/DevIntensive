package com.softdesign.devintensive.data.managers;


import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApp;

import java.util.ArrayList;
import java.util.List;

public class PreferenceManager {

    private SharedPreferences mSharedPreferences;

    private static final String[] USER_FIELDS = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_MAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GIT_KEY,
            ConstantManager.USER_BIO_KEY};

    private static final String[] USER_VALUES ={
            ConstantManager.USER_RATING_VALUE,
            ConstantManager.USER_CODE_LINES_VALUE,
            ConstantManager.USER_PROJECT_VALUE,
    };

    public PreferenceManager() {
        this.mSharedPreferences = DevIntensiveApp.getSharedPreferences();
    }

    public void saveUserProfileData(List<String> userFields) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_FIELDS.length; i++) {
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }
        editor.apply();
    }

    public List<String> loadUserProfileData() {
        List<String> userFields = new ArrayList<>();
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_PHONE_KEY, ""));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_MAIL_KEY, ""));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_VK_KEY, ""));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_GIT_KEY, ""));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_BIO_KEY, ""));

        return userFields;
    }

    public void saveUserPhoto(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PHOTO_KEY, uri.toString());
        editor.apply();
    }

    public Uri loadUserPhoto() {
        String imageUri = mSharedPreferences.getString(ConstantManager.USER_PHOTO_KEY,
                "android.resource://com.softdesign.devintensive/drawable/user_bg");
        return Uri.parse(imageUri);
    }

    public void saveAuthToken(String authToken) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.AUTH_TOKEN_KEY, authToken);
        editor.apply();
    }

    public String getAuthToken(){
        return mSharedPreferences.getString(ConstantManager.AUTH_TOKEN_KEY, "null");
    }

    public void saveUserId(String userId){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_ID_KEY, userId);
        editor.apply();
    }

    public String getUserId(){
        return mSharedPreferences.getString(ConstantManager.USER_ID_KEY, "null");
    }

    public void saveUserProfileValues(int[] userValues){
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_VALUES.length; i++) {
            editor.putString(USER_VALUES[i], String.valueOf(userValues[i]));
        }
        editor.apply();
    }

    public void saveHeaderInfoValues(String firstName, String secondName){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_FIRST_NAME_VALUE, firstName);
        editor.putString(ConstantManager.USER_SECOND_NAME_VALUE, secondName);
        editor.apply();
    }

    public List<String> loadUserProfileValues(){
        List<String> userValues = new ArrayList<>();
        userValues.add(mSharedPreferences.getString(ConstantManager.USER_RATING_VALUE, "0"));
        userValues.add(mSharedPreferences.getString(ConstantManager.USER_CODE_LINES_VALUE, "0"));
        userValues.add(mSharedPreferences.getString(ConstantManager.USER_PROJECT_VALUE, "0"));

        return userValues;
    }

    public List<String> loadHeaderInfoData() {
        List<String> headerInfo = new ArrayList<>();

        String firstName = mSharedPreferences.getString(ConstantManager.USER_FIRST_NAME_VALUE, "");
        String fullName = firstName.concat(" ").concat(mSharedPreferences.getString(ConstantManager.USER_SECOND_NAME_VALUE, ""));

        headerInfo.add(fullName);
        headerInfo.add(mSharedPreferences.getString(ConstantManager.USER_MAIL_KEY, ""));

        return headerInfo;
    }

    /**
     * Сохранение аватарки профиля
     *
     * @param uri
     */
    public void saveUserAvatar(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_AVATAR_KEY, uri.toString());
        editor.apply();
    }

    /**
     * Получение аватарки профиля
     *
     * @return
     */
    public Uri loadUserAvatar() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_AVATAR_KEY, ""));
    }
}
