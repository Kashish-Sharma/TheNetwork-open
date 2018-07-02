package com.thenetwork.app.android.thenetwork.Activities;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.thenetwork.app.android.thenetwork.Adapters.FollowAdapter;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.List;

public class FollowActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    private List<String> users_list;
    private FollowAdapter usersRecyclerAdapter;
    public Boolean myAccount;


    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference mUserRef;


    private static final String Followers = "Followers";
    private static final String Following = "Following";

    private static final String TAG = "FollowActivity errors";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        mToolbar = findViewById(R.id.follow_toolbar);
        mAuth = FirebaseAuth.getInstance();


        String toolbarTitle = getIntent().getStringExtra("title");
        Log.i("title",toolbarTitle);
        mToolbar.setTitle(toolbarTitle);
        String user_id = getIntent().getStringExtra("whichUser");


        if (mAuth.getCurrentUser().getUid().equals(user_id)){
            myAccount = true;
        } else {
            myAccount = false;
        }



        mToolbar.setElevation(10.0f);
        mToolbar.setTitleTextAppearance(FollowActivity.this, R.style.TitleBarTextAppearance);
        mToolbar.setTitleTextColor(ContextCompat.getColor(FollowActivity.this,R.color.black));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        users_list = new ArrayList<>();
        mUsersList = (RecyclerView) findViewById(R.id.follow_list);
        usersRecyclerAdapter = new FollowAdapter(users_list,myAccount);
        mUsersList.setLayoutManager(new LinearLayoutManager(FollowActivity.this));
        mUsersList.setAdapter(usersRecyclerAdapter);

        mUsersList.addItemDecoration(new DividerItemDecoration(FollowActivity.this,
                DividerItemDecoration.VERTICAL));

        firebaseFirestore = FirebaseFirestore.getInstance();



            firebaseFirestore.collection("SocialStatus/" + user_id + "/"+toolbarTitle).addSnapshotListener(FollowActivity.this,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

//                    if (!documentSnapshots.isEmpty()){
//                        for (DocumentSnapshot doc : documentSnapshots){
//                            users_list.add(doc.getId());
//                            usersRecyclerAdapter.notifyDataSetChanged();
//                        }
//                    }

                    if (!documentSnapshots.isEmpty()){
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                users_list.add(doc.getDocument().getId());
                                usersRecyclerAdapter.notifyDataSetChanged();
                            }


                        }
                    }

                }
            });

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //    @Override
//    protected void onStart() {
//        super.onStart();
//        mUserRef.child("online").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.i("online","updated");
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mUserRef.child("online").setValue("true");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mUserRef.child("online").setValue(System.currentTimeMillis());
//    }

}
