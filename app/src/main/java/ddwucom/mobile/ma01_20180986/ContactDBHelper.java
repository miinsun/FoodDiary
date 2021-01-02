package ddwucom.mobile.ma01_20180986;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "contact_db";
    public final static String TABLE_NAME = "contact_table";
    public final static String COL_ID = "_id";
    public final static String COL_NAME = "name";
    public final static String COL_DATE = "date";
    public final static String COL_REVIEW = "review";
    public final static String COL_LOCATION = "location";
    public final static String COL_RATING = "rating";
    public final static String COL_PHOTO = "photo";


    public ContactDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + " ( " + COL_ID + " integer primary key autoincrement, "
                + COL_NAME + " text, " + COL_DATE + " text, " + COL_REVIEW + " text, " + COL_LOCATION + " text, " + COL_PHOTO + " text, " + COL_RATING + " text )";
        db.execSQL(sql);

//		샘플 데이터
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '팔팔껍데기', '06/15', '레몬즙 + 돼지 껍데기는 최고의 조합!', '서울 강서구 공항대로 296-15', null, '2.9');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '제일곱창', '05/28', '파김치에 말아 먹는 소곱창, 구워주시는 솜씨가 상당함', '서울 성동구 고산자로 261', null, '2.9');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '엄지네 꼬막', '08/13', '꼬막 무침을 왕~~창 주심. 조금 짜긴하지만 공기밥 1만 추가하면 적당함', '강원 강릉시 남구길30번길 22', null, '2.9');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '마초 쉐프', '12/24', '처음 먹어보는 느낌의 삼겹살 피자, 무난한 스테이크집', '서울 광진구 능동로 125', null, '2.5');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES (null, '자매 육회집', '08/17', '광장시장가면 꼭 먹는 맛집 항상 웨이팅이 길지만 맛도 보장되는 곳', '서울 종로구 종로 200-4', null, '2.5');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + TABLE_NAME);
        onCreate(db);
    }

}
