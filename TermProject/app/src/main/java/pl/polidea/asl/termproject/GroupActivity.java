package pl.polidea.asl.termproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

    private DataOutputStream os;
    private DataInputStream is;
    private static final int PORT = 5555;
    private Socket socket;
    private String IP = "192.168.84.1";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        myinfo = PreferenceManager.getDefaultSharedPreferences(this);

        room1 = (Button) findViewById(R.id.bt_room1);
        room2 = (Button) findViewById(R.id.bt_room2);
        room3 = (Button) findViewById(R.id.bt_room3);
        room4 = (Button) findViewById(R.id.bt_room4);
        room5 = (Button) findViewById(R.id.bt_room5);

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


        if(myinfo.getString("room1","")==""){
            room1.setText("#NULL");
            room1.setTextColor(Color.parseColor("#0000FF"));
        }
        if(myinfo.getString("room2","")==""){
            room2.setText("#NULL");
            room2.setTextColor(Color.parseColor("#0000FF"));
        }
        if(myinfo.getString("room3","")==""){
            room3.setText("#NULL");
            room3.setTextColor(Color.parseColor("#0000FF"));
        }
        if(myinfo.getString("room4","")==""){
            room4.setText("#NULL");
            room4.setTextColor(Color.parseColor("#0000FF"));
        }
        if(myinfo.getString("room5","")==""){
            room5.setText("#NULL");
            room5.setTextColor(Color.parseColor("#0000FF"));
        }       //방 초기화
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
        }
        Toast.makeText(getApplicationContext(), "CLICK", Toast.LENGTH_SHORT).show();
        if(myinfo.getString("room"+flag,"") == ""){
            createRoom(flag);
        }
        else if(myinfo.getString("room"+flag,"") != ""){
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

    public void createRoom(final int roomNumber){
        LayoutInflater inflater = getLayoutInflater();
        final View view_enroll = inflater.inflate(R.layout.activity_dialogue_creatroom, null);
        final EditText roomName = (EditText) view_enroll.findViewById(R.id.et_roomName);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
        buider.setIcon(R.mipmap.ic_launcher);
        buider.setTitle("CREATE SHARE GROUP"); //Dialog 제목
        buider.setView(view_enroll); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("생성", new DialogInterface.OnClickListener() {
            //Dialog에 "Complite"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = roomName.getText().toString();//사용자이름

                switch (roomNumber){
                    case 1: room1.setText(name);    room1.setTextColor(Color.parseColor("#000000"));    break;
                    case 2: room2.setText(name);    room2.setTextColor(Color.parseColor("#000000"));    break;
                    case 3: room3.setText(name);    room3.setTextColor(Color.parseColor("#000000"));    break;
                    case 4: room4.setText(name);    room4.setTextColor(Color.parseColor("#000000"));    break;
                    case 5: room5.setText(name);    room5.setTextColor(Color.parseColor("#000000"));    break;
                }
                //서버에 전송, 서버에서 받음.

                SharedPreferences.Editor editor = myinfo.edit();
                editor.putString("room" + roomNumber, roomNumber + "");
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

    public void inforRoom(int roomNumber){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                GroupActivity.this);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setTitle("방 제목 :"+myinfo.getString("room"+roomNumber,""));

        // List Adapter 생성
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupActivity.this,
                android.R.layout.select_dialog_singlechoice);
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
                innBuilder.setMessage(strName);
                innBuilder.setTitle("당신이 선택한 것은 ");
                innBuilder.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                innBuilder.show();
            }
        });
        alertBuilder.show();
    }

    public void createRoomInBackGround(final int roomNumber, final String name, final boolean flag){       //방 생성 서버 전송
        final String msg = "";

        //final int roomID = 0;
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int roomID=0;
                String msg="";//+myinfo.getString("storedId","");
                try {
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
                    editor.putString("room" + roomNumber + "id", roomID + "");
                    Toast.makeText(getApplicationContext(), "방생성 ! roomID = "+roomID, Toast.LENGTH_SHORT).show();
                }else {
                    editor.putString("room" + roomNumber + "id", "");
                    Toast.makeText(getApplicationContext(), "방삭제 ! roomID = 0", Toast.LENGTH_SHORT).show();
                }
                editor.commit();
            }
        }.execute(null, null, null);


    }
}