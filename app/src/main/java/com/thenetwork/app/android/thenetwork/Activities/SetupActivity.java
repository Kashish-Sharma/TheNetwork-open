package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import id.zelory.compressor.Compressor;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thenetwork.app.android.thenetwork.Manifest;
import com.thenetwork.app.android.thenetwork.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int READCODE = 1;
    private static final int WRITECODE = 2;

    private Uri mainImageUri = null;

    private CircleImageView setupImage;
    private EditText setupName;
    private TextView changePhoto;
    private EditText setupSkills;
    private ImageView setupButton;
    private ProgressBar setupProgress;
    private Boolean isChanged = false;
    private String current_user_id;
    private EditText aboutYou;
    private EditText emailUser;
    private EditText phoneNumber;
    private Bitmap compressedUserImage;
    private Toolbar mToolbar;

    String user_id = "";

    //firebase
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseFirestoreSettings settings;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    private String intentThatStartedThisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.LoginTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mToolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Setup account");
        actionBar.setElevation(10.0f);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        firebaseDatabase = FirebaseDatabase.getInstance();


        setupName = findViewById(R.id.setup_name);
        setupButton = findViewById(R.id.setup_button);
        setupProgress = findViewById(R.id.setup_progress);
        setupImage = findViewById(R.id.setup_image);
        setupSkills = findViewById(R.id.setup_skill);
        changePhoto = findViewById(R.id.changeProfilePhoto);
        aboutYou = findViewById(R.id.aboutyou);
        emailUser = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phoneNumber);


        intentThatStartedThisActivity = getIntent().getStringExtra("whichState");

        if(TextUtils.isEmpty(intentThatStartedThisActivity)){
            intentThatStartedThisActivity = "not_setup";
        }


        setupProgress.setVisibility(View.VISIBLE);
        setupButton.setEnabled(false);


        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        String skills = task.getResult().getString("skills");
                        String about = task.getResult().getString("about");
                        String phone = task.getResult().getString("phone");

                        mainImageUri = Uri.parse(image);

                        setupName.setText(name);
                        setupSkills.setText(skills);
                        aboutYou.setText(about);
                        phoneNumber.setText(phone);
                        emailUser.setText(firebaseAuth.getCurrentUser().getEmail());

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest = placeholderRequest.placeholder(R.drawable.default_pic);
                        Crashlytics.log(1,"GLIDE","Before setting setupActivity pic");
                        Glide.with(getApplicationContext())
                                .setDefaultRequestOptions(placeholderRequest)
                                .load(image)
                                .into(setupImage);
                        Crashlytics.log(1,"GLIDE","After setting setupActivity pic");
                    }

                } else {
                    Snackbar.make(findViewById(R.id.setup_layout),task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                }
                setupProgress.setVisibility(View.INVISIBLE);
                setupButton.setEnabled(true);
            }
        });

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String userName = setupName.getText().toString();
                final String userSkills = setupSkills.getText().toString();
                final String userAbout = aboutYou.getText().toString();
                final String userPhone = phoneNumber.getText().toString();
                final String userEmail = emailUser.getText().toString();

                if (!TextUtils.isEmpty(userName) &&mainImageUri!=null && !TextUtils.isEmpty(userSkills)) {
                setupProgress.setVisibility(View.VISIBLE);
                if (isChanged){

                    File newImageFile = new File(mainImageUri.getPath());

                    try {
                        compressedUserImage = new Compressor(SetupActivity.this)
                                .setQuality(60)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedUserImage.compress(Bitmap.CompressFormat.JPEG,60,baos);
                    byte[] imageData = baos.toByteArray();

                    final StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                    image_path.putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                storeFirestore(task, userName,userSkills,userAbout,userPhone,userEmail,image_path);

                            } else {
                                Snackbar.make(findViewById(R.id.setup_layout), task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                setupProgress.setVisibility(View.INVISIBLE);
                            }

                        }
                    });

                } else {
                    storeFirestore(null,userName,userSkills,userAbout,userPhone,userEmail,null);
                    }
                } else {
                    Snackbar.make(findViewById(R.id.setup_layout), "Photo, name, email, skills and about are required", Snackbar.LENGTH_SHORT).show();
                }

            }
        });



        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED){
                        Snackbar.make(findViewById(R.id.setup_layout),"Please grant permissions",Snackbar.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},READCODE);
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITECODE);

                    } else {

                        bringImagePicker();
                    }

                } else {
                    bringImagePicker();
                }

            }
        });

    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, String userName,
                                String userSkills,String userAbout,String userPhone,
                                String userEmail,StorageReference image_path){



        if (TextUtils.isEmpty(userPhone)){
            userPhone = "";
        }
        if (TextUtils.isEmpty(userName)){
            userName = "";
        }
        if (TextUtils.isEmpty(userSkills)){
            userSkills = "";
        }
        if (TextUtils.isEmpty(userAbout)){
            userAbout = "";
        }
        if (TextUtils.isEmpty(userEmail)){
            userEmail = firebaseAuth.getCurrentUser().getEmail();
        }

        if (image_path!=null){
            final String finalUserName = userName;
            final String finalUserSkills = userSkills;
            final String finalUserAbout = userAbout;
            final String finalUserPhone = userPhone;
            final String finalUserEmail = userEmail;
            image_path.getDownloadUrl().addOnSuccessListener(SetupActivity.this, new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("user_id",user_id);
                    userMap.put("name", finalUserName);
                    userMap.put("skills", finalUserSkills);
                    userMap.put("about", finalUserAbout);
                    userMap.put("phone", finalUserPhone);
                    userMap.put("email", finalUserEmail);
                    userMap.put("image",uri.toString());


                    firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Snackbar.make(findViewById(R.id.setup_layout),"Settings updated",Snackbar.LENGTH_SHORT).show();
                                Log.i("CHECK",intentThatStartedThisActivity);
                                sendToMain(intentThatStartedThisActivity);

                            } else {

                                Snackbar.make(findViewById(R.id.setup_layout),task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();

                            }
                            databaseReference = firebaseDatabase.getReference();
                            databaseReference.child("Users").child(user_id).child("online").setValue(false);
                            setupProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            });
        } else {

            Map<String, String> userMap = new HashMap<>();
            userMap.put("user_id",user_id);
            userMap.put("name",userName);
            userMap.put("skills",userSkills);
            userMap.put("about",userAbout);
            userMap.put("phone",userPhone);
            userMap.put("email",userEmail);
            userMap.put("image",mainImageUri.toString());


            firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Snackbar.make(findViewById(R.id.setup_layout),"Settings updated",Snackbar.LENGTH_SHORT).show();
                        Log.i("CHECK",intentThatStartedThisActivity);
                        sendToMain(intentThatStartedThisActivity);

                    } else {

                        Snackbar.make(findViewById(R.id.setup_layout),task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();

                    }
                    databaseReference = firebaseDatabase.getReference();
                    databaseReference.child("Users").child(user_id).child("online").setValue(false);
                    setupProgress.setVisibility(View.INVISIBLE);
                }
            });

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (intentThatStartedThisActivity.equals("not_setup")){
            logout();
        }


    }

    private void sendToMain(String whichActivity){

        if (whichActivity.equals("not_setup")){
            Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
        } else {
            finish();
            
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Snackbar.make(findViewById(R.id.setup_layout),error.toString(),Snackbar.LENGTH_SHORT).show();

            }
        }
    }

    private void bringImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    private void logout(){
        firebaseAuth.signOut();
        finish();
    }

}
