package pl.polidea.asl.termproject;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends TabActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private DataOutputStream os;
    private static final int PORT = 5555;
    private Socket socket;
    private String IP = "192.168.84.1";
    private static int regFlag = 0;

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    public static final String PROPERTY_REG_ID = "mylittlecalendar-1143";

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    private static final String PROPERTY_APP_VERSION = "1.0";
    private static final String TAG = "ICELANCER";
    private static final String REGID = "regid";
    String SENDER_ID = "98102895089";


    GoogleCloudMessaging gcm;
    //SharedPreferences prefs;
    Context context;

    SharedPreferences myinfo;

    String regid;
    private TextView mDisplay;
    private TextView mInfoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = getTabHost();

        // 1번째 Tab
        tabHost.addTab(tabHost.newTabSpec("CALENDAR")
                .setIndicator("CALENDAR", getResources().getDrawable(R.drawable.ic_btn_addtask))
                .setContent(new Intent(this, CalendarActivity.class)));
        // 2번째 Tab
        tabHost.addTab(tabHost.newTabSpec("GROUP")
                .setIndicator("GROUP", getResources().getDrawable(R.drawable.ic_btn_share))
                .setContent(new Intent(this, GroupActivity.class)));

        // 3번째 Tab
        tabHost.addTab(tabHost.newTabSpec("FREIND")
                .setIndicator("FREIND", getResources().getDrawable(R.drawable.ic_btn_share))
                .setContent(new Intent(this, FriendActivity.class)));

        tabHost.setCurrentTab(0);

        myinfo = PreferenceManager.getDefaultSharedPreferences(this);
        //mDisplay = (TextView) findViewById(R.id.display);
        context = getApplicationContext();
        //mInfoText = (TextView) findViewById(R.id.infor);

        if(myinfo.getString("storedId","") == ""){
            register_info();
        }

        if(myinfo.getString("storedIp","") != ""){
            IP = myinfo.getString("storedIp","");
        }

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }


            //SharedPreferences.Editor editor = myinfo.edit();
            //editor.putString(MYREGID,"");
            //editor.commit();

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void register_info(){
        LayoutInflater inflater = getLayoutInflater();
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue, null);
        final EditText myid = (EditText) view_enroll.findViewById(R.id.et_regid);
        final EditText myname = (EditText) view_enroll.findViewById(R.id.et_regname);
        final EditText myemail = (EditText) view_enroll.findViewById(R.id.et_regemail);
        final EditText ipadrress = (EditText)   view_enroll.findViewById(R.id.et_regip);
        ipadrress.setText(IP);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성

        buider.setTitle("등록하시겠습니까?"); //Dialog 제목
        buider.setView(view_enroll); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("등록", new DialogInterface.OnClickListener() {
            //Dialog에 "Complite"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (myname.getText().length() >= 3 && myemail.getText().length() >= 3) {
                    String name = myname.getText().toString();//사용자이름
                    String email = myemail.getText().toString();//사용자id
                    String id = myid.getText().toString();
                    IP = ipadrress.getText().toString();
                    SharedPreferences.Editor editor= myinfo.edit();
                    editor.putString("storedName", name);
                    editor.putString("storedEmail", email);
                    editor.putString("storedId",id);
                    editor.putString("storedIp",IP);
                    editor.commit();
                    regFlag = 1;
                    //서버에 등록
                    //register(email, redID, name);
                    //액티비티상에 내정보 등록 & 어플리케이션에 내정보 저장


                } else {
                    Toast.makeText(MainActivity.this, "NAME : 3글자이상\nE-MAIL : 3글자이상 입력하시오.", Toast.LENGTH_LONG).show();
                    register_info();
                }
            }
        });
        buider.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        buider.show();
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    //msg = "Device registered, registration ID=" + regid;
                    //msg = regid;

                    SharedPreferences.Editor editor = myinfo.edit();
                    editor.putString(REGID, regid);
                    editor.commit();
                    System.out.println(msg);

                    while(regFlag == 0){

                        //대기
                    }
                    msg = "UP_"+myinfo.getString("storedId","")
                            +"NAME_"+myinfo.getString("storedName","")
                            +"EMAIL_"+myinfo.getString("storedEmail","")
                            +"REG_"+regid;

                    socket = new Socket(InetAddress.getByName(IP),PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    os.writeUTF(msg);
                    socket.close();

                    // 서버에 발급받은 등록 아이디를 전송한다.
                    // 등록 아이디는 서버에서 앱에 푸쉬 메시지를 전송할 때 사용된다.
                    sendRegistrationIdToBackend();

                    // 등록 아이디를 저장해 등록 아이디를 매번 받지 않도록 한다.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }

        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {

    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // 앱이 업데이트 되었는지 확인하고, 업데이트 되었다면 기존 등록 아이디를 제거한다.
        // 새로운 버전에서도 기존 등록 아이디가 정상적으로 동작하는지를 보장할 수 없기 때문이다.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        //dialog.dismiss();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("ICELANCER", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public String getStoredID(){
        return myinfo.getString("storedId","");
    }
}
