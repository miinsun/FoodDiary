package ddwucom.mobile.ma01_20180986;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

//과제명: 맛집 리뷰 관리 앱
//분반: 01분반
//학번: 20180986 성명: 운민선
//제출일: 2020년 12월 22일

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.btnOpenAllContact:
                intent = new Intent(this, AllContactsActivity.class);
                break;
            case R.id.btnSearchrestaurant:
                intent = new Intent(this, RestaurantMapActivity.class);
                break;
            case R.id.btnAllReviewMap:
                intent = new Intent(this, AllReviewMapActivity.class);
                break;
        }

        if (intent != null) startActivity(intent);
    }

}
