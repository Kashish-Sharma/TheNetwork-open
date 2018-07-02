package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thenetwork.app.android.thenetwork.HelperUtils.DateUtils;
import com.thenetwork.app.android.thenetwork.R;

public class FullScreenImageActivity extends AppCompatActivity {

    private BigImageView bigImageView;
    private Toolbar mToolbar;
    private FirebaseFirestore firebaseFirestore;

    private TextView mFrom, mTime;

    private Boolean mToggleActionBarIsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));
        setContentView(R.layout.activity_full_screen_image);

        mToolbar = findViewById(R.id.full_image_toolbar);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.clear);
        actionBar.setTitle("");
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.full_screen_action_bar,null);
        actionBar.setCustomView(action_bar_view);
        mFrom = findViewById(R.id.name_full_bar);
        mTime = findViewById(R.id.last_seen_full_bar);

        String timeString = getIntent().getStringExtra("time");
        long time = Long.parseLong(timeString);

        String imageUrl = getIntent().getStringExtra("image_url");
        String userId = getIntent().getStringExtra("user_id");
        bigImageView = (BigImageView) findViewById(R.id.mBigImage);

        blackNotificationBar(bigImageView);

        bigImageView.setProgressIndicator(new ProgressPieIndicator());
        bigImageView.showImage(Uri.parse(imageUrl));

        bigImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mToggleActionBarIsVisible){
                    mToggleActionBarIsVisible = false;
                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //Hide status bar
                    blackNotificationBar(bigImageView);
                    actionBar.hide();
                } else {
                    mToggleActionBarIsVisible = true;
                    blackNotificationBar(bigImageView);
                    //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // Show status bar
                    actionBar.show();
                }

            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();

        mTime.setText(DateUtils.formatDate(time)+", "+DateUtils.formatTime(time));

        firebaseFirestore.collection("Users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    String name = task.getResult().getString("name");
                    mFrom.setText(name);

                } else {
                    Toast.makeText(FullScreenImageActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT);
                }
            }
        });


    }


    private void blackNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.BLACK);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
