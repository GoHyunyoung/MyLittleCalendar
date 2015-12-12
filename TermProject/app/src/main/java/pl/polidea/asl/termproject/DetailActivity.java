package pl.polidea.asl.termproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class DetailActivity extends Activity implements OnClickListener {
    private static final int DIALOG_DATE =0;
    private static final int DIALOG_TIME = 1;

    int mId;
    String today;
    String title;
    String time;
    int year,month,day;
    int hh,mm;
    boolean aa;

    String selectedDate;
    EditText edt_title;
    Button btn_del, btn_date, btn_time;
    int Param;
    SQLiteDatabase db;
    MyDBHelper myDBHelper;
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        btn_date = (Button) findViewById(R.id.btn_date);
        edt_title = (EditText) findViewById(R.id.edt_title);
        btn_time = (Button) findViewById(R.id.btn_time);
        btn_del = (Button) findViewById(R.id.btn_del);

        Intent intent = getIntent();
        mId = intent.getIntExtra("_id", -1);
        Param = intent.getIntExtra("Param", -1);
        selectedDate = intent.getStringExtra("Date");
        myDBHelper = new MyDBHelper(this, "Today.db", null, 1);
        db=myDBHelper.getWritableDatabase();

        btn_date.setOnClickListener(this);
        btn_time.setOnClickListener(this);

        //일정추가시
        if (Param == 1) {
            today = selectedDate;
            long curTime = System.currentTimeMillis();
            time = DateFormat.format("AA hh시 mm분",curTime).toString();
        }

        //일정수정시
        else if (Param == 2) {
            Cursor c = db.rawQuery("SELECT * FROM today WHERE _id = "+ mId , null);
//            startManagingCursor(c);

            c.moveToNext();
            title = c.getString(1);
            today = c.getString(2);
            time = c.getString(3);
        }

        String[] tmp = today.split("/");
        year = Integer.parseInt(today.split("/")[0]);
        month = Integer.parseInt(today.split("/")[1]);
        day = Integer.parseInt(today.split("/")[2]);

        today=year+"/"+month+"/"+day;
        btn_date.setText(today);
        edt_title.setText(title);
        btn_time.setText(time);

        Button btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        Button btn_del = (Button) findViewById(R.id.btn_del);
        btn_del.setOnClickListener(this);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        //일정추가시
        if (Param == 1)
            btn_del.setVisibility(View.INVISIBLE);
            //일정수정시
        else
            btn_del.setVisibility(View.VISIBLE);

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                btn_date.setText(year+"/"+(monthOfYear+1)+"/"+dayOfMonth);
            }
        };

        timeSetListener = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //13시부터는 오후를 붙이고
                if(hourOfDay >= 12)
                    btn_time.setText("오후 "+(hourOfDay-12)+"시 "+minute+"분");

                //이전은 오전을 붙이고
                else
                    btn_time.setText("오전 "+hourOfDay+"시 "+minute+"분");
            }
        };
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        switch (v.getId()) {
            case R.id.btn_save:
                //일정추가의 경우
                if (Param == 1) {
                    db.execSQL("INSERT INTO today VALUES(null, '"
                            + edt_title.getText().toString() + "', '"
                            + btn_date.getText().toString() + "', '"
                            + btn_time.getText().toString() + "');");
                }
                //일정수정의 경우
                else if (Param == 2) {
                    db.execSQL("UPDATE today SET title='"
                            + edt_title.getText().toString() + "',date='"
                            + btn_date.getText().toString() + "', time='"
                            + btn_time.getText().toString() + "' WHERE _id='" + mId
                            + "';");
                }
                myDBHelper.close();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.btn_del:
                //일정 수정의 경우
                db.execSQL("DELETE FROM today WHERE _id='" + mId + "'");
                myDBHelper.close();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.btn_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;

            //일자버튼을 누른경우
            case R.id.btn_date:
                showDialog(DIALOG_DATE);
                break;

            case R.id.btn_time:
                showDialog(DIALOG_TIME);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id){
            case DIALOG_DATE:
                return new DatePickerDialog(this,dateSetListener,year,month-1,day);
//                return new DatePickerDialog(this,dateSetListener,2010,0,15);

            case DIALOG_TIME:
                //24시간표시제 해제
                return new TimePickerDialog(this,timeSetListener,hh,mm,false);
        }
        return super.onCreateDialog(id);
    }
}