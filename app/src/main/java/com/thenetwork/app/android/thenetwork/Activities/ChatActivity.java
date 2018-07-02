package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thenetwork.app.android.thenetwork.Adapters.MessageAdapter;
import com.thenetwork.app.android.thenetwork.Fragments.ChatFragment;
import com.thenetwork.app.android.thenetwork.HelperClasses.RecyclerItemClickListener;
import com.thenetwork.app.android.thenetwork.HelperUtils.GetTimeAgo;
import com.thenetwork.app.android.thenetwork.HelperClasses.Messages;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.HelperUtils.RecyclerItemTouchHelper;
import com.thenetwork.app.android.thenetwork.NotificationUtils.Message;
import com.thenetwork.app.android.thenetwork.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.TEXT_TYPE;

public class ChatActivity extends AppCompatActivity  {

    private String mChatUser, mChatUserImage;
    private String mChatUserName;
    private FirebaseAuth mAuth;


    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImage;

    private String mCurrentUserId;

    private static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int GALLERY_PICK = 77;

    private static final String SENDING = "sending";
    private static final String SENT = "sent";
    private static final String SEEN = "seen";


    private ImageButton mChatAddBtn;
    private ImageButton mChatSendButton;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private Bitmap compressedImageFile;
    private Bitmap compressedThumbFile;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private DatabaseReference mUserRef;
    private DatabaseReference mChatRef;

    private LayoutInflater mInflater;
    private View mActionBarView;
    private ActionBar mActionBar;

    private List<String> selectedIds = new ArrayList<>();
    private Map<String,Integer> positionMap = new HashMap<>();
    private boolean isMultiSelect = false;

    private Boolean mCheckSuccess = true;
    private int loopCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setTitle("");
        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("name");
        mChatUserImage = getIntent().getStringExtra("image");
        mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = mInflater.inflate(R.layout.chat_custom_bar,null);
        mActionBar.setCustomView(mActionBarView);
        mActionBar.setElevation(10.0f);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendButton = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        //firebase
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mLastSeenView = findViewById(R.id.last_seen_custom_bar);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mChatUser);

        mUserRef.child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.getValue().toString();
                if (online.equals("true")){
                    mLastSeenView.setText("Online");
                } else {
                    if (System.currentTimeMillis()-Long.parseLong(online) > 86400000){

                        Date date = new Date(Long.parseLong(online));
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        mLastSeenView.setText(formatter.format(date));
                    } else {
                        String lastSeenTime = GetTimeAgo.getTimeAgo(Long.parseLong(online),ChatActivity.this);
                        mLastSeenView.setText(lastSeenTime);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        // Custom actionbar items
        mTitleView = (TextView) findViewById(R.id.name_custom_bar);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mTitleView.setText(mChatUserName);

        GlideLoadImage.loadSmallImage(ChatActivity.this,mProfileImage,mChatUserImage,mChatUserImage);

        RequestManager glide = Glide.with(ChatActivity.this);

        mAdapter = new MessageAdapter(messagesList,mChatUserName,glide,ChatActivity.this,mCurrentUserId, mChatUser);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(ChatActivity.this);
        mLinearLayout.setReverseLayout(true);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);





        //Updating the chat fragment list, as the user with latest chat should come at top
        firebaseFirestore.collection("Chat").document(mCurrentUserId)
                .collection("usernames").document(mChatUser).update("timestamp",System.currentTimeMillis());

        //Marking all messages as seen
        firebaseFirestore.collection("Messages")
                .document(mCurrentUserId).collection(mChatUser)
                .whereEqualTo("seen",false)
                .addSnapshotListener(ChatActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot documentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w("ChatActivity", "listen:error", e);
                            return;
                        }
                        for (final DocumentSnapshot doc : documentSnapshots.getDocuments()) {
                            DocumentReference documentReference = doc.getReference();
                            documentReference.update("seen",true);
                        }

                    }
                });

        loadMessages();

//      Shift recyclerview accordingly when the new message is added
        mMessagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i3 < i7){
                    mMessagesList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (messagesList.size() > 0){
                                mMessagesList.scrollToPosition(0);
                            }
                        }
                    },0);
                }
            }
        });

        mMessagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean reachedTop = !mMessagesList.canScrollVertically(-1);

                if (reachedTop){
                    loadMoreMessages();
                }

            }
        });

        //Opening gallery for selecting image
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                bringImagePicker();
            }
        });

        firebaseFirestore.collection("Chat").document(mCurrentUserId)
                .collection("usernames").document(mChatUser)
                .addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (!documentSnapshot.exists()){
                    Map<String,Object> chatAddMap = new HashMap<>();
                    chatAddMap.put("seen",true);
                    chatAddMap.put("timestamp",System.currentTimeMillis());
                    chatAddMap.put("userId",mChatUser);
                    chatAddMap.put("name",mChatUserName);

                    firebaseFirestore.collection("Users").document(mCurrentUserId).get()
                            .addOnSuccessListener(ChatActivity.this,
                            new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final Map<String,Object> chatUserAddMap = new HashMap<>();
                                    chatUserAddMap.put("seen",false);
                                    chatUserAddMap.put("timestamp",System.currentTimeMillis());
                                    chatUserAddMap.put("userId",mCurrentUserId);
                                    chatUserAddMap.put("name",documentSnapshot.getString("name"));
                                    firebaseFirestore.collection("Chat").document(mChatUser)
                                            .collection("usernames").document(mCurrentUserId).set(chatUserAddMap);
                                }
                            });

                    firebaseFirestore.collection("Chat").document(mCurrentUserId)
                            .collection("usernames").document(mChatUser).set(chatAddMap);

                }

            }
        });

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendMessage();
            }
        });
        mMessagesList.scrollToPosition(messagesList.size()-1);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:

        }
        return false;
    }

    private void loadMessages(){
        Query loadMessageQuery = firebaseFirestore.collection("Messages").document(mCurrentUserId)
                .collection(mChatUser).orderBy("time", Query.Direction.DESCENDING).limit(20);
        loadMessageQuery.addSnapshotListener(ChatActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("CHAT_ACTIVITY", "listen:error", e);
                    return;
                }
                if (!documentSnapshots.isEmpty()) {

                    if (isFirstPageFirstLoad){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String messageId = doc.getDocument().getId();
                            Messages message = doc.getDocument().toObject(Messages.class).withId(messageId);

                            if (isFirstPageFirstLoad){
                                messagesList.add(message);
                                mAdapter.notifyItemInserted(messagesList.size());
                            } else {
                                messagesList.add(0,message);
                                mAdapter.notifyItemInserted(0);
                            }
                            mMessagesList.scrollToPosition(0);
                        }


                    }

                    isFirstPageFirstLoad = false;
                }
            }
        });

    }


    private void loadMoreMessages(){
        Query nextQuery = firebaseFirestore.collection("Messages").document(mCurrentUserId)
                .collection(mChatUser)
                .orderBy("time", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(20);
        nextQuery.addSnapshotListener(ChatActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("CHAT_ACTIVITY", "listen:error", e);
                    return;
                }
                if (!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String messageId = doc.getDocument().getId();
                            Messages message = doc.getDocument().toObject(Messages.class).withId(messageId);
                            messagesList.add(message);
                            mAdapter.notifyItemInserted(messagesList.size());
                        }
                    }
                }
            }
        });
    }


    private void sendMessage(){
        String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)){

            Map<String,Object> messageMap = new HashMap<>();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type",TEXT_TYPE);
            messageMap.put("time",System.currentTimeMillis());
            messageMap.put("from",mCurrentUserId);
            messageMap.put("image","");
            messageMap.put("thumb","");
            messageMap.put("imageText","");
            messageMap.put("path","");
            messageMap.put("filename","");


            mChatMessageView.setText("");

            firebaseFirestore.collection("Messages").document(mCurrentUserId).collection(mChatUser).add(messageMap);
            firebaseFirestore.collection("Messages").document(mChatUser).collection(mCurrentUserId).add(messageMap);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Intent selectImageIntent = new Intent(ChatActivity.this, selectImageActivity.class);
                selectImageIntent.putExtra("uri",result.getUri().toString());
                selectImageIntent.putExtra("currentUser",mCurrentUserId);
                selectImageIntent.putExtra("chatUser",mChatUser);
                selectImageIntent.putExtra("name",mChatUserName);
                selectImageIntent.putExtra("image",mChatUserImage);
                startActivity(selectImageIntent);
                finish();
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
                .start(this);
    }

}
