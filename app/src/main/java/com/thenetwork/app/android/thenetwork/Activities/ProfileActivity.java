package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thenetwork.app.android.thenetwork.Adapters.ProfileBlogRecyclerAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.BlogPost;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.FOLLOWING;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.FOLLOW_REQUEST_SENT_NOTIFICATION;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.NOT_FOLLOW;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.REQUEST_FOLLOW;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.REQUEST_RECEIVED;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.REQUEST_SENT;

public class ProfileActivity extends AppCompatActivity {


    private RecyclerView profileBlogListView;
    private List<BlogPost> blog_list;
    private ProfileBlogRecyclerAdapter profileBlogRecyclerAdapter;


    private Toolbar mToolbar;
    private TextView mUsername, mAbout, mChange;
    private TextView mFollowers, mFollowing, postCount;
    private CircleImageView mProfileImage;
    private ProgressBar progressBar;
    private Button followBtn;
    private final static String TAG = "PROFILE_ACTIVITY";
    private LinearLayout mFollowerLayout, mFollowingLayout;

    private RelativeLayout mRequestLayout;
    private TextView mRequestName;
    private Button mRequestAccept, mRequestCancel;
    private ProgressBar mAcceptProgress, mCancelProgress;
    private int mCurrentState;
    private String mCurrentUserId;

    private CollectionReference mRequestReference;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String mName;
    private String mCurrentName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = findViewById(R.id.profile_toolbar);
        mToolbar.setTitle("Profile");
        mToolbar.setElevation(10.0f);
        mToolbar.setTitleTextAppearance(ProfileActivity.this, R.style.TitleBarTextAppearance);
        mToolbar.setTitleTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.black));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();

        final String user_id = getIntent().getStringExtra("user_id");
        mRequestReference = firebaseFirestore.collection("Requests");
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mRequestLayout = findViewById(R.id.request_layout);
        mRequestName = findViewById(R.id.request_name);
        mRequestAccept = findViewById(R.id.accept_request_btn);
        mRequestCancel = findViewById(R.id.cancel_request_btn);
        mAcceptProgress = findViewById(R.id.acceptProgress);
        mCancelProgress = findViewById(R.id.cancelProgress);
        mCurrentState = NOT_FOLLOW;


        mUsername = findViewById(R.id.profile_name);
        mAbout = findViewById(R.id.profile_desc);
        mChange = findViewById(R.id.profile_change);
        mProfileImage = findViewById(R.id.profile_image);
        progressBar = findViewById(R.id.connectProgress);
        followBtn = findViewById(R.id.follow_btn);
        mFollowers = findViewById(R.id.followers_count);
        mFollowing = findViewById(R.id.following_count);
        mFollowerLayout = findViewById(R.id.followers_layout);
        mFollowingLayout = findViewById(R.id.following_layout);
        postCount = findViewById(R.id.postCount);


        ///Recyclerview
        blog_list = new ArrayList<>();
        profileBlogListView = findViewById(R.id.posts_mini_recycler_view);
        profileBlogRecyclerAdapter = new ProfileBlogRecyclerAdapter(this,blog_list);
        profileBlogListView.setLayoutManager(new GridLayoutManager(this,3));
        profileBlogListView.setAdapter(profileBlogRecyclerAdapter);

        Query firstQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("user_id",user_id);

        firstQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()){
                    for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                            blog_list.add(blogPost);
                            profileBlogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }

                } else {
                    Log.i(TAG,task.getException().toString()+" acb");
                }

            }
        });

        ///////////////////////////////////////////////////////


        mFollowingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToFollow("Following",user_id);
            }
        });

        mFollowerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToFollow("Followers",user_id);
            }
        });

        if (!user_id.equals(mCurrentUserId)){
            mChange.setVisibility(View.GONE);
            followBtn.setVisibility(View.VISIBLE);
        } else {
            followBtn.setVisibility(View.GONE);
        }


        updateFollowButtonStatus(mCurrentUserId, user_id);

        //Getting posts
        firebaseFirestore.collection("Posts").whereEqualTo("user_id",user_id).get().addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                 if (task.isSuccessful()){

                     if(task.getResult().getDocuments().isEmpty()){
                         postCount.setText(0+"");
                     } else {
                         postCount.setText(task.getResult().getDocuments().size()+"");
                     }
                 }
            }
        });


        mRequestAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptRequest(mCurrentUserId, user_id);
                updateFollowButtonStatus(mCurrentUserId, user_id);
            }
        });

        mRequestCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest(mCurrentUserId,user_id);
                updateFollowButtonStatus(mCurrentUserId,user_id);
            }
        });


        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(ProfileActivity.this,SetupActivity.class);
                settingsIntent.putExtra("whichState","is_setup");
                startActivity(settingsIntent);
            }
        });

    //Replace this with shared preferences////////////////////////////////////////////////////////////////////////////////////
        firebaseFirestore.collection("Users").document(mCurrentUserId)
                .addSnapshotListener(ProfileActivity.this,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            mCurrentName = documentSnapshot.getString("name");
                        }
                    }
                });
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        firebaseFirestore.collection("Users").document(user_id)
                .addSnapshotListener(ProfileActivity.this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (documentSnapshot.exists()){

                    String name = documentSnapshot.getString("name");
                    mName = name;
                    String image = documentSnapshot.getString("image");
                    String skills = documentSnapshot.getString("skills");
                    String about = documentSnapshot.getString("about");
                    String phone = documentSnapshot.getString("phone");
                    String email = documentSnapshot.getString("email");

                    mRequestName.setText(mName+" has requested to follow you");
                    mUsername.setText(name);
                    mAbout.setText(about);

                    Crashlytics.log(1, "GLIDE", "Before setting setupActivity pic");

                    GlideLoadImage.loadSmallImage(ProfileActivity.this,mProfileImage,image,image);
                    Crashlytics.log(1, "GLIDE", "After setting setupActivity pic");
                }
            }
        });


        //Getting followers
        firebaseFirestore.collection("SocialStatus/" + user_id + "/Followers")
                .addSnapshotListener(ProfileActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    int followersCount = documentSnapshots.size();
                    mFollowers.setText(followersCount+"");
                } else {
                    mFollowers.setText(0+"");
                }

            }
        });


        //Getting following
        firebaseFirestore.collection("SocialStatus/" + user_id + "/Following")
                .addSnapshotListener(ProfileActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    int followingCount = documentSnapshots.size();
                    mFollowing.setText(followingCount+"");
                } else {
                    mFollowing.setText(0+"");
                }
            }
        });


        //Sending follow request
       followBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               followBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
               firebaseFirestore.collection("SocialStatus/" + user_id + "/Followers")
                       .document(mCurrentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if (task.isSuccessful()){

                           followBtn.setVisibility(View.GONE);
                           progressBar.setVisibility(View.VISIBLE);

                           if (!task.getResult().exists()){
///////////--------------------------NOT FOLLOW STATE ---------------------------------//////////////////////////
                               if (mCurrentState == NOT_FOLLOW){

                                   final Map<String, Object> requestMap = new HashMap<>();
                                   requestMap.put("type", REQUEST_SENT);
                                   requestMap.put("timestamp",System.currentTimeMillis());
                                   requestMap.put("name",mCurrentName);

                                   mRequestReference.document(mCurrentUserId).collection("request-sent")
                                           .document(user_id).set(requestMap)
                                           .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {

                                                   if (task.isSuccessful()){

                                                       final Map<String, Object> requestMap = new HashMap<>();
                                                       requestMap.put("type", REQUEST_RECEIVED);
                                                       requestMap.put("timestamp",System.currentTimeMillis());
                                                       requestMap.put("name",mCurrentName);

                                                       mRequestReference.document(user_id).collection("request-recieved")
                                                               .document(mCurrentUserId).set(requestMap)
                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {

                                                                       if (task.isSuccessful()){

                                                                           mCurrentState = REQUEST_FOLLOW;
                                                                            followBtn.setText("Requested");
                                                                            followBtn.setTextColor(ContextCompat
                                                                                    .getColor(ProfileActivity.this,R.color.white));

                                                                            notificationForFollowRequest(mCurrentUserId,user_id);

                                                                           progressBar.setVisibility(View.GONE);
                                                                           followBtn.setVisibility(View.VISIBLE);

                                                                       } else {
                                                                           progressBar.setVisibility(View.GONE);
                                                                           followBtn.setVisibility(View.VISIBLE);
                                                                       }
                                                                   }
                                                               });
                                                   } else {
                                                       Snackbar.make(findViewById(R.id.profile_activity)
                                                               ,getResources().getString(R.string.follow_error_message)
                                                               ,Snackbar.LENGTH_LONG).show();
                                                       progressBar.setVisibility(View.GONE);
                                                       followBtn.setVisibility(View.VISIBLE);
                                                   }
                                               }
                                           });
                               }
///////////--------------------------CANCEL FOLLOW STATE ---------------------------------//////////////////////////
                               else if (mCurrentState == REQUEST_FOLLOW){
                                   mRequestReference.document(mCurrentUserId).collection("request-sent")
                                           .document(user_id).delete()
                                           .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               mRequestReference.document(user_id).collection("request-recieved")
                                                       .document(mCurrentUserId).delete()
                                                       .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {

                                                       if (task.isSuccessful()){
                                                           mCurrentState = NOT_FOLLOW;

                                                           updateFollowButtonStatus(mCurrentUserId,user_id);

                                                           progressBar.setVisibility(View.GONE);
                                                           followBtn.setVisibility(View.VISIBLE);

                                                       } else {
                                                           progressBar.setVisibility(View.GONE);
                                                           followBtn.setVisibility(View.VISIBLE);
                                                       }

                                                   }
                                               });
                                           } else {
                                               progressBar.setVisibility(View.GONE);
                                               followBtn.setVisibility(View.VISIBLE);
                                           }
                                       }
                                   });

                               }

/////////////////////// If account is open //////////////////////////////////////////////////
//                               Map<String, Object> toUserMap = new HashMap<>();
//                               toUserMap.put("timestamp", System.currentTimeMillis());
//
//                               final Map<String, Object> currentUserMap = new HashMap<>();
//                               currentUserMap.put("timestamp", System.currentTimeMillis());
//
//                               firebaseFirestore.collection("SocialStatus/" + user_id + "/Followers")
//                                       .document(mCurrentUserId).set(toUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                   @Override
//                                   public void onComplete(@NonNull Task<Void> task) {
//                                       if (task.isSuccessful()){
//                                           firebaseFirestore.collection("SocialStatus/" + mCurrentUserId + "/Following")
//                                                   .document(user_id).set(currentUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                               @Override
//                                               public void onComplete(@NonNull Task<Void> task) {
//                                                   if (task.isSuccessful()){
//                                                       Toast.makeText(ProfileActivity.this,"You started following "+mName,Toast.LENGTH_SHORT).show();
//                                                       progressBar.setVisibility(View.GONE);
//                                                       followBtn.setVisibility(View.VISIBLE);
//
//                                                   } else {
//                                                       Log.e(TAG,"following "+task.getException().getMessage());
//                                                       Snackbar.make(findViewById(R.id.profile_activity)
//                                                               ,"An unexpected error occurred, please check your connection or try again after sometime."
//                                                               ,Snackbar.LENGTH_LONG).show();
//                                                       progressBar.setVisibility(View.GONE);
//                                                       followBtn.setVisibility(View.VISIBLE);
//                                                   }
//                                               }
//                                           });
//
//                                       } else {
//
//                                           Log.e(TAG,"follower "+task.getException().getMessage());
//                                           Snackbar.make(findViewById(R.id.profile_activity)
//                                                   ,"An unexpected error occurred, please check your connection or try again after sometime."
//                                                   ,Snackbar.LENGTH_LONG).show();
//                                           progressBar.setVisibility(View.GONE);
//                                           followBtn.setVisibility(View.VISIBLE);
//                                       }
//                                   }
//                               });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                               // Unfollowing
                           } else if (task.getResult().exists()){
                               firebaseFirestore.collection("SocialStatus/" + user_id + "/Followers")
                                       .document(mCurrentUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()){

                                           firebaseFirestore.collection("SocialStatus/" + mCurrentUserId + "/Following")
                                                   .document(user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if (task.isSuccessful()){

                                                       mCurrentState = NOT_FOLLOW;
                                                       updateFollowButtonStatus(mCurrentUserId,user_id);
                                                       Toast.makeText(ProfileActivity.this,"Unfollowed",Toast.LENGTH_SHORT).show();
                                                       progressBar.setVisibility(View.GONE);
                                                       followBtn.setVisibility(View.VISIBLE);
                                                   } else {
                                                       Log.e(TAG,"following "+task.getException().getMessage());
                                                       Snackbar.make(findViewById(R.id.profile_activity)
                                                               ,getResources().getString(R.string.follow_error_message)
                                                               ,Snackbar.LENGTH_LONG).show();
                                                       progressBar.setVisibility(View.GONE);
                                                       followBtn.setVisibility(View.VISIBLE);
                                                   }
                                               }
                                           });

                                       } else {
                                           progressBar.setVisibility(View.GONE);
                                           followBtn.setVisibility(View.VISIBLE);
                                           Log.e(TAG,"follower "+task.getException().getMessage());
                                           Snackbar.make(findViewById(R.id.profile_activity)
                                                   , getResources().getString(R.string.follow_error_message)
                                                   ,Snackbar.LENGTH_LONG).show();

                                       }
                                   }
                               });
                           }

                       } else {
                           progressBar.setVisibility(View.GONE);
                           followBtn.setVisibility(View.VISIBLE);
                           Snackbar.make(findViewById(R.id.profile_activity)
                                   ,getResources().getString(R.string.follow_error_message),Snackbar.LENGTH_LONG).show();

                       }
                   }
               });

           }
       });

    }

    private void logout() {
        Map<String,Object> tokenMapRemove = new HashMap<>();
        tokenMapRemove.put("token_id", FieldValue.delete());
        firebaseFirestore.collection("Users").document(mCurrentUserId)
                .update(tokenMapRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mAuth.signOut();
                sendToLogin();
            }
        });
    }

    private void sendToLogin() {
        Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sendToFollow(String data, String userid) {
        Intent intent = new Intent(ProfileActivity.this, FollowActivity.class);
        intent.putExtra("title",data);
        intent.putExtra("whichUser",userid);
        startActivity(intent);
    }

    private void updateFollowButtonStatus(final String currentUserId, final String otherUserId){
        //Default state
        mCurrentState = NOT_FOLLOW;
        mRequestLayout.setVisibility(View.GONE);
        followBtn.setBackground(ContextCompat
                .getDrawable(ProfileActivity.this,R.drawable.connect_bg));
        followBtn.setText("Follow");
        followBtn.setTextColor(ContextCompat
                .getColor(ProfileActivity.this,R.color.white));


        // If I have sent request
        mRequestReference.document(currentUserId).collection("request-sent")
                .document(otherUserId).addSnapshotListener(ProfileActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){

                            mCurrentState = REQUEST_FOLLOW;
                            //mRequestLayout.setVisibility(View.GONE);
                            followBtn.setBackground(ContextCompat
                                    .getDrawable(ProfileActivity.this,R.drawable.connect_bg));
                            followBtn.setText("Requested");
                            followBtn.setTextColor(ContextCompat
                                    .getColor(ProfileActivity.this,R.color.white));

                        } else {
                            //If I'm his follower
                            firebaseFirestore.collection("SocialStatus/" + otherUserId + "/Followers")
                                    .document(currentUserId)
                                    .addSnapshotListener(ProfileActivity.this,new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                            if (e != null) {
                                                Log.w(TAG, "listen:error", e);
                                                return;
                                            }

                                            if (documentSnapshot.exists()){

                                                mCurrentState = FOLLOWING;

                                                followBtn.setBackground(ContextCompat.getDrawable(ProfileActivity.this,R.drawable.edit_settings_bg));
                                                followBtn.setText("Unfollow");
                                                followBtn.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.black));

                                            } else {
                                                //If he is my follower
                                                firebaseFirestore.collection("SocialStatus")
                                                        .document(currentUserId).collection("Followers")
                                                        .document(otherUserId).addSnapshotListener(ProfileActivity.this
                                                        ,new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot
                                                                    , @Nullable FirebaseFirestoreException e) {

                                                                if (e != null) {
                                                                    Log.w(TAG, "listen:error", e);
                                                                    return;
                                                                }

                                                                if (documentSnapshot.exists()){

                                                                    mCurrentState = NOT_FOLLOW;

                                                                    followBtn.setBackground(ContextCompat
                                                                            .getDrawable(ProfileActivity.this,R.drawable.connect_bg));
                                                                    followBtn.setText("Follow back");
                                                                    followBtn.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.white));
                                                                } else {
                                                                    followBtn.setBackground(ContextCompat
                                                                            .getDrawable(ProfileActivity.this,R.drawable.connect_bg));
                                                                    followBtn.setText("Follow");
                                                                    followBtn.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.white));
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                }
        });
                // If I have recieved request
                mRequestReference.document(currentUserId).collection("request-recieved")
                        .document(otherUserId).addSnapshotListener(ProfileActivity.this
                        , new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot
                                    , @Nullable FirebaseFirestoreException e) {

                                if (e != null) {
                                    Log.w(TAG, "listen:error", e);
                                    return;
                                }

                                if (documentSnapshot.exists()){

                                    mRequestLayout.setVisibility(View.VISIBLE);

                                } else {
                                    mRequestLayout.setVisibility(View.GONE);
                                }
                            }
                        });

    }

    private void acceptRequest(final String currentUserId , final String otherUserId){

        mRequestCancel.setVisibility(View.GONE);
        mRequestAccept.setVisibility(View.GONE);
        mAcceptProgress.setVisibility(View.VISIBLE);


        mRequestReference.document(currentUserId)
                .collection("request-recieved").document(otherUserId).delete()
                .addOnCompleteListener(ProfileActivity.this,new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            mRequestReference.document(otherUserId).collection("request-sent")
                                    .document(currentUserId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        final Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("timestamp", System.currentTimeMillis());

                                        firebaseFirestore.collection("SocialStatus/" + currentUserId + "/Followers")
                                                .document(otherUserId).set(userMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            firebaseFirestore.collection("SocialStatus/" + otherUserId + "/Following")
                                                                    .document(currentUserId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        updateFollowButtonStatus(currentUserId, otherUserId);
                                                                        mRequestLayout.setVisibility(View.GONE);
                                                                        mRequestAccept.setVisibility(View.VISIBLE);
                                                                        mRequestCancel.setVisibility(View.VISIBLE);
                                                                        mAcceptProgress.setVisibility(View.GONE);

                                                                    } else {
                                                                        Snackbar.make(findViewById(R.id.profile_activity)
                                                                                ,getResources().getString(R.string.follow_error_message)
                                                                                ,Snackbar.LENGTH_LONG).show();
                                                                        mAcceptProgress.setVisibility(View.GONE);
                                                                        mRequestAccept.setVisibility(View.VISIBLE);
                                                                        mRequestCancel.setVisibility(View.VISIBLE);

                                                                    }

                                                                }
                                                            });

                                                        } else {
                                                            Snackbar.make(findViewById(R.id.profile_activity)
                                                                    ,getResources().getString(R.string.follow_error_message)
                                                                    ,Snackbar.LENGTH_LONG).show();
                                                            mAcceptProgress.setVisibility(View.GONE);
                                                            mRequestAccept.setVisibility(View.VISIBLE);
                                                            mRequestCancel.setVisibility(View.VISIBLE);

                                                        }
                                                    }
                                                });

                                    } else {

                                        Snackbar.make(findViewById(R.id.profile_activity)
                                                ,getResources().getString(R.string.follow_error_message)
                                                ,Snackbar.LENGTH_LONG).show();
                                        mAcceptProgress.setVisibility(View.GONE);
                                        mRequestAccept.setVisibility(View.VISIBLE);
                                        mRequestCancel.setVisibility(View.VISIBLE);

                                    }

                                }
                            });

                        }  else {

                            Snackbar.make(findViewById(R.id.profile_activity)
                                    , getResources().getString(R.string.follow_error_message),Snackbar.LENGTH_LONG).show();
                            mAcceptProgress.setVisibility(View.GONE);
                            mRequestAccept.setVisibility(View.VISIBLE);
                            mRequestCancel.setVisibility(View.VISIBLE);

                        }

                    }
                });
    }

    private void cancelRequest(final String currentUserId, final String otherUserId){

        mRequestCancel.setVisibility(View.GONE);
        mRequestAccept.setVisibility(View.GONE);
        mCancelProgress.setVisibility(View.VISIBLE);

        mRequestReference.document(currentUserId)
                .collection("request-recieved").document(otherUserId).delete()
                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            mRequestReference.document(otherUserId)
                                    .collection("request-sent").document(currentUserId).delete()
                                    .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                updateFollowButtonStatus(currentUserId, otherUserId);
                                                mCancelProgress.setVisibility(View.GONE);
                                                mRequestLayout.setVisibility(View.GONE);
                                                mRequestCancel.setVisibility(View.VISIBLE);
                                                mRequestAccept.setVisibility(View.VISIBLE);


                                            } else {
                                                Snackbar.make(findViewById(R.id.profile_activity)
                                                        ,getResources().getString(R.string.follow_error_message)
                                                        ,Snackbar.LENGTH_LONG).show();
                                                mCancelProgress.setVisibility(View.GONE);
                                                mRequestCancel.setVisibility(View.VISIBLE);
                                                mRequestAccept.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });

                        } else {
                            Snackbar.make(findViewById(R.id.profile_activity)
                                    ,getResources().getString(R.string.follow_error_message)
                                    ,Snackbar.LENGTH_LONG).show();
                            mCancelProgress.setVisibility(View.GONE);
                            mRequestCancel.setVisibility(View.VISIBLE);
                            mRequestAccept.setVisibility(View.VISIBLE);
                        }
                    }
                });


    }

    private void notificationForFollowRequest(final String currentUserId, final String otherUserId){

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("type",FOLLOW_REQUEST_SENT_NOTIFICATION);
        notificationMap.put("from",currentUserId);

        firebaseFirestore.collection("Users/"+otherUserId+"/Notifications")
                .add(notificationMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ProfileActivity.this,"Notification sent",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this,"Failed to send notification",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}