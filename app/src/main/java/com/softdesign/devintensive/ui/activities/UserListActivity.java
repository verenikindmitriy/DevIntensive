package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.restmodels.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity {

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.user_list)
    RecyclerView mRecyclerView;

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;

    private List<User> mUsers;
    private MenuItem mSearchItem;
    private String mQuery;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mHandler = new Handler();

        setupToolbar();
        setupDrawer();
        loadUsersFromDb();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void loadUsersFromDb() {

        mUsers = mDataManager.getUserListFromDb();
        if (mUsers.isEmpty()) {
            showSnackBar(getString(R.string.error_user_list_can_not_be_load));
        } else {
           showUsers(mUsers);
        }
    }

    private void setupDrawer() {
        // TODO: 15.07.2016 Реализовать переход в другую активити при клике по элементу меню в NavigationDrawer 
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);

        mSearchItem = menu.findItem(R.id.search_action);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        searchView.setQueryHint(getString(R.string.search_hint_enter_user_name));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()){
                    mUsers = mDataManager.getUserListFromDb();
                    showUsers(mUsers);
                }else{
                    showUsersByQuery(newText);
                }
                return false;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void showUsers(List<User> users){
        mUsers = users;
        mUsersAdapter = new UsersAdapter(mUsers, new UsersAdapter.UserViewHolder.CustomClickListener() {
            @Override
            public void onUserItemClickListener(int position) {

                UserDTO userDTO = new UserDTO(mUsers.get(position));

                Intent profileIntent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                profileIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);

                startActivity(profileIntent);
            }
        });

        mRecyclerView.swapAdapter(mUsersAdapter, false);
    }

    private void showUsersByQuery(String query){
        mQuery = query;

        Runnable searchUsers = new Runnable() {
            @Override
            public void run() {
                showUsers(mDataManager.getUserListByName(mQuery));
            }
        };

        mHandler.removeCallbacks(searchUsers);
        mHandler.postDelayed(searchUsers, AppConfig.START_DELAY);
    }
}
