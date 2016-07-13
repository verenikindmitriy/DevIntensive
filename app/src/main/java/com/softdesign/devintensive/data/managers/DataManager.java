package com.softdesign.devintensive.data.managers;


import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.restmodels.ServiceGenerator;
import com.softdesign.devintensive.data.network.restmodels.req.UserLoginReq;
import com.softdesign.devintensive.data.network.restmodels.res.UserModelRes;

import retrofit2.Call;
import retrofit2.http.Body;

public class DataManager {

    public static DataManager INSTANCE = null;


    private PreferenceManager mPreferenceManager;
    private RestService mRestService;

    public DataManager() {
        this.mPreferenceManager = new PreferenceManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
    }

    public static DataManager getInstance() {
        if (INSTANCE == null){
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    //region ================ Network ===============

    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq){
        return mRestService.loginUser(userLoginReq);
    }

    //endregion

    //region ================ DataBase ===============


    //endregion
}
