package com.thenetwork.app.android.thenetwork.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
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
import com.thenetwork.app.android.thenetwork.Activities.FullScreenImageActivity;
import com.thenetwork.app.android.thenetwork.HelperClasses.Messages;
import com.thenetwork.app.android.thenetwork.HelperUtils.DateUtils;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.IMAGE_TEXT_TYPE;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.IMAGE_TEXT_TYPE_OTHER;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.IMAGE_TEXT_TYPE_USER;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.IMAGE_TYPE;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.IMAGE_TYPE_OTHER;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.IMAGE_TYPE_USER;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.TEXT_TYPE;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.TEXT_TYPE_OTHER;
import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.TEXT_TYPE_USER;


public class MessageAdapter extends RecyclerView.Adapter {

    private static final String TAG = "messageAdapte";
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String chatUserName;
    private  String type;
    private RequestManager glide;
    private Context context;
    private String currentUserId;
    private List<String> selectedIds = new ArrayList<>();
    private String mChatUser;

    public MessageAdapter(List<Messages> mMessageList, String chatUserName
            , RequestManager glide, Context context, String currentUserId, String mChatUser){
        this.mMessageList = mMessageList;
        this.chatUserName = chatUserName;
        this.glide = glide;
        this.context = context;
        this.currentUserId = currentUserId;
        this.mChatUser = mChatUser;
    }

    public void setSelectedIds(List<String> selectedIds) {
        this.selectedIds = selectedIds;
        notifyDataSetChanged();
    }
    public Messages getItem(int position){
        return mMessageList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mAuth = FirebaseAuth.getInstance();
        View view;
        switch (viewType) {
            case TEXT_TYPE_OTHER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
                return new TextMessageViewHolder(view);
            case TEXT_TYPE_USER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout_user, parent, false);
                return new TextMessageViewHolderUser(view);
            case IMAGE_TYPE_OTHER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_image_single_layout, parent, false);
                return new ImageMessageViewHolder(view);
            case IMAGE_TYPE_USER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_image_single_layout_user, parent, false);
                return new ImageMessageViewHolderUser(view);
            case IMAGE_TEXT_TYPE_OTHER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_image_single_layout_with_text, parent, false);
                return new ImageMessageWithTextViewHolder(view);
            case IMAGE_TEXT_TYPE_USER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_image_single_layout_with_text_user, parent, false);
                return new ImageMessageWithTextViewHolderUser(view);
        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        switch (mMessageList.get(position).getType()){
            case TEXT_TYPE:
                if (mMessageList.get(position).getFrom().equals(currentUserId)){
                    return TEXT_TYPE_USER;}
                else{
                    return TEXT_TYPE_OTHER;}
            case IMAGE_TYPE:
                if (mMessageList.get(position).getFrom().equals(currentUserId)){
                    return IMAGE_TYPE_USER;}
                else{
                    return IMAGE_TYPE_OTHER;}
            case IMAGE_TEXT_TYPE:
                if (mMessageList.get(position).getFrom().equals(currentUserId)){
                    return IMAGE_TEXT_TYPE_USER;}
                else{
                    return IMAGE_TEXT_TYPE_OTHER;}
            default:
                return 1234567890;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void removeItem(int position) {
        mMessageList.remove(position);
        notifyItemRemoved(position);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int listPosition) {

        final Messages object = mMessageList.get(holder.getAdapterPosition());
        final String fromUser = object.getFrom();
        final String lastSeenTime = DateFormat.format("HH:mm", new Date(object.getTime())).toString();
        RequestBuilder<Drawable> thumbnailRequestBlogImage;
        thumbnailRequestBlogImage =glide.load(object.getThumb());
        Boolean isNewDay = false;



        // If there is at least one item preceding the current one, check the previous message.
        int position = holder.getAdapterPosition();
        if (position < mMessageList.size() - 1 && position>-1 ) {
            Messages prevMessage = mMessageList.get(holder.getAdapterPosition() + 1);

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(object.getTime(), prevMessage.getTime())) {
                isNewDay = true;
            }
        } else if (position == mMessageList.size() - 1) {
            isNewDay = true;
        }


            switch (holder.getItemViewType()) {
                case TEXT_TYPE_USER:

                    if (isNewDay) {
                        ((TextMessageViewHolderUser) holder).textDateUser.setVisibility(View.VISIBLE);
                        ((TextMessageViewHolderUser) holder).textDateUser.setText(DateUtils.formatDate(object.getTime()));

                    } else {
                        ((TextMessageViewHolderUser) holder).textDateUser.setVisibility(View.GONE);
                    }

                        ((TextMessageViewHolderUser) holder).messageTextUser.setText(object.getMessage());
                        ((TextMessageViewHolderUser) holder).timeUser.setText(lastSeenTime);
                    break;
                case TEXT_TYPE_OTHER:

                    if (isNewDay) {
                        ((TextMessageViewHolder) holder).textDate.setVisibility(View.VISIBLE);
                        ((TextMessageViewHolder) holder).textDate.setText(DateUtils.formatDate(object.getTime()));
                    } else {
                        ((TextMessageViewHolder) holder).textDate.setVisibility(View.GONE);
                    }

                        ((TextMessageViewHolder) holder).messageText.setText(object.getMessage());
                        ((TextMessageViewHolder) holder).time.setText(lastSeenTime);
                    break;
                case IMAGE_TYPE_USER:

                    if (isNewDay) {
                        ((ImageMessageViewHolderUser) holder).imageChatDateUser.setVisibility(View.VISIBLE);
                        ((ImageMessageViewHolderUser) holder).imageChatDateUser.setText(DateUtils.formatDate(object.getTime()));
                    } else {
                        ((ImageMessageViewHolderUser) holder).imageChatDateUser.setVisibility(View.GONE);
                    }

                    ((ImageMessageViewHolderUser) holder).imageTimeUser.setText(lastSeenTime);

                    GlideLoadImage.loadImage(context,((ImageMessageViewHolderUser) holder).messageImageUser
                            ,object.getThumb(),object.getImage());

                    break;
                case IMAGE_TYPE_OTHER:

                    if (isNewDay) {
                        ((ImageMessageViewHolder) holder).imageChatDate.setVisibility(View.VISIBLE);
                        ((ImageMessageViewHolder) holder).imageChatDate.setText(DateUtils.formatDate(object.getTime()));
                    } else {
                        ((ImageMessageViewHolder) holder).imageChatDate.setVisibility(View.GONE);
                    }

                    ((ImageMessageViewHolder) holder).imageTime.setText(lastSeenTime);

                    GlideLoadImage.loadImage(context,((ImageMessageViewHolder) holder).messageImage
                            ,object.getThumb(),object.getImage());

                    break;
                case IMAGE_TEXT_TYPE_USER:

                    if (isNewDay) {
                        ((ImageMessageWithTextViewHolderUser) holder).imageTextDateUser.setVisibility(View.VISIBLE);
                        ((ImageMessageWithTextViewHolderUser) holder).imageTextDateUser.setText(DateUtils.formatDate(object.getTime()));
                    } else {
                        ((ImageMessageWithTextViewHolderUser) holder).imageTextDateUser.setVisibility(View.GONE);
                    }

                        ((ImageMessageWithTextViewHolderUser) holder).imageTextUser.setText(object.getImageText());
                        ((ImageMessageWithTextViewHolderUser) holder).imageTimeUser.setText(lastSeenTime);

                    GlideLoadImage.loadImage(context,((ImageMessageWithTextViewHolderUser) holder).messageImageUser
                            ,object.getThumb(),object.getImage());
                    break;
                case IMAGE_TEXT_TYPE_OTHER:

                    if (isNewDay) {
                        ((ImageMessageWithTextViewHolder) holder).imageTextDate.setVisibility(View.VISIBLE);
                        ((ImageMessageWithTextViewHolder) holder).imageTextDate.setText(DateUtils.formatDate(object.getTime()));
                    } else {
                        ((ImageMessageWithTextViewHolder) holder).imageTextDate.setVisibility(View.GONE);
                    }

                        ((ImageMessageWithTextViewHolder) holder).imageText.setText(object.getImageText());
                        ((ImageMessageWithTextViewHolder) holder).imageTime.setText(lastSeenTime);

                    GlideLoadImage.loadImage(context,((ImageMessageWithTextViewHolder) holder)
                            .messageImage,object.getThumb(),object.getImage());

                    break;
            }
    }

    public class TextMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private TextView messageText;
        //private CardView cardView;
        private TextView textDate;
        private View view;
        RelativeLayout relativeLayout;

        public TextMessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.message_text_layout);
            time = view.findViewById(R.id.time_text_layout);
            textDate = view.findViewById(R.id.text_chat_date);
            //cardView = itemView.findViewById(R.id.cardView);
            view = itemView.findViewById(R.id.text_message_view);
            relativeLayout = itemView.findViewById(R.id.message_single_layout);
        }
    }


    public class TextMessageViewHolderUser extends RecyclerView.ViewHolder {
        private TextView timeUser;
        private TextView messageTextUser;
        private TextView textDateUser;
        //private CardView cardViewUser;
        private RelativeLayout relativeLayout;
        private View view;

        public TextMessageViewHolderUser(View view) {
            super(view);
            messageTextUser = view.findViewById(R.id.message_text_layout_user);
            timeUser = view.findViewById(R.id.time_text_layout_user);
            textDateUser = view.findViewById(R.id.text_chat_date_user);
            //cardViewUser = itemView.findViewById(R.id.cardViewUser);
            relativeLayout = itemView.findViewById(R.id.message_single_layout_user);
            view = itemView.findViewById(R.id.text_message_view_user);

        }
    }

    public class ImageMessageWithTextViewHolder extends RecyclerView.ViewHolder {
        private ImageView messageImage;
        //private CardView imageMessage;
        private TextView imageText;
        private TextView imageTime;
        private TextView imageTextDate;
        private View view;
        private RelativeLayout relativeLayout;

        public ImageMessageWithTextViewHolder(View itemView) {
            super(itemView);
            imageText = itemView.findViewById(R.id.imageText_with_text);
            imageTime = itemView.findViewById(R.id.time_text_layout_image_with_text);
            messageImage = itemView.findViewById(R.id.message_image_layout_with_text);
            imageTextDate = itemView.findViewById(R.id.text_image_chat_date);
            view = itemView.findViewById(R.id.text_message_image_view);
            relativeLayout = itemView.findViewById(R.id.message_image_single_layout_with_text);
            //imageMessage = itemView.findViewById(R.id.imageMessageWithText);

            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Messages message = mMessageList.get(position);
                        final String imageUrl = message.getImage();
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        intent.putExtra("image_url", imageUrl);
                        intent.putExtra("user_id", message.getFrom());
                        intent.putExtra("time", String.valueOf(message.getTime()));
                        context.startActivity(intent);
                    }
                }
            });

        }
    }

    public class ImageMessageWithTextViewHolderUser extends RecyclerView.ViewHolder {
        private ImageView messageImageUser;
        //private CardView imageMessageUser;
        private TextView imageTextUser;
        private TextView imageTimeUser;
        private TextView imageTextDateUser;
        private RelativeLayout relativeLayout;
        private View view;

        public ImageMessageWithTextViewHolderUser(View itemView) {
            super(itemView);
            imageTextUser = itemView.findViewById(R.id.imageText_with_text_user);
            imageTimeUser = itemView.findViewById(R.id.time_text_layout_image_with_text_user);
            messageImageUser = itemView.findViewById(R.id.message_image_layout_with_text_user);
            imageTextDateUser = itemView.findViewById(R.id.text_image_chat_date_user);
            view = itemView.findViewById(R.id.text_message_image_user_view);
            relativeLayout = itemView.findViewById(R.id.message_image_single_layout_with_text_user);

            messageImageUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Messages message = mMessageList.get(position);
                        final String imageUrl = message.getImage();
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        intent.putExtra("image_url", imageUrl);
                        intent.putExtra("user_id", message.getFrom());
                        intent.putExtra("time", String.valueOf(message.getTime()));
                        context.startActivity(intent);
                    }
                }
            });

        }
    }

    public class ImageMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView messageImage;
        //private CardView cardView;
        private TextView imageTime;
        private TextView imageChatDate;
        private View view;
        private RelativeLayout relativeLayout;

        public ImageMessageViewHolder(View itemView) {
            super(itemView);
            imageTime = itemView.findViewById(R.id.time_text_layout_image);
            //cardView = itemView.findViewById(R.id.imageMessage);
            messageImage = itemView.findViewById(R.id.message_image_layout);
            imageChatDate = itemView.findViewById(R.id.image_chat_date);
            view = itemView.findViewById(R.id.message_image_view);
            relativeLayout = itemView.findViewById(R.id.message_image_single_layout);

            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Messages message = mMessageList.get(position);
                        final String imageUrl = message.getImage();
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        intent.putExtra("image_url", imageUrl);
                        intent.putExtra("user_id", message.getFrom());
                        intent.putExtra("time", String.valueOf(message.getTime()));
                        context.startActivity(intent);
                    }
                }
            });

        }
    }

    public class ImageMessageViewHolderUser extends RecyclerView.ViewHolder {
        private ImageView messageImageUser;
        //private CardView cardViewUser;
        private TextView imageTimeUser;
        private TextView imageChatDateUser;
        private View view;
        private RelativeLayout relativeLayout;

        public ImageMessageViewHolderUser(View itemView) {
            super(itemView);
            imageTimeUser = itemView.findViewById(R.id.time_text_layout_image_user);
            //cardViewUser = itemView.findViewById(R.id.imageMessageUser);
            messageImageUser = itemView.findViewById(R.id.message_image_layout_user);
            imageChatDateUser = itemView.findViewById(R.id.image_chat_date_user);
            view = itemView.findViewById(R.id.message_image_view_user);
            relativeLayout = itemView.findViewById(R.id.message_image_single_layout_user);

            messageImageUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Messages message = mMessageList.get(position);
                        final String imageUrl = message.getImage();
                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                        intent.putExtra("image_url", imageUrl);
                        intent.putExtra("user_id", message.getFrom());
                        intent.putExtra("time", String.valueOf(message.getTime()));
                        context.startActivity(intent);
                    }
                }
            });

        }
    }

}
