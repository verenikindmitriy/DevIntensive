package com.softdesign.devintensive.data.managers;


import android.content.Context;

import com.softdesign.devintensive.data.network.PicassoCache;
import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.restmodels.ServiceGenerator;
import com.softdesign.devintensive.data.network.restmodels.req.UserLoginReq;
import com.softdesign.devintensive.data.network.restmodels.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.DevIntensiveApp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class DataManager {

    private static DataManager INSTANCE = null;
    private Picasso mPicasso;
    private Context mContext;


    private PreferenceManager mPreferenceManager;
    private RestService mRestService;

    private DaoSession mDaoSession;

    public DataManager() {
        this.mPreferenceManager = new PreferenceManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
        this.mContext = DevIntensiveApp.getContext();
        this.mPicasso = new PicassoCache(mContext).getPicassoInstance();
        this.mDaoSession = DevIntensiveApp.getDaoSession();
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

    public Picasso getPicasso() {
        return mPicasso;
    }

    //region ================ Network ===============

    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq){
        return mRestService.loginUser(userLoginReq);
    }

    public Call<UserListRes> getUserListFromNetwork(){
        return mRestService.getUserList();
    }

    //endregion

    //region ================ DataBase ===============


    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public List<User> getUserListFromDb(){
        List<User> userList = new ArrayList<>();

        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.CodeLines.gt(0))
                    .orderDesc(UserDao.Properties.CodeLines)
                    .build()
                    .list();

        }catch (Exception e){
            e.printStackTrace();
        }
        return userList;
    }

    public  List<User> getUserListByName(String query){

        List<User> userList = new ArrayList<>();
        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.Rating.gt(0), UserDao.Properties.SearchName.like("%"+query.toUpperCase()+"%"))
                    .orderDesc(UserDao.Properties.CodeLines)
                    .build()
                    .list();
        }catch (Exception e){
            e.printStackTrace();
        }

        return  userList;
    }

    //endregion
}
