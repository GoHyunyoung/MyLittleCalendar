package pl.polidea.asl.termproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class CalendarActivity extends Activity implements OnClickListener,
        OnItemClickListener {
    ArrayList<String> m_date;
    ArrayList<String> m_tasklist;
    CustomAdapter gv_adapter;
    ArrayAdapter<String> lv_adapter;
    GridView grid;
    ListView lview;

    //텍스트뷰인 년도와 월
    TextView tv_year;
    TextView tv_month;

    //일정추가 버튼
    ImageView btn_addTask;
    //공유버튼
    ImageButton btn_share;

    //일정관리를 위한 DB
    MyDBHelper myDBHelper;
    SQLiteDatabase db;
    Cursor cursor;
    SimpleCursorAdapter adapter;

    //선택된일자를 저장하기 위한 변수
    String selectedDate;
    //다음달,이전달 버튼
    Button btn_prev_month, btn_next_month;
    //현재 년도와 달
    int year, mon;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        myDBHelper = new MyDBHelper(this, "Today.db", null, 1);
        tv_year = (TextView) findViewById(R.id.tv_year);
        tv_month = (TextView) findViewById(R.id.tv_month);
        btn_addTask = (ImageView) findViewById(R.id.btn_addtask);

        //btn_share = (ImageButton) findViewById(R.id.btn_share);
        btn_next_month = (Button) findViewById(R.id.btn_next_month);
        btn_prev_month = (Button) findViewById(R.id.btn_prev_month);
        m_date = new ArrayList<String>();

        m_tasklist = new ArrayList<String>();
        lv_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, m_tasklist);

        db = myDBHelper.getWritableDatabase();

        lview = (ListView) this.findViewById(R.id.lv_task);
        lview.setAdapter(lv_adapter);

        grid = (GridView) this.findViewById(R.id.gv_cal);
        gv_adapter = new CustomAdapter(this, m_date);
        grid.setAdapter(gv_adapter);

        btn_addTask.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_addtask));
        //btn_share.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_share));

        btn_addTask.setOnClickListener(new  OnClickListener() {
            @Override
            public void onClick(View view) {
                try{

                    Integer.parseInt(selectedDate.split("/")[2]);

                    Intent intent = new Intent(CalendarActivity.this, Detail.class);
                    intent.putExtra("Date", selectedDate);
                    //addTask버튼 누를시 Param에 1전달
                    intent.putExtra("Param", 1);
                    startActivity(intent);
                }

                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "날짜를 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        grid.setOnItemClickListener(this);
        lview.setOnItemClickListener(this);
        btn_prev_month.setOnClickListener(this);
        btn_next_month.setOnClickListener(this);

        Date date = new Date();// 오늘에 날짜를 세팅 해준다.
        year = date.getYear() + 1900;
        mon = date.getMonth() + 1;
        tv_year.setText(year + "");
        tv_month.setText(mon + "");

        fillDate(year, mon);
//        fillColor(year,mon);


    }

    private class CustomAdapter<String> extends BaseAdapter implements OnClickListener {
        ArrayList m_date;

        //임시
        LinearLayout prev_colored;
        LinearLayout next_colored;
        boolean flag = false;
//        LayoutInflater inflater;

        public CustomAdapter(CalendarActivity calendarActivity, ArrayList<String> m_date) {
            this.m_date = m_date;
        }

        @Override
        public int getCount() {
            return m_date.size();
        }

        @Override
        public Object getItem(int position) {
            return m_date.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.cell, null);

            TextView tv = (TextView) v.findViewById(R.id.date);
            ImageView iv1 = (ImageView) v.findViewById(R.id.task1);
            ImageView iv2 = (ImageView) v.findViewById(R.id.task2);
            ImageView iv3 = (ImageView) v.findViewById(R.id.task3);
            LinearLayout layout = (LinearLayout)v.findViewById(R.id.layout);
            tv.setText(getItem(position).toString());

            cursor = db.rawQuery("SELECT * FROM today WHERE date = '" + year + "/" + mon + "/" + getItem(position) + "'", null);

            if (cursor.moveToNext()) {
                 //레이아웃을 빨강으로처리
                layout.setBackgroundColor(0x55f90000);
                //문구에있는 내용에 따라 아이콘모양 추가
                if (cursor.getString(1).contains("생일") ||
                        cursor.getString(1).contains("결혼") ||
                        cursor.getString(1).contains("축하"))
                    iv1.setImageDrawable(getResources().getDrawable(R.drawable.ic_birth));
                else if (cursor.getString(1).contains("회의") ||
                        cursor.getString(1).contains("미팅") ||
                        cursor.getString(1).contains("토론") ||
                        cursor.getString(1).contains("모임"))
                    iv1.setImageDrawable(getResources().getDrawable(R.drawable.ic_meeting));
                else if (cursor.getString(1).contains("공연") ||
                        cursor.getString(1).contains("축제") ||
                        cursor.getString(1).contains("여행")||
                        cursor.getString(1).contains("할인"))
                    iv1.setImageDrawable(getResources().getDrawable(R.drawable.ic_ticket));
                else if (cursor.getString(1).contains("과제") ||
                        cursor.getString(1).contains("시험") ||
                        cursor.getString(1).contains("퀴즈") ||
                        cursor.getString(1).contains("작성"))
                    iv1.setImageDrawable(getResources().getDrawable(R.drawable.ic_test));
                else
                    iv1.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            }
            if (cursor.moveToNext()) {
                if (cursor.getString(1).contains("생일") ||
                        cursor.getString(1).contains("결혼") ||
                        cursor.getString(1).contains("축하"))
                    iv2.setImageDrawable(getResources().getDrawable(R.drawable.ic_birth));
                else if (cursor.getString(1).contains("회의") ||
                        cursor.getString(1).contains("미팅") ||
                        cursor.getString(1).contains("토론") ||
                        cursor.getString(1).contains("모임"))
                    iv2.setImageDrawable(getResources().getDrawable(R.drawable.ic_meeting));
                else if (cursor.getString(1).contains("공연") ||
                        cursor.getString(1).contains("축제") ||
                        cursor.getString(1).contains("여행") ||
                        cursor.getString(1).contains("할인"))
                    iv2.setImageDrawable(getResources().getDrawable(R.drawable.ic_ticket));
                else if (cursor.getString(1).contains("과제") ||
                        cursor.getString(1).contains("시험") ||
                        cursor.getString(1).contains("퀴즈") ||
                        cursor.getString(1).contains("작성"))
                    iv2.setImageDrawable(getResources().getDrawable(R.drawable.ic_test));
                else
                    iv2.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            }

            if (cursor.moveToNext()) {
                if (cursor.getString(1).contains("생일") ||
                        cursor.getString(1).contains("결혼") ||
                        cursor.getString(1).contains("축하"))
                    iv3.setImageDrawable(getResources().getDrawable(R.drawable.ic_birth));
                else if (cursor.getString(1).contains("회의") ||
                        cursor.getString(1).contains("미팅") ||
                        cursor.getString(1).contains("토론") ||
                        cursor.getString(1).contains("모임"))
                    iv3.setImageDrawable(getResources().getDrawable(R.drawable.ic_meeting));
                else if (cursor.getString(1).contains("공연") ||
                        cursor.getString(1).contains("축제") ||
                        cursor.getString(1).contains("여행") ||
                        cursor.getString(1).contains("할인"))
                    iv3.setImageDrawable(getResources().getDrawable(R.drawable.ic_ticket));
                else if (cursor.getString(1).contains("과제") ||
                        cursor.getString(1).contains("시험") ||
                        cursor.getString(1).contains("퀴즈") ||
                        cursor.getString(1).contains("작성"))
                    iv3.setImageDrawable(getResources().getDrawable(R.drawable.ic_test));
                else
                    iv3.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            }

            layout.setOnClickListener(this);

            return v;
        }

        @Override
        public void onClick(View v) {
            TextView tv_date = (TextView) v.findViewById(R.id.date);
            //처음누른것이면
            if(!flag) {
                prev_colored = (LinearLayout) v;
                //옅은파란색으로 처리
                prev_colored.setBackgroundColor(0x550004f9);
                flag=true;
            }
            //처음이 아니라면
            else {
                next_colored = ( LinearLayout)v;
                prev_colored.setBackgroundColor(Color.TRANSPARENT);

                //만약 일정이 있는 날이면 빨간색
                cursor = db.rawQuery("SELECT * FROM today WHERE date = '" + year + "/" + mon + "/" + ((TextView)prev_colored.getChildAt(0)).getText() + "'", null);
                if (cursor.moveToNext())
                    //옅은빨강으로처리
                    prev_colored.setBackgroundColor(0x55f90000);

                //옅은파란색으로 처리
                next_colored.setBackgroundColor(0x550004f9);
                prev_colored=next_colored;
            }
            setTask(tv_year.getText() + "/"
                    + tv_month.getText() + "/"
                    + tv_date.getText());

            selectedDate = tv_year.getText() + "/"
                    + tv_month.getText() + "/"
                    + tv_date.getText();
        }
    }

    //화면전환시 항상 reload
    @Override
    protected void onResume() {
        super.onResume();

        m_date = new ArrayList<String>();
        gv_adapter = new CustomAdapter(this, m_date);

        m_tasklist = new ArrayList<String>();
        lv_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, m_tasklist);

        grid.setAdapter(gv_adapter);
        lview.setAdapter(lv_adapter);

        //gridView에 일자들을 채움
        fillDate(year, mon);
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {


            //다음달 버튼 클릭시
            case R.id.btn_next_month:
                Date date = new Date();// 오늘에 날짜를 세팅 해준다.
                mon += 1;
                //12달을 넘기면 year+1
                if (mon > 12) {
                    mon = 1;
                    year += 1;
                }
                tv_year.setText(year + "");
                tv_month.setText(mon + "");



                fillDate(year, mon);
                break;

            //이전달 버튼 클릭시
            case R.id.btn_prev_month:
                date = new Date();// 오늘에 날짜를 세팅 해준다.
                mon -= 1;
                //12달을 넘기면 year+1
                if (mon < 1) {
                    mon = 12;
                    year -= 1;
                }
                tv_year.setText(year + "");
                tv_month.setText(mon + "");

                fillDate(year, mon);
                break;
        }
    }

    private void fillDate(int year, int month) {
        m_date.clear();

        m_date.add("일");
        m_date.add("월");
        m_date.add("화");
        m_date.add("수");
        m_date.add("목");
        m_date.add("금");
        m_date.add("토");

        Date current = new Date(year - 1900, month - 1, 1);
        int day = current.getDay(); // 요일도 int로 저장.

        for (int i = 0; i < day; i++) {
            m_date.add("");
        }

        current.setDate(32);// 32일까지 입력하면 1일로 바꿔준다.
        int last = 32 - current.getDate();

        for (int i = 1; i <= last; i++) {
            m_date.add(i + "");
        }

        gv_adapter.notifyDataSetChanged();

    }

    //일자를 클릭할때마다, listView내용이 변경
    private void setTask(String today) {
        //SELECT * FROM today WHERE YEAR = 'year'

        cursor = db.rawQuery("SELECT * FROM today WHERE date = '" + today + "'", null);
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
                new String[]{"time", "title"},
                new int[]{android.R.id.text1, android.R.id.text2});
        ListView list = (ListView) findViewById(R.id.lv_task);
        list.setAdapter(adapter);

        lv_adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long arg3) {
        //날짜를 클릭한경우
        switch (parent.getId()) {
            //리스트를 클릭한경우
            //이미 있는 일정을 수정하거나 삭제
            case R.id.lv_task:
//                Toast.makeText(getApplicationContext(), "list_Clicked", Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
                //커서 객체를 통해 일정의 타이틀을 가져옴
                Cursor c = (Cursor) parent.getItemAtPosition(pos);
//                Toast.makeText(getApplicationContext(),c.getString(1),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Detail.class);
                intent.putExtra("_id", Integer.parseInt(c.getString(0)));
                //lv_Task버튼 누를시 Param에 2전달
                intent.putExtra("Param", 2);
                startActivity(intent);
                break;
        }

    }

}