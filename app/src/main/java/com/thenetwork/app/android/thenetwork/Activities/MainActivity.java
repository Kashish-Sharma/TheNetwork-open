package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thenetwork.app.android.thenetwork.Fragments.ChatFragment;
import com.thenetwork.app.android.thenetwork.Fragments.EventsFragment;
import com.thenetwork.app.android.thenetwork.Fragments.HomeFragment;
import com.thenetwork.app.android.thenetwork.Fragments.RequestsFragment;
import com.thenetwork.app.android.thenetwork.HelperUtils.BottomNavigationViewHelper;
import com.thenetwork.app.android.thenetwork.R;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private String current_user_id;
    private BottomNavigationView mainBottomNav;
    private DrawerLayout mainDrawer;
    private ActionBarDrawerToggle mainToggle;
    private NavigationView mainNav;


    private TextView drawerName;
    private CircleImageView drawerImage;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseFirestoreSettings settings;
    private DatabaseReference mUserRef;

    private HomeFragment homeFragment;
    private RequestsFragment requestsFragment;
    private ChatFragment chatFragment;
    private EventsFragment eventsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainDrawer = findViewById(R.id.main_activity);
        mainToggle = new ActionBarDrawerToggle(this, mainDrawer, R.string.open, R.string.close);
        mainDrawer.addDrawerListener(mainToggle);
        mainToggle.syncState();

        mainToolbar = (Toolbar) findViewById(R.id.toolbar);
        //mainToolbar.setTitleTextAppearance(this, R.style.TitleBarTextAppearance);
        //setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Network");
        getSupportActionBar().setElevation(10.0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //firebase
        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() != null){
        mainNav = findViewById(R.id.main_nav);
        View headerView = mainNav.getHeaderView(0);


        drawerName = headerView.findViewById(R.id.nav_name);
        drawerImage = headerView.findViewById(R.id.nav_image);


        mainBottomNav = findViewById(R.id.mainBottomNav);
        BottomNavigationViewHelper.disableShiftMode(mainBottomNav);
        //mainBottomNav.setElevation(10.0f);
        firebaseFirestore = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        firebaseFirestore.setFirestoreSettings(settings);
        //fragment
        homeFragment = new HomeFragment();
        requestsFragment = new RequestsFragment();
        chatFragment = new ChatFragment();
        requestsFragment = new RequestsFragment();
        eventsFragment = new EventsFragment();


        firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("MAIN_NAV", "listen:error", e);
                    return;
                }

                if (documentSnapshot.exists()) {

                    String image = documentSnapshot.getString("image");

                    drawerName.setText(documentSnapshot.getString("name"));
                    Crashlytics.log(1, "GLIDE", "Before setting Drawer pic");
                    Glide.with(MainActivity.this).load(image)
                            .apply(new RequestOptions().dontTransform().dontAnimate().placeholder(R.drawable.default_pic))
                            //.thumbnail(thumbnailRequestUserImage)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(drawerImage);
                    Crashlytics.log(1, "GLIDE", "After setting Drawer pic");

                }
            }
        });

        initializeFragment();


//        mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
//            @Override
//            public void onSuccess(GetTokenResult getTokenResult) {
//                String token_id = getTokenResult.getToken();
//                Map<String,Object> tokenMap = new HashMap<>();
//                tokenMap.put("token_id",token_id);
//                Log.i("token","Map made uploading .....");
//                firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid())
//                        .update(tokenMap).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful())
//                        {
//                            Log.i("token","updated");
//                        } else {
//                            Log.i("token","failed "+task.getException().getMessage());
//                        }
//                    }
//                });
//
//            }
//        });


            String token_id = FirebaseInstanceId.getInstance().getToken();
            Map<String,Object> tokenMap = new HashMap<>();
            tokenMap.put("token_id",token_id);
            Log.i("token","Map made uploading .....");
            firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                    .update(tokenMap).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        Log.i("token","updated");
                    } else {
                        Log.i("token","failed "+task.getException().getMessage());
                    }
                }
            });



        mainNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_find:
                        Intent usersIntent = new Intent(MainActivity.this,AllUsersActivity.class);
                        startActivity(usersIntent);
                        return true;
                    case R.id.action_following:
                        sendToFollow("Following",mAuth.getCurrentUser().getUid());
                        return true;
                    case R.id.action_followers:
                        sendToFollow("Followers",mAuth.getCurrentUser().getUid());
                        return true;
                        //Do some thing here
                        // add navigation drawer item onclick method here
                    case R.id.action_invites:
                        //Do some thing here
                        // add navigation drawer item onclick method here
                        return true;
                }
                return false;
            }
        });


        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.bottom_home:
                        replaceFragment(homeFragment);
                        return true;
                    case R.id.bottom_requests:
                        replaceFragment(requestsFragment);
                        return true;
                    case R.id.bottom_chat:
                        replaceFragment(chatFragment);
                        return true;
                    case R.id.bottom_event:
                        replaceFragment(eventsFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(mAuth.getCurrentUser().getUid());



    }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            mUserRef.child("online").setValue(System.currentTimeMillis());
        }
    }

    @Override
    public void onBackPressed() {
        if (this.mainDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mainDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
            sendToLogin();
        } else {

            if (!currentUser.isEmailVerified()){
                Toast.makeText(MainActivity.this,"Email not verified",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,LoginStatusActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                current_user_id = mAuth.getCurrentUser().getUid();
                firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()){
                            if (!task.getResult().exists()){

                            Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
                            setupIntent.putExtra("whichState","not_setup");
                            startActivity(setupIntent);
                            finish();

                            }
                        } else {
                            Snackbar.make(findViewById(R.id.main_activity),task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
            }
            mUserRef.child("online").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("online","updated");
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser()!=null){
            mUserRef.child("online").setValue("true");
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mUserRef.child("online").setValue(System.currentTimeMillis());
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mainToggle.onOptionsItemSelected(item)){
            return true;
        }

        switch (item.getItemId()){

            case R.id.action_logout:
                logout();
                return true;

            case R.id.action_account:
                Intent accountIntent = new Intent(MainActivity.this,ProfileActivity.class);
                accountIntent.putExtra("user_id",current_user_id);
                startActivity(accountIntent);
                return true;

            default:
                return false;

        }

    }

    private void logout() {
        Map<String,Object> tokenMapRemove = new HashMap<>();
        tokenMapRemove.put("token_id", FieldValue.delete());
        firebaseFirestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .update(tokenMapRemove).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mAuth.signOut();
                    sendToLogin();
                } else {
                    Toast.makeText(MainActivity.this,"Error logging out, please check your connection."
                            ,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToLogin(){
        Intent intent = new Intent(MainActivity.this,AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (fragment == homeFragment){
            fragmentTransaction.hide(requestsFragment);
            fragmentTransaction.hide(chatFragment);
            fragmentTransaction.hide(eventsFragment);

        } else if (fragment == requestsFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(chatFragment);
            fragmentTransaction.hide(eventsFragment);

        } else if (fragment == chatFragment){
            fragmentTransaction.hide(requestsFragment);
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(eventsFragment);

        } else if (fragment == eventsFragment){
            fragmentTransaction.hide(requestsFragment);
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(chatFragment);
        }

        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }



    private void sendToFollow(String data, String userid) {
        Intent intent = new Intent(MainActivity.this, FollowActivity.class);
        intent.putExtra("title",data);
        intent.putExtra("whichUser",userid);
        startActivity(intent);
    }

    public void initializeFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_container,homeFragment);
        fragmentTransaction.add(R.id.main_container,requestsFragment);
        fragmentTransaction.add(R.id.main_container,chatFragment);
        fragmentTransaction.add(R.id.main_container,eventsFragment);

        fragmentTransaction.hide(requestsFragment);
        fragmentTransaction.hide(chatFragment);
        fragmentTransaction.hide(eventsFragment);

        fragmentTransaction.commit();

    }


}
