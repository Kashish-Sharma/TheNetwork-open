package com.thenetwork.app.android.thenetwork.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.thenetwork.app.android.thenetwork.Activities.ProfileActivity;
import com.thenetwork.app.android.thenetwork.HelperClasses.RequestItem;
import com.thenetwork.app.android.thenetwork.HelperClasses.Users;
import com.thenetwork.app.android.thenetwork.HelperUtils.DateUtils;
import com.thenetwork.app.android.thenetwork.HelperUtils.GetTimeAgo;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Kashish on 19-06-2018.
 */

public class RequestRecyclerAdapter extends RecyclerView.Adapter<RequestRecyclerAdapter.RequestHolder> implements Filterable {

    private static final String TAG = "ChatActivity";
    private Context context;
    private List<RequestItem> users_list;
    private List<RequestItem> users_list_filtered;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private CollectionReference mRequestReference;

    public RequestRecyclerAdapter(List<RequestItem> users_list, Context context){
        this.users_list = users_list;
        this.context = context;
        this.users_list_filtered = users_list;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_request_item,parent,false);
        return new RequestHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestHolder holder,int position) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        mRequestReference = firebaseFirestore.collection("Requests");
        final RequestItem object = users_list_filtered.get(holder.getAdapterPosition());
        mAuth = FirebaseAuth.getInstance();

        firebaseFirestore.collection("Users").document(object.requestItemUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    Users users = documentSnapshot.toObject(Users.class);
                    holder.userName.setText(users.getName());

                    GlideLoadImage.loadSmallImage(context,holder.userImage,users.getImage(),users.getImage());
                    holder.lastMessageTime.setText(DateUtils.formatDateTime(object.getTimestamp()));

                }

            }
        });

        final DocumentReference documentReference = firebaseFirestore.collection("Requests")
                .document(mAuth.getCurrentUser().getUid())
                .collection("request-recieved").document(object.requestItemUserId);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshot.exists()){
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        removeItem(position);
                    }
                }

            }
        });



    }

    public void removeItem(int position) {
        users_list_filtered.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return users_list_filtered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                Log.i("SearchInfo","Called");
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    users_list_filtered = users_list;
                } else {
                    List<RequestItem> filteredList = new ArrayList<>();
                    for (RequestItem row : users_list) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                            Log.i("SearchInfo","Yes added");
                        }
                    }
                    users_list_filtered = filteredList;
                    Log.i("SearchInfo",users_list_filtered.size()+" is the filter size");
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = users_list_filtered;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                users_list_filtered = (List<RequestItem>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class RequestHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView userName;
        CircleImageView userImage;
        TextView lastMessageTime;
        Button acceptRequest, cancelRequest;
        ProgressBar acceptProgress, cancelProgress;

        public RequestHolder(View itemView) {
            super(itemView);
            mView = itemView;
            userName = itemView.findViewById(R.id.request_name_frag);
            userImage = itemView.findViewById(R.id.userSingleImageFrag);
            lastMessageTime = itemView.findViewById(R.id.reqTime);
            acceptRequest = itemView.findViewById(R.id.accept_request_btn_frag);
            cancelRequest = itemView.findViewById(R.id.cancel_request_btn_frag);
            acceptProgress = itemView.findViewById(R.id.acceptProgressFrag);
            cancelProgress = itemView.findViewById(R.id.cancelProgressFrag);

            userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        RequestItem requestItem = users_list_filtered.get(position);
                        String userId = requestItem.requestItemUserId;
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("user_id",userId);
                        context.startActivity(intent);
                    }
                }
            });

            acceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        RequestItem requestItem = users_list_filtered.get(position);
                        String userId = requestItem.requestItemUserId;
                        acceptRequest(mAuth.getCurrentUser().getUid(),userId,acceptRequest,cancelRequest,acceptProgress);
                    }
                }
            });

            cancelRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        RequestItem requestItem = users_list_filtered.get(position);
                        String userId = requestItem.requestItemUserId;
                        cancelRequest(mAuth.getCurrentUser().getUid(),userId,acceptRequest,cancelRequest,cancelProgress);
                    }
                }
            });


        }

    }

    private void acceptRequest(final String currentUserId , final String otherUserId
            , final Button acceptRequest, final Button cancelRequest, final ProgressBar acceptProgress){

        cancelRequest.setVisibility(View.GONE);
        acceptRequest.setVisibility(View.GONE);
        acceptProgress.setVisibility(View.VISIBLE);


        mRequestReference.document(currentUserId)
                .collection("request-recieved").document(otherUserId).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                                        acceptRequest.setVisibility(View.VISIBLE);
                                                                        cancelRequest.setVisibility(View.VISIBLE);
                                                                        acceptProgress.setVisibility(View.GONE);

                                                                    } else {
                                                                        Toast.makeText(context,R.string.follow_error_message,Toast.LENGTH_SHORT).show();
                                                                        acceptProgress.setVisibility(View.GONE);
                                                                        acceptRequest.setVisibility(View.VISIBLE);
                                                                        cancelRequest.setVisibility(View.VISIBLE);

                                                                    }

                                                                }
                                                            });

                                                        } else {
                                                            Toast.makeText(context,R.string.follow_error_message,Toast.LENGTH_SHORT).show();
                                                            acceptProgress.setVisibility(View.GONE);
                                                            acceptRequest.setVisibility(View.VISIBLE);
                                                            cancelRequest.setVisibility(View.VISIBLE);

                                                        }
                                                    }
                                                });

                                    } else {

                                        Toast.makeText(context,R.string.follow_error_message,Toast.LENGTH_SHORT).show();

                                        acceptProgress.setVisibility(View.GONE);
                                        acceptRequest.setVisibility(View.VISIBLE);
                                        cancelRequest.setVisibility(View.VISIBLE);

                                    }

                                }
                            });

                        }  else {

                            Toast.makeText(context,R.string.follow_error_message,Toast.LENGTH_SHORT).show();

                            acceptProgress.setVisibility(View.GONE);
                            acceptRequest.setVisibility(View.VISIBLE);
                            cancelRequest.setVisibility(View.VISIBLE);

                        }

                    }
                });
    }

    private void cancelRequest(final String currentUserId , final String otherUserId
            , final Button acceptRequest, final Button cancelRequest, final ProgressBar cancelProgress){

        cancelRequest.setVisibility(View.GONE);
        acceptRequest.setVisibility(View.GONE);
        cancelProgress.setVisibility(View.VISIBLE);

        mRequestReference.document(currentUserId)
                .collection("request-recieved").document(otherUserId).delete()
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            mRequestReference.document(otherUserId)
                                    .collection("request-sent").document(currentUserId).delete()
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                cancelProgress.setVisibility(View.GONE);
                                                cancelRequest.setVisibility(View.VISIBLE);
                                                acceptRequest.setVisibility(View.VISIBLE);


                                            } else {
                                                Toast.makeText(context,R.string.follow_error_message,Toast.LENGTH_SHORT).show();

                                                cancelProgress.setVisibility(View.GONE);
                                                cancelRequest.setVisibility(View.VISIBLE);
                                                acceptRequest.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(context,R.string.follow_error_message,Toast.LENGTH_SHORT).show();

                            cancelProgress.setVisibility(View.GONE);
                            cancelRequest.setVisibility(View.VISIBLE);
                            acceptRequest.setVisibility(View.VISIBLE);
                        }
                    }
                });


    }

}
