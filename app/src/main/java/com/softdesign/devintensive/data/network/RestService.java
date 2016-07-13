package com.softdesign.devintensive.data.network;

import com.softdesign.devintensive.data.network.restmodels.req.UserLoginReq;
import com.softdesign.devintensive.data.network.restmodels.res.UserModelRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestService {

    @POST("login")
    Call<UserModelRes> loginUser(@Body UserLoginReq req);
}
