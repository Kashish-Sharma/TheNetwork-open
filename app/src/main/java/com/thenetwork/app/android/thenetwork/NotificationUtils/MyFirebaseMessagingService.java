package com.thenetwork.app.android.thenetwork.NotificationUtils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thenetwork.app.android.thenetwork.Activities.MainActivity;
import com.thenetwork.app.android.thenetwork.R;

import java.util.Map;

import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.FOLLOW_REQUEST_SENT_NOTIFICATION;

/**
 * Created by Kashish on 04-06-2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CONSOLE_DATA_NOTIFICATION_ID = "1138";
    public static final int CONSOLE_DATA_PENDING_INTENT_ID = 3417;
    private static final String TAG = "fcmservice";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();


        Log.i("notification ",title+" "+" icon"+remoteMessage.getNotification().getIcon());
        Log.i("notification ",body+" "+" sound"+remoteMessage.getNotification().getSound());


        String dataMessage = remoteMessage.getData().get("message");
        String dataFrom = remoteMessage.getData().get("user_id");
        String dataName = remoteMessage.getData().get("name");
        String dataImage = remoteMessage.getData().get("image");

        sendNotificationFollow(title, body,this, click_action, dataFrom, dataName, dataImage);

        Log.i("notification ",dataFrom+ " is the data");
    }


    private void sendNotificationFollow(String messageTitle,String messageBody, Context context
            , String click_action, String dataFrom, String dataName, String dataImage){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(
                            CONSOLE_DATA_NOTIFICATION_ID,
                            context.getString(R.string.main_notification_channel_name),
                            NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }


        Intent resultIntent = new Intent(click_action);

        if (click_action.equals("com.thenetwork.app.android.thenetwork.Activities.ONFOLLOWREQUESTRECEIVED")){
            resultIntent.putExtra("user_id",dataFrom);
        } else if (click_action.equals("com.thenetwork.app.android.thenetwork.Activities.ONNEWMESSAGERECEIVED")){
            resultIntent.putExtra("user_id",dataFrom);
            resultIntent.putExtra("name",dataName);
            resultIntent.putExtra("image",dataImage);

        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500,500,500,500,500};
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CONSOLE_DATA_NOTIFICATION_ID)
                                                        .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                                                        .setSmallIcon(R.drawable.action_normal_like_white)
                                                        .setLargeIcon(largeIcon(this))
                                                        .setContentTitle(messageTitle)
                                                        .setContentText(messageBody)
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                                                        .setAutoCancel(true)
                                                        .setVibrate(pattern)
                                                        .setLights(Color.BLUE,1,1)
                                                        .setSound(defaultSoundUri)
                                                        .setContentIntent(contentIntent(context,resultIntent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    private static PendingIntent contentIntent (Context context, Intent resultIntent){
        return PendingIntent.getActivity(
                                    context,
                                    CONSOLE_DATA_PENDING_INTENT_ID,
                                    resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context){
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res,R.drawable.action_normal_like_white);
        return largeIcon;
    }

}
