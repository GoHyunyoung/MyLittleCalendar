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
    SharedPreferences information;
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
        information = PreferenceManager.getDefaultSharedPreferences(this);
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

        tv_addr.setText(information.getString("storedIp", ""));   //저장된 IP 불러오기
        tv_id.setText(information.getString("storedId",""));        //저장된 아이디 불러오기

        if(information.getString("room1","")==""){      //방에 저장된 정보가 없다면 NULL표시
            room1.setText("#NULL");
            room1.setTextColor(Color.parseColor("#0000FF"));
        } else{ room1.setText(information.getString("room1name",""));
        }
        if(information.getString("room2","")==""){
            room2.setText("#NULL");
            room2.setTextColor(Color.parseColor("#0000FF"));
        } else{ room2.setText(information.getString("room2name", ""));
        }
        if(information.getString("room3","")==""){
            room3.setText("#NULL");
            room3.setTextColor(Color.parseColor("#0000FF"));
        } else{ room3.setText(information.getString("room3name", ""));
        }
        if(information.getString("room4","")==""){
            room4.setText("#NULL");
            room4.setTextColor(Color.parseColor("#0000FF"));
        } else{ room4.setText(information.getString("room4name", ""));
        }
        if(information.getString("room5","")==""){
            room5.setText("#NULL");
            room5.setTextColor(Color.parseColor("#0000FF"));
        } else{ room5.setText(information.getString("room5name",""));    }       //방 초기화
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
        Toast.makeText(getApplicationContext(), "ROOM"+flag, Toast.LENGTH_SHORT).show();

        if(flag  == 6){
            changeIP();     //아이피 변경 다이얼로그
        }
        else if (information.getString("room"+flag,"") == ""){
            createRoom(flag);   //방 생성 다이얼로그
        }
        else if (information.getString("room"+flag,"") != ""){
            inforRoom(flag);    //방 정보 다이얼로그
        }
    }


    @Override
    public boolean onLongClick(View view) {     //방 삭제
        int flag = 0;
        switch (view.getId()){
            case R.id.bt_room1: flag = 1;   break;
            case R.id.bt_room2: flag = 2;   break;
            case R.id.bt_room3: flag = 3;   break;
            case R.id.bt_room4: flag = 4;   break;
            case R.id.bt_room5: flag = 5;   break;
        }
        Toast.makeText(getApplicationContext(), "LONG CLICK", Toast.LENGTH_SHORT).show();
        deleteRoom(flag);   //방 삭제 다이얼로그

        return false;
    }

    public void changeIP(){     //서버 아이피 변경 다이얼로그
        LayoutInflater inflater = getLayoutInflater();       //서버 변경 다이얼로그 콘텐츠 뷰
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue_changeip, null);
        final EditText ip = (EditText) view_enroll.findViewById(R.id.et_chaip);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //객체 생성
        buider.setIcon(R.mipmap.ic_launcher);   //이미지
        buider.setTitle("CREATE SERVER IP ADDRESS"); //타이틀
        buider.setView(view_enroll); //다이얼로그 객체 세팅
        buider.setPositiveButton("변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = ip.getText().toString();
                SharedPreferences.Editor editor = information.edit();
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
        buider.show();      //다이얼로그 보여주기
    }

    public void createRoom(final int roomNumber){   //방 생성 다이얼로그
        LayoutInflater inflater = getLayoutInflater();      //방 생성 다이얼로그 컨텐츠 뷰
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue_createroom, null);
        final EditText roomName = (EditText) view_enroll.findViewById(R.id.et_roomName);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //객체 생성

        buider.setIcon(R.mipmap.ic_launcher);
        buider.setTitle("CREATE SHARE GROUP"); //타이틀
        buider.setView(view_enroll); //빌더 세팅

        buider.setNeutralButton("입장", new DialogInterface.OnClickListener() {
            //Dialog에 입장 버튼 설정
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                try{
                    LayoutInflater inninflater = getLayoutInflater();       //Inner Dialog 객체 선언
                    final View view_enroll = inninflater.inflate(R.layout.activity_dialogue_enterroom, null);
                    final EditText hostid = (EditText) view_enroll.findViewById(R.id.et_hostid);
                    final EditText roomid = (EditText) view_enroll.findViewById(R.id.et_roomID);
                    final AlertDialog.Builder innbuider = new AlertDialog.Builder(GroupActivity.this); //객체 생성
                    innbuider.setIcon(R.mipmap.ic_launcher);
                    innbuider.setTitle("ENTER SHARE GROUP"); //타이틀
                    innbuider.setView(view_enroll); //이너빌더 세팅
                    innbuider.setPositiveButton("입장", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "방에 입장합니다..", Toast.LENGTH_SHORT).show();
                            enterRoomInBackGround(roomNumber, hostid.getText().toString(), Integer.parseInt(roomid.getText().toString()));
                            dialogInterface.dismiss();
                            dialog.dismiss();
                        }
                    });
                    innbuider.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
                    innbuider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    innbuider.show();
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "에러.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buider.setPositiveButton("생성", new DialogInterface.OnClickListener() {
            //Dialog에 생성 버튼 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {    //방이 생성 된다.
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


                SharedPreferences.Editor editor = information.edit();
                editor.putString("room" + roomNumber + "name", name);
                editor.commit();    //쉐어 프리퍼런시스에 정보를 저장한다.

                createRoomInBackGround(roomNumber, name, true);
                //백그라운드에서 서버랑 통신한다.
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
                        SharedPreferences.Editor editor = information.edit();
                        editor.putString("room"+roomNumber, "");
                        editor.putString("room"+roomNumber+"id", "");
                        editor.commit();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }

    public void inforRoom(final int roomNumber){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GroupActivity.this);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setTitle("선택하세요   -방 코드 :"+information.getString("room"+roomNumber,""));

        // List Adapter 생성
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupActivity.this, android.R.layout.select_dialog_singlechoice);
        adapter.add("일정 추가");
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
                // AlertDialog 안에 있는 AlertDialog Inner Dialog
                String strName = adapter.getItem(id);
                AlertDialog.Builder innBuilder = new AlertDialog.Builder(GroupActivity.this);
                final ArrayAdapter<String> innAdapter = new ArrayAdapter<String>(GroupActivity.this, android.R.layout.select_dialog_singlechoice);
                    innBuilder.setIcon(R.mipmap.ic_launcher);
                    innBuilder.setTitle("일정 추가");
                    innBuilder.setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    cursor = db.rawQuery("SELECT * FROM today ", null);
                    //DB에 접속하여 자신의 일정을 전부 가져와 리스트에 추가한다.
                    while (cursor.moveToNext()) {
                        innAdapter.add(cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3));
                        System.out.println(cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3));
                    }
                    innBuilder.setAdapter(innAdapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            adjustRoomInBackGround(roomNumber, id, true);
                        }
                    });
                    innBuilder.show();

            }
        });
        alertBuilder.show();
    }

    public void enterRoomInBackGround(final int roomNumber, final String hostId, final int roomId) {
        //방 입장을 위해 서버 접속
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                String msg = "";
                try {
                    IP = information.getString("storedIp","");
                    System.out.println(IP);
                    socket = new Socket(InetAddress.getByName(IP), PORT);   //5555포트로 소켓통신
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());
                    //어느 방에 입장 할 것인지 메시지에 첨부한다.
                    msg = "EN_"+hostId+"ROOMID_"+roomId+"ID_"+information.getString("storedId","")+"ROOM_"+roomNumber;
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
            protected void onPostExecute(String msg) {      //전송이 끝난 후
                if(msg.equals("false")){    //해당 정보가 서버 DB에 없을 경우
                    Toast.makeText(getApplicationContext(), "잘못된 입력.", Toast.LENGTH_SHORT).show();
                }
                else{
                    switch (roomNumber){
                        case 1: room1.setText(msg);    room1.setTextColor(Color.parseColor("#000000"));    break;
                        case 2: room2.setText(msg);    room2.setTextColor(Color.parseColor("#000000"));    break;
                        case 3: room3.setText(msg);    room3.setTextColor(Color.parseColor("#000000"));    break;
                        case 4: room4.setText(msg);    room4.setTextColor(Color.parseColor("#000000"));    break;
                        case 5: room5.setText(msg);    room5.setTextColor(Color.parseColor("#000000"));    break;
                    }
                    SharedPreferences.Editor editor = information.edit();
                    editor.putString("room"+roomNumber, roomId+"");
                    editor.putString("room"+roomNumber+"name", msg);
                    editor.commit();    //룸 정보들을 저장한다. (방 제목, 방 코드)
                }
            }
        }.execute(null, null, null);
    }

    public void adjustRoomInBackGround(final int roomNumber, final int id, final boolean flag){
        //방 수정을 위해 서버와 통신한다. (일정 추가)
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int roomID = 0;
                String msg = "", body = "";
                try {
                    IP = information.getString("storedIp","");
                    System.out.println(IP);
                    //서버와 소켓 통신 시작.
                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());
                    //해당 position으로 커서를 이동한다.
                    cursor.moveToPosition(id);
                    //선택된 일정을 스트링에 대입하여 전송한다.
                    body = cursor.getString(1) + "__" + cursor.getString(2) + "__" + cursor.getString(3);
                    System.out.println(body);
                    msg = "AD_" + body + "ROOMID_"+information.getString("room"+roomNumber,"");
                    os.writeUTF(msg);
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

    public void createRoomInBackGround(final int roomNumber, final String name, final boolean flag){       //방 생성,삭제 서버 전송
        //방 생성과 삭제를 위해 서버와 통신
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int roomID=0;
                String msg="";
                try {
                    //서버와 소켓 통신 시작
                    IP = information.getString("storedIp","");
                    System.out.println(IP);
                    socket = new Socket(InetAddress.getByName(IP), PORT);
                    os = new DataOutputStream(socket.getOutputStream());
                    is = new DataInputStream(socket.getInputStream());
                    //방 생성일경우
                    if(flag == true)    msg = "MK_" +information.getString("storedId","")+"TITLE_"+name+ "ROOM_"+roomNumber;
                    //방 삭제일경우
                    else if(flag == false) msg = "DR_"+ information.getString("storedId","")+"ROOM_"+roomNumber;

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
                SharedPreferences.Editor editor = information.edit();
                if(flag == true) {
                    editor.putString("room" + roomNumber, roomID + "");
                    Toast.makeText(getApplicationContext(), "방생성 ! roomCODE = "+roomID, Toast.LENGTH_SHORT).show();
                }else {
                    editor.putString("room" + roomNumber , "");
                    Toast.makeText(getApplicationContext(), "방삭제 ! roomCODE = "+roomID, Toast.LENGTH_SHORT).show();
                }
                editor.commit();
            }
        }.execute(null, null, null);


    }
}