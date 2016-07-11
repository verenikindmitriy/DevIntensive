package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.util.Property;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.*;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Main Activity";

    private ImageView user_avatar = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    private DataManager mDataManager;

    private int mCurrentEditMode = 0;
    private Toolbar mToolbar;
    private DrawerLayout mNavigationDrawer;
    private FloatingActionButton mFab;
    private EditText mUserPhone, mUserMail, mUserVk, mUserGit, mUserBio;
    private RelativeLayout mProfilePlaceholder;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private ImageView mProfileImage;

    private List<EditText> mUserInfoViews;

    private AppBarLayout.LayoutParams mAppBarParams = null;
    private File mPhotoFile = null;
    private Uri mSelectedImage = null;

    @BindView(R.id.button_call) ImageView button_call;
    @BindView(R.id.button_send_email) ImageView button_send_email;
    @BindView(R.id.button_open_vk) ImageView button_open_vk;
    @BindView(R.id.button_open_git) ImageView button_open_git;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance();

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mProfilePlaceholder = (RelativeLayout) findViewById(R.id.profile_placeholder);
        mProfileImage = (ImageView) findViewById(R.id.user_photo_image);

        mUserPhone = (EditText) findViewById(R.id.user_phone);
        mUserMail = (EditText) findViewById(R.id.user_email);
        mUserVk = (EditText) findViewById(R.id.user_vk);
        mUserGit = (EditText) findViewById(R.id.user_git);
        mUserBio = (EditText) findViewById(R.id.user_bio);

        mUserInfoViews = new ArrayList<>();
        mUserInfoViews.add(mUserPhone);
        mUserInfoViews.add(mUserMail);
        mUserInfoViews.add(mUserVk);
        mUserInfoViews.add(mUserGit);
        mUserInfoViews.add(mUserBio);

        //маска для номера телефона
        mUserPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY, 1);
            changeEditMode(mCurrentEditMode);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View hView = navigationView.getHeaderView(0);

        user_avatar = (ImageView) hView.findViewById(R.id.user_avatar);
        if (user_avatar != null) {
            Bitmap oldAvatar = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.user_avatar);
            Bitmap roundAvatar = BitmapsManager.getRoundedBitmap(oldAvatar);

            user_avatar.setImageBitmap(roundAvatar);
        }

        mFab.setOnClickListener(this);
        mProfilePlaceholder.setOnClickListener(this);

        setupToolbar();
        setupDrawer();
        loadUserInfoValue();
        Picasso.with(this).load(mDataManager.getPreferenceManager().loadUserPhoto()).into(mProfileImage);

        Log.d(TAG, "onCreate");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Получение результата из другой Activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null){
                    mSelectedImage = data.getData();
                    insertProfileImage(mSelectedImage);
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null){
                    mSelectedImage = Uri.fromFile(mPhotoFile);
                    insertProfileImage(mSelectedImage);
                }
                else {
                    mPhotoFile.delete();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUserInfoValue();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {

        if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbarLayout.getLayoutParams();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        NavigationView.OnNavigationItemSelectedListener navigationOnClickListener = new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        };

        navigationView.setNavigationItemSelectedListener(navigationOnClickListener);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (mCurrentEditMode == 0) {
                    changeEditMode(1);
                    mCurrentEditMode = 1;
                } else {
                    changeEditMode(0);
                    mCurrentEditMode = 0;
                }
                break;

            case R.id.profile_placeholder:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;

            case R.id.button_call:
                String number = mUserPhone.getText().toString();
                if (!number.equals("") || !number.equals("null")) {

                    //проверка разрешения на звонок
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, ConstantManager.PERMISSION_CALL_REQUEST_CODE);
                    } else {
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null));
                        startActivity(callIntent);
                    }
                }
                break;

            case R.id.button_send_email:
                String email = mUserMail.getText().toString();
                if (!email.equals("") || !email.equals("null")) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
                    startActivity(Intent.createChooser(emailIntent, "Отправка письма..."));
                }
                break;

            case R.id.button_open_vk:
                String vkUrl = mUserVk.getText().toString();
                if (!vkUrl.equals("") || !vkUrl.equals("null")) {
                    Intent vkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/" + vkUrl));
                    startActivity(vkIntent);
                }
                break;

            case R.id.button_open_git:
                String gitUrl = mUserGit.getText().toString();
                if (!gitUrl.equals("") || !gitUrl.equals("null")) {
                    Intent gitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/" + gitUrl));
                    startActivity(gitIntent);
                }
                break;
        }
    }

    /**
     * переключает режим редактирования
     *
     * @param mode если 1 - режим редактирования, если 0 - режим просмотра
     */
    private void changeEditMode(int mode) {
        if (mode == 1) {
            mFab.setImageResource(R.drawable.ic_done_white_24dp);
            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(true);
                userValue.setFocusable(true);
                userValue.setFocusableInTouchMode(true);

                if (userValue == mUserPhone) {
                    userValue.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mUserPhone, InputMethodManager.SHOW_IMPLICIT);
                }

                showProfilePlaceholder();
                lockToolbar();

                mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
            }
        } else {
            mFab.setImageResource(R.drawable.ic_create_white_24dp);
            for (EditText userValue : mUserInfoViews) {
                userValue.setEnabled(false);
                userValue.setFocusable(false);
                userValue.setFocusableInTouchMode(false);

                hideProfilePlaceholder();
                unlockToolbar();

                mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.white));

                saveUserInfoValue();
            }
        }
    }

    private void loadUserInfoValue() {
        List<String> userData = mDataManager.getPreferenceManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfoViews.get(i).setText(userData.get(i));
        }
    }

    private void saveUserInfoValue() {
        List<String> userData = new ArrayList<>();
        for (EditText userFieldView : mUserInfoViews) {
            userData.add(userFieldView.getText().toString());
        }
        mDataManager.getPreferenceManager().saveUserProfileData(userData);
    }

    private void loadPhotoFromGallery(){
        Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeGalleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(takeGalleryIntent, getString(R.string.chose_photo)), ConstantManager.REQUEST_GALLERY_PICTURE);

    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = createdImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.PERMISSION_CAMERA_REQUEST_CODE);
        }
    }

    private void hideProfilePlaceholder(){
        mProfilePlaceholder.setVisibility(View.GONE);
    }

    private void showProfilePlaceholder(){
        mProfilePlaceholder.setVisibility(View.VISIBLE);
    }

    private void lockToolbar(){
        mAppBarLayout.setExpanded(true, true);
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbarLayout.setLayoutParams(mAppBarParams);
    }

    private void unlockToolbar(){
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbarLayout.setLayoutParams(mAppBarParams);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String [] selectItems = {getString(R.string.photo_load_from_gallery), getString(R.string.photo_from_camera), getString(R.string.cancel)};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.change_profile_photo));
                builder.setItems(selectItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choiceItem) {
                        switch (choiceItem){
                            case 0:
                                loadPhotoFromGallery();
                                break;
                            case 1:
                                loadPhotoFromCamera();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }

                    }
                });

                return builder.create();
            default:
                return null;
        }
    }

    private File createdImageFile() throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    private void insertProfileImage(Uri selectedImage) {

        Picasso.with(this).load(selectedImage).into(mProfileImage);

        mDataManager.getPreferenceManager().saveUserPhoto(selectedImage);

    }

    /**
     * Обрабатывает ответ о получении разрешений
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.PERMISSION_CAMERA_REQUEST_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                loadPhotoFromCamera();
            }
        }

        if (requestCode == ConstantManager.PERMISSION_CALL_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClick(ButterKnife.findById(this, R.id.button_call));
            }
        }
    }
}
