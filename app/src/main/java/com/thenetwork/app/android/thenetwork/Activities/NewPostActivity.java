package com.thenetwork.app.android.thenetwork.Activities;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thenetwork.app.android.thenetwork.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final int READCODE = 1;
    private static final int WRITECODE = 2;

    //private Toolbar newPostToolbar;
    private ImageView newPostImageView;
    private EditText newPostTitle;
    private EditText newPostDesc;
    private ProgressBar newPostProgressBar;
    private Menu mMenu;

    private Uri postImageUri = null;
    private String current_user_id;

    private Bitmap compressedImageFile;
    private Bitmap compressedThumbFile;

    private String lineDownloadLink = "https://firebasestorage.googleapis.com/v0/b/thenetwork-445bd.appspot.com/o/line-min.png?alt=media&token=eea0df68-1689-4297-9dbc-10f50ac50199";

    //firebase
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser mCurrentUser;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        //newPostToolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(newPostToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add a new post");
        actionBar.setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        newPostImageView = findViewById(R.id.new_post_image);
        newPostTitle = findViewById(R.id.new_post_title);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostProgressBar = findViewById(R.id.new_post_progress);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        newPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(NewPostActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(NewPostActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED){
                        Snackbar.make(findViewById(R.id.newPostLayout),"Please grant permissions",Snackbar.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},READCODE);
                        ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITECODE);

                    } else {

                        bringImagePicker();
                    }

                } else {
                    bringImagePicker();
                }

            }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImageView.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void bringImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowCounterRotation(true)
                .setMultiTouchEnabled(true)
                .setMinCropResultSize(512,512)
                .start(NewPostActivity.this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.new_post_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_post_blog:
                mMenu.findItem(R.id.action_post_blog).setEnabled(false);
                Log.i("NewPost","Disabled");
                //mMenu.findItem(R.id.action_post_blog).setCheckable(false);


                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                final String title = newPostTitle.getText().toString();
                final String desc = newPostDesc.getText().toString();
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && postImageUri!=null){

                    //Verifying user
                    newPostImageView.setEnabled(false);
                    newPostTitle.setEnabled(false);
                    newPostTitle.setFocusable(false);
                    newPostTitle.setCursorVisible(false);


                    newPostDesc.setEnabled(false);
                    newPostDesc.setFocusable(false);
                    newPostDesc.setCursorVisible(false);


                    if (!mCurrentUser.isEmailVerified()){

                        Snackbar mSnackbar = Snackbar.make(findViewById(R.id.newPostLayout), "Your account is not verified, please verify to post.", Snackbar.LENGTH_LONG)
                                .setAction("VERIFY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendToStatus();
                                    }
                                });
                        mSnackbar.show();
                        mMenu.findItem(R.id.action_post_blog).setEnabled(true);
                        Log.i("NewPost","Enabled user not verified");
                       // mMenu.findItem(R.id.action_post_blog).setCheckable(true);
                        return true;

                    } else if (mCurrentUser.isEmailVerified()){
                        Log.i("DEBUG","Login true");
                        newPostProgressBar.setVisibility(View.VISIBLE);
                        if (postImageUri != null){

                            //Converting URI to Bitmap

                            File newImageFile = new File(postImageUri.getPath());

                            try {
                                compressedImageFile = new Compressor(NewPostActivity.this)
                                        .setQuality(75)
                                        .compressToBitmap(newImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG,75,baos);
                            byte[] imageData = baos.toByteArray();

                            //Conversion complete
                            final String randomName = UUID.randomUUID().toString();
                            final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                            filePath.putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    final String downloadUrl = filePath.getDownloadUrl().toString();
                                    if (task.isSuccessful()){

                                        File newThumbFile = new File(postImageUri.getPath());
//
                                        try {
                                            compressedThumbFile = new Compressor(NewPostActivity.this)
                                                    .setMaxWidth(200)
                                                    .setMaxHeight(200)
                                                    .setQuality(2)
                                                    .compressToBitmap(newThumbFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        compressedThumbFile.compress(Bitmap.CompressFormat.JPEG,2,baos);
                                        byte[] thumbData = baos.toByteArray();

                                        final UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                                .child(randomName + ".jpg").putBytes(thumbData);

                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                filePath.getDownloadUrl().addOnSuccessListener(NewPostActivity.this, new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(final Uri uriImage) {

                                                        storageReference.child("post_images/thumbs")
                                                                .child(randomName + ".jpg").getDownloadUrl().addOnSuccessListener(NewPostActivity.this, new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {

                                                                String downloadUrl = uriImage.toString();
                                                                String downloadThumbUri = uri.toString();
//
                                                                Map<String,Object> postMap = new HashMap<>();
                                                                postMap.put("image_url",downloadUrl);
                                                                postMap.put("thumb_url",downloadThumbUri);
                                                                postMap.put("title",title);
                                                                postMap.put("desc",desc);
                                                                postMap.put("user_id",current_user_id);
                                                                postMap.put("timestamp",System.currentTimeMillis());
                                                                postMap.put("image_name",randomName);


                                                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                        if (task.isSuccessful()){

                                                                            //Snackbar.make(findViewById(R.id.newPostLayout),"Success", Snackbar.LENGTH_SHORT).show();

                                                                            Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(mainIntent);
                                                                            Log.i("DEBUG","Login true , finish with image");


                                                                        } else {
                                                                            Snackbar.make(findViewById(R.id.newPostLayout), task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                        }
                                                                        newPostProgressBar.setVisibility(View.INVISIBLE);
                                                                        mMenu.findItem(R.id.action_post_blog).setEnabled(true);
                                                                        Log.i("NewPost","Enabled upload successful");
                                                                        //mMenu.findItem(R.id.action_post_blog).setCheckable(true);
                                                                        newPostImageView.setEnabled(true);

                                                                        newPostTitle.setEnabled(true);
                                                                        newPostTitle.setFocusable(true);
                                                                        newPostTitle.setCursorVisible(true);


                                                                        newPostDesc.setEnabled(true);
                                                                        newPostDesc.setFocusable(true);
                                                                        newPostDesc.setCursorVisible(true);

                                                                    }
                                                                });

                                                            }
                                                        });

                                                    }
                                                });
//
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mMenu.findItem(R.id.action_post_blog).setEnabled(true);
                                                mMenu.findItem(R.id.action_post_blog).setCheckable(true);
                                                newPostProgressBar.setVisibility(View.GONE);
                                                newPostTitle.setEnabled(true);
                                                Log.i("NewPost","Enabled upload failed");
                                                newPostTitle.setFocusable(true);
                                                newPostTitle.setCursorVisible(true);


                                                newPostDesc.setEnabled(true);
                                                newPostDesc.setFocusable(true);
                                                newPostDesc.setCursorVisible(true);
                                            }
                                        });

                                    } else {
                                        Snackbar.make(findViewById(R.id.newPostLayout), task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                        mMenu.findItem(R.id.action_post_blog).setEnabled(true);
                                        Log.i("NewPost","Enabled downloadUrl success not thumb");
                                        //mMenu.findItem(R.id.action_post_blog).setCheckable(true);
                                        newPostProgressBar.setVisibility(View.GONE);
                                        newPostTitle.setEnabled(true);
                                        newPostTitle.setFocusable(true);
                                        newPostTitle.setCursorVisible(true);


                                        newPostDesc.setEnabled(true);
                                        newPostDesc.setFocusable(true);
                                        newPostDesc.setCursorVisible(true);
                                    }

                                }
                            });
                        }

//                        else {
//
//                            Map<String,Object> postMap = new HashMap<>();
//                            postMap.put("image_url",lineDownloadLink);
//                            postMap.put("thumb_url",lineDownloadLink);
//                            postMap.put("title",title);
//                            postMap.put("desc",desc);
//                            postMap.put("user_id",current_user_id);
//                            postMap.put("timestamp",System.currentTimeMillis());
//
//                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentReference> task) {
//                                    if (task.isSuccessful()){
//
//                                        Snackbar.make(findViewById(R.id.newPostLayout),"Success", Snackbar.LENGTH_SHORT).show();
//                                        mMenu.findItem(R.id.action_post_blog).setEnabled(true);
//                                        Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
//                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(mainIntent);
//                                        Log.i("DEBUG","Login true , finish without image");
//
//
//                                    } else {
//                                        Snackbar.make(findViewById(R.id.newPostLayout), task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
//                                    }
//                                    newPostProgressBar.setVisibility(View.INVISIBLE);
//                                }
//                            });
//
//                        }

                    }

                    //verify ends

                } else {
                    Snackbar.make(findViewById(R.id.newPostLayout),"Title, image and description are required.", Snackbar.LENGTH_LONG).show();
                    mMenu.findItem(R.id.action_post_blog).setEnabled(true);
                    Log.i("NewPost","Enabled textutils were empty");
                    //mMenu.findItem(R.id.action_post_blog).setCheckable(true);
                    newPostTitle.setEnabled(true);
                    newPostTitle.setFocusable(true);
                    newPostTitle.setCursorVisible(true);

                    newPostDesc.setEnabled(true);
                    newPostDesc.setFocusable(true);
                    newPostDesc.setCursorVisible(true);
                }
                //mMenu.findItem(R.id.action_post_blog).setEnabled(true);
                //mMenu.findItem(R.id.action_post_blog).setCheckable(true);
                newPostTitle.setEnabled(true);
                newPostTitle.setFocusable(true);
                newPostTitle.setCursorVisible(true);

                newPostDesc.setEnabled(true);
                newPostDesc.setFocusable(true);
                newPostDesc.setCursorVisible(true);
                return true;

            default:
                return false;

        }

    }

    private void sendToStatus(){

        Intent statusCheckIntent = new Intent(NewPostActivity.this,LoginStatusActivity.class);
        startActivity(statusCheckIntent);
    }

}
