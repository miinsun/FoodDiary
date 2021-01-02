package ddwucom.mobile.ma01_20180986;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AllReviewMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    final static String TAG = "InsertActivity";
    final static int PERMISSION_REQ_CODE = 100;
    final int REQUEST_TAKE_PHOTO = 200;

    private LocationManager locationManager;
    private GoogleMap mGoogleMap;
    private Location lastKnownLocation;
    private Geocoder geocoder;
    ContactDBHelper helper;
    Cursor cursor;

    ConstraintLayout csLayout;
    File photoFile;
    TextView tvName;
    TextView tvDate;
    TextView tvReview;
    TextView tvAddress;
    TextView tvRating;
    String mCurrentPhotoPath;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_review_map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        tvName = findViewById(R.id.tvName);
        tvDate = findViewById(R.id.tvDate);
        tvAddress = findViewById(R.id.tvAddress);
        tvRating = findViewById(R.id.tvRating);
        tvReview = findViewById(R.id.tvReview);
        imageView = findViewById(R.id.imageView2);

        if(helper == null){
            helper = new ContactDBHelper(this);// initialize your helper.
        }
        
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + ContactDBHelper.TABLE_NAME + ";", null );
        
        mapLoad();

        if(checkPermission()) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        else
            finish();

        helper = new ContactDBHelper(this);
    }

    public void onClick(View v) throws IOException {
        switch(v.getId()) {
            case R.id.btnShareMap:
                capturePictureIntent();
                break;
        }
    }

    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap){
        mGoogleMap = googleMap;
        Log.d(TAG, "MapReady");

        if(checkPermission()){
            mGoogleMap.setMyLocationEnabled(true); //내 위치 보여주는 버튼 보이기
        }
        else
            finish();

        LatLng currentLoc = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 11));

        geocoder = new Geocoder(this);

        //DB접근
        while(cursor.moveToNext()){
            getLatlng(cursor.getString(4), cursor.getString(1));
        }

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                SQLiteDatabase db = helper.getReadableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM contact_table WHERE name like '%" + marker.getTag().toString() + "%'", null);
                String result = "";

                while(cursor.moveToNext()){
                    tvName.setText(cursor.getString(1));
                    tvDate.setText(cursor.getString(2));
                    tvReview.setText(cursor.getString(3));
                    tvAddress.setText(cursor.getString(4));
                    mCurrentPhotoPath = cursor.getString(5);
                    tvRating.setText(cursor.getString(6));
                }

                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                if(bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                else{
                    Drawable drawable = getResources().getDrawable(R.drawable.defaultimage);
                    imageView.setImageDrawable(drawable);
                }
                helper.close();
                return false;
            }
        });
    }

    /* 필요 permission 요청 */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }


    /*구글맵을 멤버변수로 로딩*/
    private void mapLoad() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.reviewMap);

        mapFragment.getMapAsync( AllReviewMapActivity.this);
    }
    


    public LatLng getLatlng(String str, String name) {
        List<Address> addressList = null;
        try {
            // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
            addressList = geocoder.getFromLocationName(
                    str, // 주소
                    1); // 최대 검색 결과 개수
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch(NumberFormatException e){
            e.printStackTrace();
        }
        LatLng point = null;
        if((addressList != null) && (addressList.size() > 0)){
            Address address = addressList.get(0);
            // 좌표(위도, 경도) 생성
            point = new LatLng(address.getLatitude(), address.getLongitude());
            
            // 마커 생성
            MarkerOptions mOptions2 = new MarkerOptions();
            mOptions2.title(name);
            mOptions2.snippet(addressList.get(0).getAddressLine(0));
            mOptions2.position(point);

            // 마커 추가
            Marker newMarker = mGoogleMap.addMarker(mOptions2);
            newMarker.setTag(name);
        }

        return point;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor 사용 종료
        if (cursor != null) cursor.close();
    }

    /*원본 사진 요청*/
    public void capturePictureIntent() throws IOException {
        View container;
        container = getWindow().getDecorView();
        container.buildDrawingCache();
        Bitmap captureView = container.getDrawingCache();
        FileOutputStream fos;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        Uri uri = FileProvider.getUriForFile(this, "ddwucom.mobile.ma01_20180986.fileprovider",
                image);

        try{
            fos = new FileOutputStream(mCurrentPhotoPath);
            captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Intent shareintent = new Intent(Intent.ACTION_SEND);

        shareintent.putExtra(Intent.EXTRA_STREAM, uri);
        shareintent.setType("image/*");
        startActivity(Intent.createChooser(shareintent, "나만의 지도를 공유해보세요"));
    }

}