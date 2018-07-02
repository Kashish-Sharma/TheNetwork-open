package com.thenetwork.app.android.thenetwork.Activities;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thenetwork.app.android.thenetwork.Adapters.AllUsersAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.RecyclerItemClickListener;
import com.thenetwork.app.android.thenetwork.HelperClasses.Users;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.List;

public class AllUsersActivity extends AppCompatActivity{


    private static final String EXTRA_IMAGE = " com.thenetwork.app.android.thenetwork.Activities.extraImage";
    private static final String EXTRA_TITLE = " com.thenetwork.app.android.thenetwork.Activities.extraTitle";

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    private List<Users> users_list;
    private AllUsersAdapter usersRecyclerAdapter;


    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private static final String TAG = "AllUserActivity_errors";

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private List<String> selectedIds = new ArrayList<>();
    private boolean isMultiSelect = false;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

//        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_IMAGE);
//        supportPostponeEnterTransition();

        mToolbar = (Toolbar) findViewById(R.id.all_users_toolbar);
        mToolbar.setTitle("All Users");
        mToolbar.setTitleTextAppearance(AllUsersActivity.this, R.style.TitleBarTextAppearance);
        mToolbar.setTitleTextColor(ContextCompat.getColor(AllUsersActivity.this,R.color.black));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setElevation(10.0f);

        firebaseFirestore = FirebaseFirestore.getInstance();

        users_list = new ArrayList<>();
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        usersRecyclerAdapter = new AllUsersAdapter(users_list);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersList.setAdapter(usersRecyclerAdapter);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        mUsersList.addOnItemTouchListener(new RecyclerItemClickListener(this
                , mUsersList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect){
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

                invalidateOptionsMenu();
                Log.i(TAG,position+" is selected");

                if (!isMultiSelect){
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;
                }

                multiSelect(position);
            }
        }));




           mUsersList.addItemDecoration(new DividerItemDecoration(AllUsersActivity.this,
                DividerItemDecoration.VERTICAL));

        mUsersList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                if (reachedBottom){
                    loadMoreUsers();
                }

            }
        });

        if (currentUser.isEmailVerified()){
            Query firstQuery = firebaseFirestore.collection("Users").orderBy("name",Query.Direction.ASCENDING).limit(20);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    if (documentSnapshots!=null){

                        if (isFirstPageFirstLoad){
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                            if (doc.getType() == DocumentChange.Type.ADDED){

                                Users users = doc.getDocument().toObject(Users.class);
                                users_list.add(users);
                                Log.i("USERS","ADDED");
                                Log.i("USERS","Name" + users.getName());
                                usersRecyclerAdapter.notifyDataSetChanged();

                            }

                        }
                        isFirstPageFirstLoad = false;
                    }

                }
            });
        }

    }


    private void multiSelect(int position) {
        Users data = usersRecyclerAdapter.getItem(position);
        if (data != null){
            if (selectedIds.contains(data.getUser_id())) {
                selectedIds.remove(data.getUser_id());
            }
            else
                selectedIds.add(data.getUser_id());

            if (selectedIds.size() > 0)
                getSupportActionBar().setTitle(String.valueOf(selectedIds.size())); //show selected item count on action mode.
            else{
                isMultiSelect = false;
                selectedIds = new ArrayList<>();
                usersRecyclerAdapter.setSelectedIds(new ArrayList<String>());
                getSupportActionBar().setTitle("All users"); //remove item count from action mode.
            }
            usersRecyclerAdapter.setSelectedIds(selectedIds);


        }
    }

    private void loadMoreUsers(){

        if (currentUser.isEmailVerified()){
            Query nextQuery = firebaseFirestore.collection("Users")
                    .orderBy("name",Query.Direction.ASCENDING)
                    .startAfter(lastVisible)
                    .limit(20);

            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    if (!documentSnapshots.isEmpty()){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                            if (doc.getType() == DocumentChange.Type.ADDED){

                                Users users = doc.getDocument().toObject(Users.class);
                                users_list.add(users);

                                usersRecyclerAdapter.notifyDataSetChanged();

                            }

                        }
                    }

                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            mMenu = menu;
            menu.clear();
            if (selectedIds.size()>0){
                getMenuInflater().inflate(R.menu.menu_select, menu);
            } else {
                getMenuInflater().inflate(R.menu.new_post_menu, menu);
            }
            return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                //just to show selected items.
                StringBuilder stringBuilder = new StringBuilder();
                for (Users data : users_list) {
                    if (selectedIds.contains(data.getUser_id()))
                        stringBuilder.append("\n").append(data.getName());
                }
                Toast.makeText(this, "Selected items are :" + stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        if (isMultiSelect){
            getSupportActionBar().setTitle("All users");
            isMultiSelect = false;
            selectedIds = new ArrayList<>();
            invalidateOptionsMenu();
            usersRecyclerAdapter.setSelectedIds(new ArrayList<String>());
        } else {
            super.onBackPressed();
        }
    }
}
