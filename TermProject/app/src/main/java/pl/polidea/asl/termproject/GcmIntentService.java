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
 * Created by admin on 2015-12-10.
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
        //메시지를 받기위해서 intent형식으로 파라미터 대입
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
           if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
               //총 5번 받는다. 정보가 일치하는지 검사하기위해 -> 지연시간이 8초정도 걸리는 이유.
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
        //WakefulBroadcastReceiver로 장치를 꺠운다.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    //PUSH알림을 위한 노티피케이션
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        String noti ="";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("My Little Calender")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(noti = msgParsing(msg) + "\n새로운 일정이 동기화 되었습니다."))
                        .setContentText(noti);      //노티피케이션 텍스트 세팅
        System.out.println(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public String msgParsing(String msg){       //메시지 파싱
        String title = "";
        String date = "";
        String time = "";

        Log.i("test", "title : "+title+" len : "+title.length());

        msg = msg.substring(msg.indexOf("ST_")+3, msg.indexOf("_END"));
        System.out.println(msg);
        if(msg.substring(0, 4).equals("NEW_")){     //새로운 일정이 동기화 되었을 때
            msg = msg.substring(4);
            try {
                title = msg.substring(0, msg.indexOf("DATE_"));     //인코딩된 한글을 디코딩한다.
                title = URLDecoder.decode(title, "euc-kr");
                msg = msg.substring(msg.indexOf("DATE_") + 5);

                date = msg.substring(0, msg.indexOf("TIME_"));
                msg = msg.substring(msg.indexOf("TIME_") + 5);

                time = msg;
                time = URLDecoder.decode(time, "euc-kr");

                CalendarActivity cal = new CalendarActivity();
                cal.notificatonCalander(title, date, time); //캘린더 액티비티에 새로운 일정 갠신
                CalendarActivity.notificatonCalander(title, date, time);
            } catch (UnsupportedEncodingException e){
                Log.d("TAG", e.getMessage());
            }
            System.out.println("title -> "+title+" date -> "+date+" time -> "+time);
            msg = "TITLE : "+title+" DATE : "+date+" TIME : "+time;
        }

        return msg; //파싱된 메시지를 반환하는 이유는 노티피케이션에 정보를 표현하기 위해
    }
}

