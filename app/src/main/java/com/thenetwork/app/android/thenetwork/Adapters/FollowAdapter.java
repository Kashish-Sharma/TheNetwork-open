package com.thenetwork.app.android.thenetwork.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.thenetwork.app.android.thenetwork.Activities.ChatActivity;
import com.thenetwork.app.android.thenetwork.Activities.ProfileActivity;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Kashish on 18-03-2018.
 */

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.FollowViewHolder> {


    private Context context;
    private List<String> userList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Boolean myAccount;
    private Dialog mDialog;

    private TextView dName, dMail;
    private CircleImageView dImage;
    private Button dProfile, dChat;

    public FollowAdapter(List<String> userList, Boolean myAccount){
        this.userList = userList;
        this.myAccount = myAccount;
    }

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final FollowViewHolder vHolder = new FollowViewHolder(view);

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FollowViewHolder holder, int position) {

        final String userId = userList.get(holder.getAdapterPosition());
        String userName;
        final CharSequence options[] = {"Open Profile","Send Message"};

        firebaseFirestore.collection("Users").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w("follow_adapter", "listen:error", e);
                    return;
                }
                if (documentSnapshot.exists()){
                    holder.userName.setText(documentSnapshot.getString("name"));
                    holder.userStatus.setText(documentSnapshot.getString("about"));
                    holder.userName.setBackground(null);
                    holder.userStatus.setBackground(null);
                    final String name = documentSnapshot.getString("name");
                    final String userImageUrl = documentSnapshot.getString("image");

                    if (myAccount){
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i==0){
                                            Intent profileIntent = new Intent(context, ProfileActivity.class);
                                            profileIntent.putExtra("user_id",userId);
                                            context.startActivity(profileIntent);
                                        }
                                        if (i==1){
                                            Intent chatIntent = new Intent(context, ChatActivity.class);
                                            chatIntent.putExtra("user_id",userId);
                                            chatIntent.putExtra("name",name);
                                            chatIntent.putExtra("image",userImageUrl);
                                            context.startActivity(chatIntent);
                                        }
                                    }
                                }).show();
                            }
                        });
                    }

                    GlideLoadImage.loadSmallImage(context,holder.userImage,userImageUrl,userImageUrl);
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class FollowViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView userName, userStatus;
        CircleImageView userImage;

        public FollowViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            userName = (TextView) itemView.findViewById(R.id.userSingleName);
            userStatus = (TextView) itemView.findViewById(R.id.userSingleStatus);
            userImage = (CircleImageView) itemView.findViewById(R.id.userSingleImage);
        }
    }

}
