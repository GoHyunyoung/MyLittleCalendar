package pl.polidea.asl.termproject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by icelancer on 15. 2. 21..
 */
public class GcmIntentService extends IntentService {
    public static final String TAG = "icelancer";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
//        Used to name the worker thread, important only for debugging.
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
           if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
               // This loop represents the service doing some work.
               for (int i=0; i<5; i++) {
                   Log.i(TAG, "Working... " + (i + 1)
                           + "/5 @ " + SystemClock.elapsedRealtime());
                   try {
                       Thread.sleep(5000);
                   } catch (InterruptedException e) {
                   }
               }
               Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
               // Post notification of received message.
               sendNotification("Received: " + extras.toString());
               Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("새로운 일정이 동기화 되었습니다."))
                        .setContentText("새로운 일정이 동기화 되었습니다.");
        System.out.println(msg);
        msgParsing(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    public void msgParsing(String msg){
        String title = "";
        String date = "";
        String time = "";

        Log.i("test", "title : "+title+" len : "+title.length());

        msg = msg.substring(msg.indexOf("ST_")+3, msg.indexOf("_END"));
        System.out.println(msg);
        if(msg.substring(0, 4).equals("NEW_")){
            msg = msg.substring(4);
            try {


                title = msg.substring(0, msg.indexOf("DATE_"));
                title = URLDecoder.decode(title, "euc-kr");
                msg = msg.substring(msg.indexOf("DATE_") + 5);

                date = msg.substring(0, msg.indexOf("TIME_"));
                msg = msg.substring(msg.indexOf("TIME_") + 5);

                time = msg;
                time = URLDecoder.decode(time, "euc-kr");
                //notificationCalander(title,date,time);
                CalendarActivity cal = new CalendarActivity();
                cal.notificatonCalander(title, date, time);
                CalendarActivity.notificatonCalander(title, date, time);
            } catch (UnsupportedEncodingException e){
                Log.d("TAG", e.getMessage());
            }

            System.out.println("title -> "+title+" date -> "+date+" time -> "+time);

        }

    }
}

