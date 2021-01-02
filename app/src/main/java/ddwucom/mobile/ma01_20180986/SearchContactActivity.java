package ddwucom.mobile.ma01_20180986;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SearchContactActivity extends AppCompatActivity {
    TextView tvSearchResult;
    EditText etSearchName;
    ContactDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);

        tvSearchResult = findViewById(R.id.tvSearchResult);
        etSearchName = findViewById(R.id.etSearchName);
        helper = new ContactDBHelper(this);
    }


    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSearchContactSave:
//			DB 검색 작업 수행
                SQLiteDatabase db = helper.getReadableDatabase();
//			String selection = ContactDBHelper.COL_NAME + "=?";
//			String[] selectArgs = new String[] {etSearchName.getText().toString()};
//			Cursor cursor = db.query(ContactDBHelper.TABLE_NAME, null, selection, selectArgs, null, null, null, null);

                //like로 이름의 일부만 입력해도 찾을 수 있음
                Cursor cursor = db.rawQuery("SELECT * FROM contact_table WHERE name like '%" + etSearchName.getText().toString() + "%'", null);
                String result = "";
                while (cursor.moveToNext()){
                    result += " 리뷰 ID : " + cursor.getInt( cursor.getColumnIndex (ContactDBHelper.COL_ID)) + " \n";
                    result += " 식당 명 : " + cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_NAME)) + " \n";
                    result += " 날짜 : " + cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_DATE)) + " \n";
                    result += " 장소 : " + cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_LOCATION)) + " \n";
                    result += " 리뷰 : " + cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_REVIEW)) + " \n";
                    result += " 평점 : " + cursor.getString(cursor.getColumnIndex(ContactDBHelper.COL_RATING)) + "\n";
                }

                tvSearchResult.setText(result);
                cursor.close();
                break;
            case R.id.btnClose :
                finish();
                break;
        }
    }
}
