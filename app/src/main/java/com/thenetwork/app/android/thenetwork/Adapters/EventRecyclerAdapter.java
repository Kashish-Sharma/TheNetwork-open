package com.thenetwork.app.android.thenetwork.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.thenetwork.app.android.thenetwork.Activities.DetailEventActivity;
import com.thenetwork.app.android.thenetwork.Activities.ProfileActivity;
import com.thenetwork.app.android.thenetwork.HelperClasses.Event;
import com.thenetwork.app.android.thenetwork.HelperUtils.BlurTransformation;
import com.thenetwork.app.android.thenetwork.HelperUtils.GetTimeAgo;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Kashish on 16-06-2018.
 */

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder> implements Filterable {

    private List<Event> event_list;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;


    private RequestManager glide;


    private List<Event> eventListFiltered;

    private static final String TAG = "blogAdapter errors";

    public EventRecyclerAdapter(Context context,List<Event> event_list, RequestManager glide){
        this.event_list = event_list;
        this.context = context;
        this.eventListFiltered = event_list;
        this.glide = glide;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_event_item,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventViewHolder holder, int position) {

        Event event = eventListFiltered.get(position);
        final String eventId = event.EventId;

        String user_id = event.getUser_id();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        final String eventImageUrl = event.getImage_url();
        final String eventThumbUrl = event.getThumb_url();
        final String eventTitle = event.getTitle();
        final String eventDateFrom = event.getEventDateFrom();
        final String eventDateTo = event.getEventDateTo();
        final String eventPlaceId = event.getEventPlaceId();
        final Boolean isSingle = event.getIsSingle();

        firebaseFirestore.collection("Users").document(user_id)
                .addSnapshotListener((Activity) context,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }
                if (documentSnapshot.exists()) {
                    String userImageUrl = documentSnapshot.get("image").toString();
                    String userNameData = documentSnapshot.get("name").toString();
                    if (userNameData.isEmpty()) {
                        userNameData = "No name";
                    }
                    holder.userName.setText(userNameData);
                    GlideLoadImage.loadSmallImage(context,holder.userImage,userImageUrl,userImageUrl);
                } else {
                    Log.w("USER_DETAIL", "Empty DOC");
                }
            }
        });

        GlideLoadImage.loadImage(context,holder.eventImage,eventThumbUrl,eventImageUrl);

        holder.eventTitleView.setText(eventTitle);
        holder.eventStartDateView.setText(eventDateFrom);
        if (!isSingle){
            holder.eventEndDateView.setVisibility(View.VISIBLE);
            holder.eventEndDateView.setText(eventDateTo);
        }


        long millisecond = event.getTimestamp();
        String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
        String timeString = DateFormat.format("HH:mm", new Date(millisecond)).toString();
        holder.postTime.setText(timeString);

        if (System.currentTimeMillis() - event.getTimestamp() < 86400 * 1000) {
            String lastSeenTime = GetTimeAgo.getTimeAgo(millisecond, context);
            holder.postDate.setText(lastSeenTime);
        } else {
            holder.postDate.setText(dateString);
        }


    }

    @Override
    public int getItemCount() {
        return eventListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                Log.i("SearchInfo","Called");
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    eventListFiltered = event_list;
                } else {
                    List<Event> filteredList = new ArrayList<>();
                    for (Event row : event_list) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                            Log.i("SearchInfo","Yes added");
                        }
                    }
                    eventListFiltered = filteredList;
                    Log.i("SearchInfo",eventListFiltered.size()+" is the filter size");
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = eventListFiltered;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                eventListFiltered = (List<Event>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {

        //user details
        private CircleImageView userImage;
        private TextView userName, postDate, postTime;
        private ConstraintLayout constraintLayout;

        //event details
        private KenBurnsView eventImage;
        private TextView eventStartDateView, eventTitleView, eventEndDateView;


        public EventViewHolder(View itemView) {
            super(itemView);

            //user details
            userImage = itemView.findViewById(R.id.event_user_image);
            userName = itemView.findViewById(R.id.event_user_name);
            postDate = itemView.findViewById(R.id.post_date);
            postTime = itemView.findViewById(R.id.post_time);
            constraintLayout = itemView.findViewById(R.id.constraintLayout2);

            //event details
            eventImage = itemView.findViewById(R.id.event_image);
            eventStartDateView = itemView.findViewById(R.id.event_start_date);
            eventEndDateView = itemView.findViewById(R.id.event_end_date);
            eventTitleView = itemView.findViewById(R.id.event_title);

            eventImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Event event = eventListFiltered.get(position);
                        Intent intent = new Intent(context, DetailEventActivity.class);
                        intent.putExtra("userId",event.getUser_id());
                        intent.putExtra("placeId",event.getEventPlaceId());
                        intent.putExtra("eventId",event.EventId);
                        intent.putExtra("startDate",event.getEventDateFrom());
                        intent.putExtra("endDate",event.getEventDateTo());
                        intent.putExtra("imageUrl",event.getImage_url());
                        intent.putExtra("time",event.getTimestamp()+"");
                        intent.putExtra("contact",event.getEventContact()+"");
                        context.startActivity(intent);
                    }

                }
            });

            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Event event = eventListFiltered.get(position);
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("user_id",event.getUser_id());
                        context.startActivity(intent);
                    }
                }
            });

        }
    }

}
