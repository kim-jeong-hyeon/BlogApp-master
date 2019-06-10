package com.example.aws.blogapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aws.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {


    ImageView ImgUserPhoto;
    static int PReqCode = 1 ;
    static int REQUESCODE = 1 ;
    Uri pickedImgUri ;

    private EditText userEmail,userPassword,userPAssword2,userName;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ini views
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPAssword2 = findViewById(R.id.regPassword2);
        userName = findViewById(R.id.regName);
        loadingProgress = findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);


        mAuth = FirebaseAuth.getInstance();


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPAssword2.getText().toString();
                final String name = userName.getText().toString();

                if( email.isEmpty() || name.isEmpty() || password.isEmpty()  || !password.equals(password2)) {

                    // 모든 값들이 다 입려되어야 한다
                    // 값을 채우지 않았을 경우에 메시 출력
                    showMessage("모든 값을 다 채워 주세요.") ;
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }
                else {
                    // 모든 필드가 채워져야 계정을 새로 생성할 수 있다.
                    //
                    //이메일이 중복되지 않ㅇ르 경우만 새로 만들 수 있음
                    CreateUserAccount(email,name,password);
                }

            }
        });

        ImgUserPhoto = findViewById(R.id.regUserPhoto) ;//사용자 프로필 사진 설정하기

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {//프로필 사진 버튼을 누르면
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 22) {//SDK버전이 22 이상일 경우에 퍼미션 요청

                    checkAndRequestForPermission();


                }
                else
                {
                    openGallery();//그이하는 바로 갤러리에 접근함
                }


            }
        });


    }

    private void CreateUserAccount(String email, final String name, String password) {

        // 유저의 계정을 만들기 특정 이메일과 패스워드로
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //아이디가 올바르게 만들어졌을 경우
                            showMessage("계정 생성 완료");
                            //유저의 아이디가 만들어 진후 프로필 사진과 이름을 업데이트 해야한다.
                            updateUserInfo( name ,pickedImgUri,mAuth.getCurrentUser());

                        }
                        else
                        {
                            // 아이디 생성실패
                            showMessage("계정 생성 실패" + task.getException().getMessage());
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);

                        }
                    }
                });





    }


    // 유저의 사진과 이름을 업데이트

    /**
     *
     * 유저의 이름 ,선택된 Uri , 현재 유저정보
     */
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {

        // first we need to upload user photo to firebase storage and get url
        //첫번째로 firebase storage에 유저의 사진을 업로드 하고 url을 얻는다.

        //파이어 스토리지에 "users_photos"가 없다면 를 만들고 있다면 그 하위에 사진을 올린다.
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");

        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());

        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // 이미지가 성공적으로 업로드 되었다.
                // 이제 url을 얻어올 수가 있다.
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url
                        //유저 프로필 업데이트 파이베이스 제공
                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)//사용자 사진주소 URL값
                                .build();

                        //firebase 기본제공 메소드로 사용자 개인 프로 설정이 가능하다.
                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // 유저의 정보가 성공적으로 입력됨
                                           showMessage("회원가입 완료!");
                                           updateUI();
                                        }

                                    }
                                });

                    }
                });





            }
        });






    }

    private void updateUI() {//회원가입을 완료하고 ->Home로 인덴트

        Intent homeActivity = new Intent(getApplicationContext(),Home.class);
        startActivity(homeActivity);
        finish();

    }

    // simple method to show toast message
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    //갤러리를 열어 ("image/*") 하여 모든 사진을 다가져옴

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }

    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this,"Please accept for required permission",Toast.LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                    PReqCode);
            }

        }
        else
            openGallery();

    }

    //사진이 올바르게 선택되 었을 경우에 실행된다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {

            // 성공적으로 이미지를 선택했을 경우에
            // Uri를 변수에 저장한다.
            pickedImgUri = data.getData() ;//사진의 uri를 저장
            ImgUserPhoto.setImageURI(pickedImgUri);//이미지뷰에 유알아이를 넣어 사진을 뿌려줌

        }


    }
}
