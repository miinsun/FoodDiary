package ddwucom.mobile.ma01_20180986;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AllContactsActivity extends AppCompatActivity {

    ListView lvContacts = null;
    ContactDBHelper helper;
    Cursor cursor;
    //SimpleCursorAdapter adapter;
    MyCursorAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);
        lvContacts = (ListView)findViewById(R.id.lvContacts);

        helper = new ContactDBHelper(this);

        adapter = new MyCursorAdapter(this, R.layout.listview_layout, null);
        lvContacts.setAdapter(adapter);


//		리스트 뷰 클릭 처리
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(AllContactsActivity.this, UpdateActivity.class);
                intent.putExtra("_id", id);
                startActivity(intent);
            }
        });

        lvContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int _id = (int)id;
                AlertDialog.Builder builder = new AlertDialog.Builder(AllContactsActivity.this);
                builder.setTitle("리뷰 삭제")
                        .setMessage("리뷰를 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase db = helper.getWritableDatabase();
                                String whereClause = helper.COL_ID + "=?";
                                String[] whereArgs = new String[] { String.valueOf(_id) };
                                db.delete(ContactDBHelper.TABLE_NAME, whereClause, whereArgs);

                                Toast.makeText(AllContactsActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                                helper.close();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .setCancelable(false)
                        .show();
                return true;
            }
        });
    }

    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.btninsertcontact:
                intent = new Intent(this, InsertContactActivity.class);
                break;
            case R.id.btnSearchContact:
                intent = new Intent(this, SearchContactActivity.class);
                break;
        }

        if (intent != null) startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        DB에서 데이터를 읽어와 Adapter에 설정
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + ContactDBHelper.TABLE_NAME, null);
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
        helper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor 사용 종료
        if (cursor != null) cursor.close();
    }
}

