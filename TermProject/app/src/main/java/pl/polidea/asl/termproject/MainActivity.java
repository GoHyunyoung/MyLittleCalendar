package pl.polidea.asl.termproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends TabActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private DataOutputStream os;
    private DataInputStream is;
    private static final int PORT = 5555;
    private Socket socket;
    private String IP = "192.168.84.1";
    private static int regFlag = 0;

    public static final String PROPERTY_REG_ID = "mylittlecalendar-1143";
    private static final String PROPERTY_APP_VERSION = "1.0";
    private static final String TAG = "ICELANCER";
    String SENDER_ID = "98102895089";

    String clientID;
    String clientEM;
    String clientNAME;  //AsyncTask를 위한 변수들.

    GoogleCloudMessaging gcm;
    Context context;

    SharedPreferences information;

    String regid;
    private TextView mDisplay;
    private TextView mInfoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = getTabHost();

        // 1번째 Tab      캘린더
        tabHost.addTab(tabHost.newTabSpec("CALENDAR")
                .setIndicator("CALENDAR", getResources().getDrawable(R.drawable.ic_btn_addtask))
                .setContent(new Intent(this, CalendarActivity.class)));
        // 2번째 Tab      그룹
        tabHost.addTab(tabHost.newTabSpec("GROUP")
                .setIndicator("GROUP", getResources().getDrawable(R.drawable.ic_btn_share))
                .setContent(new Intent(this, GroupActivity.class)));

        tabHost.setCurrentTab(0);

        information = PreferenceManager.getDefaultSharedPreferences(this);
        context = getApplicationContext();

        if(information.getString("storedId","") == ""){
            register_info();
        }       //저장된 아이디가 없다면 계정 등록

        if(information.getString("storedIp","") != ""){
            IP = information.getString("storedIp","");
        }       //아이피가 저장될 경우 아이피 저장


    }
    public void register_info(){
        LayoutInflater inflater = getLayoutInflater();
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue, null);
        final EditText myid = (EditText) view_enroll.findViewById(R.id.et_regid);
        final EditText myname = (EditText) view_enroll.findViewById(R.id.et_regname);
        final EditText myemail = (EditText) view_enroll.findViewById(R.id.et_regemail);
        final EditText ipadrress = (EditText)   view_enroll.findViewById(R.id.et_regip);
        ipadrress.setText(IP);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //객체 생성

        buider.setTitle("INPUT YOUR INFORMATION"); //타이틀
        buider.setView(view_enroll); //빌더 세팅
        buider.setIcon(R.drawable.logo);   //이미지
        buider.setPositiveButton("제출", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (myname.getText().length() >= 1 && myemail.getText().length() >= 1 && myid.getText().length() >= 1) {
                    String name = myname.getText().toString();//사용자이름
                    String email = myemail.getText().toString();//사용자id
                    String id = myid.getText().toString();
                    IP = ipadrress.getText().toString();

                    clientNAME = name;
                    clientEM = email;
                    clientID = id;
                    /*
                    SharedPreferences.Editor editor= information.edit();
                    editor.putString("storedName", name);
                    editor.putString("storedEmail", email);
                    editor.putString("storedId",id);
                    editor.putString("storedIp",IP);
                    editor.commit();
                    */
                    //regFlag = 1;    //저장과 동시에, 백그라운드에서 대기하던 서버통신 재개
                    //new ProgressDlgTest(Main.this).execute(100);
                    //다이얼로그에 입력된 계정 정보들을 저장한다.

                    if (checkPlayServices()) {      //gcm과 통신이 가능한지 검사
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                        regid = getRegistrationId(context);
                        //regID를 가져온다.
                        if (regid.isEmpty() || information.getString("storedIp","") == null) {
                            registerInBackground();
                        }
                        //만약 없다면 백그라운드에서 새로운 regID를 발급 받는다.
                    } else {
                        Log.i(TAG, "No valid Google Play Services APK found.");
                    }


                } else {
                    Toast.makeText(MainActivity.this, "NULL값을 가질 수 없습니다.", Toast.LENGTH_LONG).show();
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
        new AsyncTask<Integer, String, Integer>() {
            private ProgressDialog mDlg;

            @Override
            protected void onCancelled() {
            // TODO Auto-generated method stub
                mDlg.dismiss();
                finish();
                Log.d(TAG,"OnCancelled");
            }


            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                mDlg = new ProgressDialog(MainActivity.this);
                mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDlg.setMessage("서버 찾는중");
                mDlg.show();
                mDlg.setCancelable(false);
            }

            @Override
            protected Integer doInBackground(Integer... params) {
                // TODO Auto-generated method stub
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    //regID를 발급받는다.
                    SharedPreferences.Editor editor = information.edit();
                    editor.putString("regid", regid);
                    editor.commit();
                    System.out.println(msg);


                    //mDlg.show();
                    //while문의 break 조건은 계정정보가 완료이다.
                    msg = "UP_" + clientID
                            + "NAME_" + clientNAME
                            + "EMAIL_" + clientEM
                            + "REG_" + regid;

                    publishProgress("서버에 접속 중");
                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());
                    os.writeUTF(msg);
                    msg = is.readUTF();
                    socket.close();

                    if(msg.equals("success")){
                        //regid와 앱 버전을 저장한다.
                        storeRegistrationId(context, regid);
                        return 1;
                    }
                    else if(msg.equals("false")){
                        return 2;
                    }
                    return 0;
                } catch (IOException ex) {
                    //msg = "Error :" + ex.getMessage();
                    Log.d("TAG", ex.getMessage());
                    return 0;
                }
            }

            //onProgressUpdate() 함수는 publishProgress() 함수로 넘겨준 데이터들을 받아옴
            @Override
            protected void onProgressUpdate(String... msg) {
                // TODO Auto-generated method stub
                if (msg[0] ==""){
                    mDlg.setMessage(msg[0]);
                }
            }

            @Override
            protected void onPostExecute(Integer msg) {
                // TODO Auto-generated method stub
                if(msg == 1){
                    Toast.makeText(MainActivity.this, "서버 접속 성공.", Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor= information.edit();
                    editor.putString("storedName", clientNAME);
                    editor.putString("storedEmail", clientEM);
                    editor.putString("storedId",clientID);
                    editor.putString("storedIp",IP);
                    editor.commit();
                    mDlg.dismiss();
                }
                else if(msg == 2){
                    Toast.makeText(MainActivity.this, "중복 ID가 존재합니다..", Toast.LENGTH_LONG).show();
                    mDlg.dismiss();
                    finish();
                }
                else{
                    Toast.makeText(MainActivity.this, "서버 등록 실패.", Toast.LENGTH_LONG).show();
                    mDlg.dismiss();
                    finish();
                }
            }

        }.execute(1);
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


    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

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
        return information.getString("storedId","");
    }
}