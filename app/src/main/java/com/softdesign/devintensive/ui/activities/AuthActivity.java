package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.restmodels.req.UserLoginReq;
import com.softdesign.devintensive.data.network.restmodels.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";
    @BindViews({R.id.auth_login, R.id.auth_password})
    List<EditText> mAuthFields;

    @BindView(R.id.auth_button)
    Button mAuthButton;

    @OnClick(R.id.auth_button)
    public void onSingInClick() {

        if (NetworkStatusChecker.isNetworkAvailable(this)) {

            String login = mAuthFields.get(0).getText().toString();
            String password = mAuthFields.get(1).getText().toString();

            Call<UserModelRes> call = mDataManager.loginUser(new UserLoginReq(login, password));
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        loginSuccess(response.body());
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    // TODO: 12.07.2016 Обработать ошибки ретрофита
                }
            });

        } else {
            // TODO: 12.07.2016 вывести сообщение - нет подключения
        }
    }

    @BindView(R.id.forgot_pass)
    TextView mForgotPass;

    @OnClick(R.id.forgot_pass)
    public void onForgotPassClick() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private DataManager mDataManager;
    private RepositoryDao mRepositoryDao;
    private UserDao mUserDao;

    private final Handler handler = new Handler();
    private final Runnable splashTask = new Runnable() {
        @Override
        public void run() {
            saveUserInDb();
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        mUserDao = mDataManager.getDaoSession().getUserDao();
        mRepositoryDao = mDataManager.getDaoSession().getRepositoryDao();


    }

    public void loginSuccess(UserModelRes userModel){

        mDataManager.getPreferenceManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferenceManager().saveUserId(userModel.getData().getUser().getId());
        saveUserValues(userModel);

        List<String> userProfileValues = new ArrayList<>();
        userProfileValues.add(userModel.getData().getUser().getContacts().getPhone());
        userProfileValues.add(userModel.getData().getUser().getContacts().getEmail());
        userProfileValues.add(userModel.getData().getUser().getContacts().getVk());
        userProfileValues.add(userModel.getData().getUser().getRepositories().getFirstRepozitory());
        userProfileValues.add(userModel.getData().getUser().getPublicInfo().getBio());

        mDataManager.getPreferenceManager().saveUserProfileData(userProfileValues);

        mDataManager.getPreferenceManager().saveUserAvatar(Uri.parse(userModel.getData().getUser().getPublicInfo().getAvatar()));
        mDataManager.getPreferenceManager().saveUserPhoto(Uri.parse(userModel.getData().getUser().getPublicInfo().getPhoto()));

        mDataManager.getPreferenceManager().saveHeaderInfoValues(userModel.getData().getUser().getFirstName(), userModel.getData().getUser().getSecondName());

        //handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(AuthActivity.this, UserListActivity.class);
                startActivity(loginIntent);
            }
        }, AppConfig.START_DELAY);

        handler.removeCallbacks(splashTask);
        handler.postDelayed(splashTask, AppConfig.START_DELAY);

    }

    private void saveUserValues(UserModelRes userModel){
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects(),
        };

        mDataManager.getPreferenceManager().saveUserProfileValues(userValues);
    }

    private void saveUserInDb(){
        Call<UserListRes> call = mDataManager.getUserListFromNetwork();

        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {

                if (response.code() == 200) {

                    List<Repository> allRepositories = new ArrayList<Repository>();
                    List<User> allUsers = new ArrayList<User>();

                    for (UserListRes.UserData userRes : response.body().getData()) {
                        allRepositories.addAll(getRepoListFromUserRes(userRes));
                        allUsers.add(new User(userRes));
                    }

                    mRepositoryDao.insertOrReplaceInTx(allRepositories);
                    mUserDao.insertOrReplaceInTx(allUsers);

                }else{
                    Log.e(TAG, "onResponce: " + String.valueOf(response.errorBody().source()));
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                // TODO: 15.07.2016 Обработать ошибки
            }
        });
    }

    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData){
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(userId, repositoryRes));
        }

        return repositories;
    }
}
