package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thenetwork.app.android.thenetwork.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class selectImageActivity extends AppCompatActivity {

    final String randomName = UUID.randomUUID().toString();
    private Bitmap compressedImageFile;
    private Bitmap compressedThumbFile;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    private Toolbar mToolbar;

    private ImageView imageView;
    private EditText imageMessage;

    private FloatingActionButton floatingActionButton;

    private String mChatUser;
    private String mChatUserName;
    private String mChatUserImage;
    private ProgressBar mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        mToolbar = findViewById(R.id.select_image_app_bar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Image Message");

        imageView = findViewById(R.id.select_imageview);
        imageMessage = findViewById(R.id.select_edittext);

        mProgress = findViewById(R.id.progressBar2);

        final long time = System.currentTimeMillis();
        String uriString = getIntent().getStringExtra("uri");
        final String mCurrentUserId = getIntent().getStringExtra("currentUser");
        mChatUser = getIntent().getStringExtra("chatUser");
        mChatUserName = getIntent().getStringExtra("name");
        mChatUserImage = getIntent().getStringExtra("image");
        final Uri resultUri = Uri.parse(uriString);
        floatingActionButton = findViewById(R.id.select_image_fab);
        imageView.setImageURI(resultUri);

        if (imageMessage.isFocused()){
            imageMessage.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }


        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();



        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.setVisibility(View.VISIBLE);
                imageMessage.setEnabled(false);

                Bitmap bitmap;
                //Saving file to SD card
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(selectImageActivity.this.getContentResolver(),resultUri);


                    File newImageFile = new File(resultUri.getPath());
                    try {
                        compressedImageFile = new Compressor(selectImageActivity.this)
                                .setQuality(60)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG,60,baos);
                    final byte[] imageData = baos.toByteArray();

                    //Conversion complete
                    final StorageReference filePath = storageReference.child("chat_images/"+randomName+".jpg");
                    filePath.putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()){
                                File newThumbFile = new File(resultUri.getPath());
                                try {
                                    compressedThumbFile = new Compressor(selectImageActivity.this)
                                            .setMaxWidth(100)
                                            .setMaxHeight(100)
                                            .setQuality(2)
                                            .compressToBitmap(newThumbFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedThumbFile.compress(Bitmap.CompressFormat.JPEG,2,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("chat_images/thumbs/"+randomName+".jpg")
                                .putBytes(thumbData);


                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        filePath.getDownloadUrl().addOnSuccessListener(selectImageActivity.this,new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(final Uri uriImage) {

                                                storageReference.child("chat_images/thumbs/"+randomName+".jpg").getDownloadUrl().addOnSuccessListener(selectImageActivity.this,new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String downloadUrl = uriImage.toString();
                                                        String downloadThumbUri = uri.toString();

                                                        final Map<String,Object> messageMap = new HashMap<>();
                                                        messageMap.put("message","");
                                                        messageMap.put("image",downloadUrl);
                                                        messageMap.put("thumb",downloadThumbUri);
                                                        messageMap.put("type",imageMessage.getText().toString().isEmpty()?1:2);
                                                        messageMap.put("from",mCurrentUserId);
                                                        messageMap.put("seen",true);
                                                        messageMap.put("time",System.currentTimeMillis());
                                                        messageMap.put("path","");
                                                        messageMap.put("filename",randomName);
                                                        messageMap.put("imageText",imageMessage.getText().toString());

                                                        final Map<String,Object> messageUserMap = new HashMap<>();
                                                        messageUserMap.put("message","");
                                                        messageUserMap.put("image",downloadUrl);
                                                        messageUserMap.put("thumb",downloadThumbUri);
                                                        messageUserMap.put("type",imageMessage.getText().toString().isEmpty()?1:2);
                                                        messageUserMap.put("from",mCurrentUserId);
                                                        messageUserMap.put("seen",false);
                                                        messageUserMap.put("time",System.currentTimeMillis());
                                                        messageUserMap.put("path","");
                                                        messageUserMap.put("filename",randomName);
                                                        messageUserMap.put("imageText",imageMessage.getText().toString());
                                                        Log.i("ImageInfo",imageMessage.getText().toString());

                                                        firebaseFirestore.collection("Messages").document(mCurrentUserId).collection(mChatUser).add(messageMap);
                                                        firebaseFirestore.collection("Messages").document(mChatUser).collection(mCurrentUserId).add(messageUserMap);
                                                        mProgress.setVisibility(View.GONE);
                                                        Intent intent = new Intent(selectImageActivity.this,ChatActivity.class);
                                                        intent.putExtra("user_id",mChatUser);
                                                        intent.putExtra("name",mChatUserName);
                                                        intent.putExtra("image",mChatUserImage);
                                                        Log.i("ImageInfo","sent "+ (System.currentTimeMillis()-time)/1000 + " secs");
                                                        startActivity(intent);
                                                        finish();

                                                    }
                                                });

                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(selectImageActivity.this,ChatActivity.class);
        intent.putExtra("user_id",mChatUser);
        intent.putExtra("name",mChatUserName);
        intent.putExtra("image",mChatUserImage);
        startActivity(intent);
        finish();

    }

    private String saveToInternalStorage(Bitmap bitmapImage, String filename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("Chat_Images", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos!=null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}
