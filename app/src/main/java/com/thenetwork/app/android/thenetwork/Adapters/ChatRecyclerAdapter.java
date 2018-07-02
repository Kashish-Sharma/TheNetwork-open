package com.thenetwork.app.android.thenetwork.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thenetwork.app.android.thenetwork.Activities.ChatActivity;
import com.thenetwork.app.android.thenetwork.HelperClasses.Conv;
import com.thenetwork.app.android.thenetwork.HelperUtils.DateUtils;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ChatHolder> implements Filterable{

    private static final String TAG = "ChatActivity";
    private Context context;
    private List<Conv> users_list;
    private List<Conv> users_list_filtered;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private static final String SENDING = "sending";
    private static final String SENT = "sent";
    private static final String SEEN = "seen";


    public ChatRecyclerAdapter(List<Conv> users_list, Context context){
        this.users_list = users_list;
        this.context = context;
        this.users_list_filtered = users_list;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout,parent,false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatHolder holder, int position) {

        firebaseFirestore = FirebaseFirestore.getInstance();
        final Conv object = users_list_filtered.get(holder.getAdapterPosition());
        mAuth = FirebaseAuth.getInstance();

        //New Messages count
        firebaseFirestore.collection("Messages").document(mAuth.getCurrentUser().getUid())
                .collection(object.getUserId())
                .whereEqualTo("from",object.getUserId())
                .whereEqualTo("seen",false)
                //.orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((Activity) context,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ChatAdapter", "listen:error", e);
                    return;
                }
                if (!queryDocumentSnapshots.isEmpty()){
                    holder.newMessageCount.setVisibility(View.VISIBLE);
                    holder.newMessageCount.setText(queryDocumentSnapshots.size()+"");
                    Log.i(TAG,String.valueOf(queryDocumentSnapshots.size())+"");
                } else {
                    holder.newMessageCount.setVisibility(View.GONE);
                }
            }
        });

            // Showing the last message in chat fragment, if it is from other user then checking whether it's seen or not.
                   final Query lastMessageQuery = firebaseFirestore.collection("Messages")
                           .document(mAuth.getCurrentUser().getUid())
                           .collection(object.getUserId()).orderBy("time", Query.Direction.DESCENDING).limit(1);
                   lastMessageQuery.addSnapshotListener((Activity) context,new EventListener<QuerySnapshot>() {
                       @Override
                       public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                           if (e != null) {
                               Log.w("ChatRecyclerAdapter", "listen:error", e);
                               return;
                           }

                           if (!documentSnapshots.isEmpty()){
                               DocumentSnapshot documentSnapshot = documentSnapshots.getDocuments().get(0);
                                long time = documentSnapshot.getLong("time");
                               String timeString = DateFormat.format("HH:mm", new Date(time)).toString();
                               String dateStringString = DateFormat.format("dd/MM/yyyy", new Date(time)).toString();

                               holder.lastMessageTime.setText(DateUtils.formatDateTime(time));

                               int type = Integer.parseInt(documentSnapshot.get("type").toString());

                               if (documentSnapshot.getString("from").equals(mAuth.getCurrentUser().getUid())){
                                   if (type == 1){
                                       holder.userStatus.setText("photo");
                                   } else if (type == 2){
                                       holder.userStatus.setText(documentSnapshot.getString("imageText"));
                                   } else if (type == 0){
                                       holder.userStatus.setText(documentSnapshot.getString("message"));
                                   } else {
                                       holder.userStatus.setText("");
                                   }

                               } else {

                                   if (type == 1){
                                       holder.userStatus.setText("photo");
                                   } else if (type == 0){
                                       holder.userStatus.setText(documentSnapshot.getString("message"));
                                   } else if (type == 2){
                                       holder.userStatus.setText(documentSnapshot.getString("imageText"));
                                   } else {
                                       holder.userStatus.setText("");
                                   }
                                   
                                   if (documentSnapshot.getBoolean("seen")){
                                       holder.newMessageCount.setVisibility(View.INVISIBLE);
                                       holder.userStatus.setTextColor(ContextCompat.getColor(context,R.color.grey));
                                       holder.userStatus.setTypeface(null, Typeface.NORMAL);
                                       holder.lastMessageTime.setTextColor(ContextCompat.getColor(context,R.color.grey));
                                   } else {
                                       holder.userStatus.setTextColor(ContextCompat.getColor(context,R.color.black));
                                       holder.userStatus.setTypeface(null, Typeface.BOLD);
                                       holder.lastMessageTime.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
                                   }
                               }

                           } else if (documentSnapshots.isEmpty()){
                               holder.userStatus.setText("");
                           }
                       }
                   });

                   firebaseFirestore.collection("Users").document(object.getUserId())
                           .addSnapshotListener((Activity) context,new EventListener<DocumentSnapshot>() {
                               @Override
                               public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                   if (e != null) {
                                       Log.w("ChatRecyclerAdapter", "listen:error", e);
                                       return;
                                   }

                                   holder.userName.setText(documentSnapshot.getString("name"));

                                   GlideLoadImage.loadSmallImage(context,holder.userImage,
                                           documentSnapshot.getString("image"),documentSnapshot.getString("image"));
                                   final String user_id = object.getUserId();

                                   holder.mView.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           Intent chatIntent = new Intent(context,ChatActivity.class);
                                           chatIntent.putExtra("user_id",user_id);
                                           chatIntent.putExtra("name",documentSnapshot.getString("name"));
                                           chatIntent.putExtra("image",documentSnapshot.getString("image"));
                                           context.startActivity(chatIntent);
                                       }
                                   });

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
                    List<Conv> filteredList = new ArrayList<>();
                    for (Conv row : users_list) {
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
                users_list_filtered = (List<Conv>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ChatHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView userName, userStatus, newMessageCount;
        CircleImageView userImage;
        TextView lastMessageTime;
        RelativeLayout viewBackground;
        public ConstraintLayout viewForeground;

        public ChatHolder(View itemView) {
            super(itemView);
            mView = itemView;
            userName = itemView.findViewById(R.id.userSingleName);
            userStatus = itemView.findViewById(R.id.userSingleStatus);
            userImage = itemView.findViewById(R.id.userSingleImage);
            newMessageCount = itemView.findViewById(R.id.newMessageCount);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }

    }

}
