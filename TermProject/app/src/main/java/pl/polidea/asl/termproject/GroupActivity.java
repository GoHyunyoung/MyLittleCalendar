package pl.polidea.asl.termproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by admin on 2015-12-10.
 */
public class GroupActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    Button room1;
    Button room2;
    Button room3;
    Button room4;
    Button room5;
    SharedPreferences myinfo;
    MyDBHelper myDBHelper;
    SQLiteDatabase db;
    Cursor cursor;
    private DataOutputStream os;
    private DataInputStream is;
    private static final int PORT = 5555;
    private Socket socket;
    private String IP = "192.168.84.1";
    private boolean diaIsEnd = false;
    TextView tv_addr;
    TextView tv_id;
    ImageButton bt_addr;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        myinfo = PreferenceManager.getDefaultSharedPreferences(this);
        myDBHelper = new MyDBHelper(this, "Today.db", null, 1);
        db = myDBHelper.getWritableDatabase();

        tv_addr = (TextView) findViewById(R.id.tv_addr);
        tv_id = (TextView) findViewById(R.id.tv_id);
        bt_addr = (ImageButton) findViewById(R.id.bt_ip);
        room1 = (Button) findViewById(R.id.bt_room1);
        room2 = (Button) findViewById(R.id.bt_room2);
        room3 = (Button) findViewById(R.id.bt_room3);
        room4 = (Button) findViewById(R.id.bt_room4);
        room5 = (Button) findViewById(R.id.bt_room5);

        bt_addr.setOnClickListener(this);
        room1.setOnClickListener(this);
        room2.setOnClickListener(this);
        room3.setOnClickListener(this);
        room4.setOnClickListener(this);
        room5.setOnClickListener(this);
        room1.setOnLongClickListener(this);
        room2.setOnLongClickListener(this);
        room3.setOnLongClickListener(this);
        room4.setOnLongClickListener(this);
        room5.setOnLongClickListener(this);

        tv_addr.setText(myinfo.getString("storedIp", ""));
        tv_id.setText(myinfo.getString("storedId",""));

        if(myinfo.getString("room1","")==""){
            room1.setText("#NULL");
            room1.setTextColor(Color.parseColor("#0000FF"));
        } else{ room1.setText(myinfo.getString("room1name",""));
        }
        if(myinfo.getString("room2","")==""){
            room2.setText("#NULL");
            room2.setTextColor(Color.parseColor("#0000FF"));
        } else{ room2.setText(myinfo.getString("room2name", ""));
        }
        if(myinfo.getString("room3","")==""){
            room3.setText("#NULL");
            room3.setTextColor(Color.parseColor("#0000FF"));
        } else{ room3.setText(myinfo.getString("room3name", ""));
        }
        if(myinfo.getString("room4","")==""){
            room4.setText("#NULL");
            room4.setTextColor(Color.parseColor("#0000FF"));
        } else{ room4.setText(myinfo.getString("room4name", ""));
        }
        if(myinfo.getString("room5","")==""){
            room5.setText("#NULL");
            room5.setTextColor(Color.parseColor("#0000FF"));
        } else{ room5.setText(myinfo.getString("room5name",""));    }       //방 초기화
    }


    @Override
    public void onClick(View v){
        int flag = 0;
        switch (v.getId()){
            case R.id.bt_room1: flag = 1;   break;
            case R.id.bt_room2: flag = 2;   break;
            case R.id.bt_room3: flag = 3;   break;
            case R.id.bt_room4: flag = 4;   break;
            case R.id.bt_room5: flag = 5;   break;
            case R.id.bt_ip: flag = 6;    break;
        }
        //Toast.makeText(getApplicationContext(), "CLICK", Toast.LENGTH_SHORT).show();

        if(flag  == 6){
            changeIP();
        }
        else if (myinfo.getString("room"+flag,"") == ""){
            createRoom(flag);
        }
        else if (myinfo.getString("room"+flag,"") != ""){
            inforRoom(flag);
        }
    }


    @Override
    public boolean onLongClick(View view) {
        int flag = 0;
        switch (view.getId()){
            case R.id.bt_room1: flag = 1;   break;
            case R.id.bt_room2: flag = 2;   break;
            case R.id.bt_room3: flag = 3;   break;
            case R.id.bt_room4: flag = 4;   break;
            case R.id.bt_room5: flag = 5;   break;
        }
        Toast.makeText(getApplicationContext(), "LONG CLICK", Toast.LENGTH_SHORT).show();
        deleteRoom(flag);

        return false;
    }

    public void changeIP(){
        LayoutInflater inflater = getLayoutInflater();
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue_changeip, null);
        final EditText ip = (EditText) view_enroll.findViewById(R.id.et_chaip);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
        buider.setIcon(R.mipmap.ic_launcher);
        buider.setTitle("CREATE SERVER IP ADDRESS"); //Dialog 제목
        buider.setView(view_enroll); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("변경", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = ip.getText().toString();
                SharedPreferences.Editor editor = myinfo.edit();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                editor.putString("storedIp", msg);
                editor.commit();
                tv_addr.setText(msg);
            }
        });
        buider.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        buider.show();
    }

    public void createRoom(final int roomNumber){
        LayoutInflater inflater = getLayoutInflater();
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue_createroom, null);
        final EditText roomName = (EditText) view_enroll.findViewById(R.id.et_roomName);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
        Button enterRoom = (Button) view_enroll.findViewById(R.id.bt_enterroom);

        enterRoom.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    LayoutInflater inninflater = getLayoutInflater();
                    final View view_enroll = inninflater.inflate(R.layout.activity_dialogue_enterroom, null);
                    final EditText hostid = (EditText) view_enroll.findViewById(R.id.et_hostid);
                    final EditText roomid = (EditText) view_enroll.findViewById(R.id.et_roomID);
                    final AlertDialog.Builder innbuider = new AlertDialog.Builder(GroupActivity.this); //AlertDialog.Builder 객체 생성
                    innbuider.setIcon(R.mipmap.ic_launcher);
                    innbuider.setTitle("ENTER SHARE GROUP"); //Dialog 제목
                    innbuider.setView(view_enroll); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
                    innbuider.setPositiveButton("입장", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "클릭됨.", Toast.LENGTH_SHORT).show();
                            //수정
                            enterRoomInBackGround(roomNumber, hostid.getText().toString(), Integer.parseInt(roomid.getText().toString()));

                        }

                    });

                    innbuider.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();//finish();
                        }
                    });
                    innbuider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//finish();
                        }
                    });
                    innbuider.show();
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "에러다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buider.setIcon(R.mipmap.ic_launcher);
        buider.setTitle("CREATE SHARE GROUP"); //Dialog 제목
        buider.setView(view_enroll); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("생성", new DialogInterface.OnClickListener() {
            //Dialog에 "Complite"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String name = roomName.getText().toString();//방 제목

                switch (roomNumber) {
                    case 1:
                        room1.setText(name);
                        room1.setTextColor(Color.parseColor("#000000"));
                        break;
                    case 2:
                        room2.setText(name);
                        room2.setTextColor(Color.parseColor("#000000"));
                        break;
                    case 3:
                        room3.setText(name);
                        room3.setTextColor(Color.parseColor("#000000"));
                        break;
                    case 4:
                        room4.setText(name);
                        room4.setTextColor(Color.parseColor("#000000"));
                        break;
                    case 5:
                        room5.setText(name);
                        room5.setTextColor(Color.parseColor("#000000"));
                        break;
                }
                //서버에 전송, 서버에서 받음.

                SharedPreferences.Editor editor = myinfo.edit();
                //editor.putString("room" + roomNumber, roomNumber + "");
                editor.putString("room" + roomNumber + "name", name);
                editor.commit();


                createRoomInBackGround(roomNumber, name, true);

                //서버에 등록
                //register(email, redID, name);
                //액티비티상에 내정보 등록 & 어플리케이션에 내정보 저장
            }
        });
        buider.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();//finish();
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();//finish();
            }
        });

        buider.show();



    }

    public void deleteRoom(final int roomNumber){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this
        // 여기서 부터는 알림창의 속성 설정
        builder.setTitle("삭제")        // 제목 설정
                .setMessage("삭제 하시겠습니까?")        // 메세지 설정
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        createRoomInBackGround(roomNumber, null, false);        //방 삭제 서버 전송

                        switch (roomNumber){
                            case 1: room1.setText("#NULL");    room1.setTextColor(Color.parseColor("#0000FF"));    break;
                            case 2: room2.setText("#NULL");    room2.setTextColor(Color.parseColor("#0000FF"));    break;
                            case 3: room3.setText("#NULL");    room3.setTextColor(Color.parseColor("#0000FF"));    break;
                            case 4: room4.setText("#NULL");    room4.setTextColor(Color.parseColor("#0000FF"));    break;
                            case 5: room5.setText("#NULL");    room5.setTextColor(Color.parseColor("#0000FF"));    break;
                        }
                        SharedPreferences.Editor editor = myinfo.edit();
                        editor.putString("room"+roomNumber, "");
                        editor.putString("room"+roomNumber+"id", "");
                        editor.commit();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기


    }

    public void inforRoom(final int roomNumber){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroupActivity.this);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setTitle("선택하세요   -방 코드 :"+myinfo.getString("room"+roomNumber,""));

        // List Adapter 생성
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupActivity.this, android.R.layout.select_dialog_singlechoice);
        adapter.add("일정 추가");
        adapter.add("일정 목록");
        // 버튼 생성
        alertBuilder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });

        // Adapter 셋팅
        alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // AlertDialog 안에 있는 AlertDialog
                String strName = adapter.getItem(id);
                AlertDialog.Builder innBuilder = new AlertDialog.Builder(GroupActivity.this);
                final ArrayAdapter<String> innAdapter = new ArrayAdapter<String>(GroupActivity.this, android.R.layout.select_dialog_singlechoice);
                if (id == 0) {
                    innBuilder.setIcon(R.mipmap.ic_launcher);
                    innBuilder.setTitle("일정 추가");
                    innBuilder.setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    cursor = db.rawQuery("SELECT * FROM today ", null);

                    while (cursor.moveToNext()) {
                        innAdapter.add(cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3));
                        System.out.println(cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3));
                    }
                    innBuilder.setAdapter(innAdapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            adjustRoomInBackGround(roomNumber, id, true);
                        }
                    });
                    //innBuilder.setMessage(strName);

                    innBuilder.show();
                } else if (id == 1) {
                    innBuilder.setMessage(strName);
                    innBuilder.setIcon(R.mipmap.ic_launcher);
                    innBuilder.setTitle("일정 목록");
                    innBuilder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    innBuilder.show();
                }
            }
        });
        alertBuilder.show();
    }

    public void enterRoomInBackGround(final int roomNumber, final String hostId, final int roomId) {


        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                String msg = "";
                try {
                    IP = myinfo.getString("storedIp","");
                    System.out.println(IP);

                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());

                    msg = "EN_"+hostId+"ROOMID_"+roomId+"ID_"+myinfo.getString("storedId","")+"ROOM_"+roomNumber;

                    os.writeUTF(msg);

                    msg = is.readUTF();
                    System.out.println(msg);
                    socket.close();
                } catch (Exception e) {
                    Log.d("TAG", e.getMessage());
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                if(msg.equals("false")){
                    Toast.makeText(getApplicationContext(), "잘못된 입력.", Toast.LENGTH_SHORT).show();
                }
                else{
                    switch (roomNumber){
                        case 1: room1.setText(msg);    room1.setTextColor(Color.parseColor("#0000FF"));    break;
                        case 2: room2.setText(msg);    room2.setTextColor(Color.parseColor("#0000FF"));    break;
                        case 3: room3.setText(msg);    room3.setTextColor(Color.parseColor("#0000FF"));    break;
                        case 4: room4.setText(msg);    room4.setTextColor(Color.parseColor("#0000FF"));    break;
                        case 5: room5.setText(msg);    room5.setTextColor(Color.parseColor("#0000FF"));    break;
                    }
                    SharedPreferences.Editor editor = myinfo.edit();
                    editor.putString("room"+roomNumber, roomId+"");
                    editor.putString("room"+roomNumber+"name", msg);

                    //editor.putString("")
                    editor.commit();
                }
            }
        }.execute(null, null, null);
    }

    public void adjustRoomInBackGround(final int roomNumber, final int id, final boolean flag){

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int roomID = 0;
                String msg = "", body = "";
                try {
                    IP = myinfo.getString("storedIp","");
                    System.out.println(IP);

                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());
                    //System.out.println("test->"+id);
                    cursor.moveToPosition(id);

                    body = cursor.getString(1) + "__" + cursor.getString(2) + "__" + cursor.getString(3);
                    System.out.println("test");
                    System.out.println(body);
                    if(flag == true)    msg = "AD_" + body + "ROOMID_"+myinfo.getString("room"+roomNumber,"");
                    else if(flag == false)  msg = "LI_"+body;
                    os.writeUTF(msg);
                    //msg = is.readUTF();
                    //roomID = Integer.parseInt(msg);   ????
                    //수정
                    socket.close();
                } catch (Exception e){
                    Log.d("TAG",e.getMessage());
                }
                return roomID;
            }
            @Override
            protected void onPostExecute(Integer roomID) {
                if(flag == true)
                    Toast.makeText(getApplicationContext(), "일정 추가 완료!", Toast.LENGTH_SHORT).show();
            }
        }.execute(null, null, null);
    }

    public void createRoomInBackGround(final int roomNumber, final String name, final boolean flag){       //방 생성 서버 전송

        //final int roomID = 0;
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int roomID=0;
                String msg="";//+myinfo.getString("storedId","");
                try {

                    IP = myinfo.getString("storedIp","");
                    System.out.println(IP);
                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());
                    if(flag == true)    msg = "MK_" +myinfo.getString("storedId","")+"TITLE_"+name+ "ROOM_"+roomNumber;

                    else if(flag == false) msg = "DR_"+ myinfo.getString("storedId","")+"ROOM_"+roomNumber;

                    os.writeUTF(msg);
                    msg = is.readUTF();
                    roomID = Integer.parseInt(msg);
                    //수정
                    socket.close();
                }   catch (Exception e){
                    Log.d("TAG",e.getMessage());
                }

                return roomID;
            }
            @Override
            protected void onPostExecute(Integer roomID) {
                SharedPreferences.Editor editor = myinfo.edit();
                if(flag == true) {
                    editor.putString("room" + roomNumber, roomID + "");
                    Toast.makeText(getApplicationContext(), "방생성 ! roomID = "+roomID, Toast.LENGTH_SHORT).show();
                }else {
                    editor.putString("room" + roomNumber , "");
                    Toast.makeText(getApplicationContext(), "방삭제 ! roomID = 0", Toast.LENGTH_SHORT).show();
                }
                editor.commit();
            }
        }.execute(null, null, null);


    }
}