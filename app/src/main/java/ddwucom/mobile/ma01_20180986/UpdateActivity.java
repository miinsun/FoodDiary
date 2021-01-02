package ddwucom.mobile.ma01_20180986;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UpdateActivity extends AppCompatActivity implements OnMapReadyCallback {
    final static String TAG = "UpdateActivity";
    final static int PERMISSION_REQ_CODE = 100;

    private GoogleMap mGoogleMap;
    private Geocoder geocoder;
    final int REQUEST_TAKE_PHOTO = 200;

    ContactDBHelper helper;
    Cursor cursor;
    MyCursorAdapter adapter;

    Button button;
    File photoFile;
    String mCurrentPhotoPath;
    EditText etName;
    EditText etDate;
    EditText etReview;
    EditText etLocation;
    RatingBar ratingBar;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        adapter = new MyCursorAdapter(this, R.layout.listview_layout, null);
        Intent intent = getIntent();
        int _id = (int)intent.getLongExtra("_id", 0);

        if(helper == null){
            helper = new ContactDBHelper(this);// initialize your helper.
        }
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + ContactDBHelper.TABLE_NAME + " WHERE _id  = '" + _id + "';", null );

        button = (Button)findViewById(R.id.btnsearchlocation);
        etName = findViewById(R.id.etName);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        imageView = findViewById(R.id.imageView);

        mapLoad();

        String result = "";
        while(cursor.moveToNext()){
            etName.setText(cursor.getString(1));
            etDate.setText(cursor.getString(2));
            etReview.setText(cursor.getString(3));
            etLocation.setText(cursor.getString(4));
            mCurrentPhotoPath = cursor.getString(5);
            ratingBar.setRating(Float.parseFloat(cursor.getString(6)));
        }

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        if(bitmap != null)
            imageView.setImageBitmap(bitmap);

        cursor.close();
        helper.close();

    }

    public void onClick(View v) throws IOException {
        Intent intent = getIntent();
        Long _id = intent.getLongExtra("_id", 0);
        if(helper == null){
            helper = new ContactDBHelper(this);// initialize your helper.
        }
        SQLiteDatabase db = helper.getWritableDatabase();

        switch(v.getId()) {
            //사진 촬영
            case R.id.btncaptureimage:
                dispatchPictureIntent();
                break;

            case R.id.btnNewUpdateContact:
//                DB 데이터 업데이트 작업 수행
                ContentValues row = new ContentValues();
                double rate = ratingBar.getRating();
                row.put(ContactDBHelper.COL_NAME, etName.getText().toString());
                row.put(ContactDBHelper.COL_DATE, etDate.getText().toString());
                row.put(ContactDBHelper.COL_LOCATION, etLocation.getText().toString());
                row.put(ContactDBHelper.COL_RATING, String.valueOf(rate));
                row.put(ContactDBHelper.COL_REVIEW, etReview.getText().toString());
                row.put(ContactDBHelper.COL_PHOTO, mCurrentPhotoPath);

                String whereClause = ContactDBHelper.COL_ID + " = ? ";
                String[] whereArgs = new String[] {String.valueOf(_id)};
                db.update(ContactDBHelper.TABLE_NAME, row, whereClause, whereArgs);
                Toast.makeText(this, etName.getText().toString() + "이 성공적으로 수정되었습니다.", Toast.LENGTH_LONG).show();
                helper.close();
                finish();
                break;

            case R.id.btnClose:
//                DB 데이터 업데이트 작업 취소
                finish();
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

        if(checkPermission()) {
            mGoogleMap.setMyLocationEnabled(true); //내 위치 보여주는 버튼 보이기
        }
        else
            finish();
        geocoder = new Geocoder(this);

        String address = etLocation.getText().toString();
        String name = etName.getText().toString();
        searchLoaction();

        // 버튼 이벤트
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                searchLoaction();
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
        //SupportMapFragment mapFragment =
        //      (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);      // 매배변수 this: MainActivity 가 OnMapReadyCallback 을 구현하므로
    }


    /*원본 사진 요청*/
    public void dispatchPictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            photoFile = null;

            try{
                photoFile = createImageFile();
            }
            catch (IOException e){
                e.printStackTrace();
            }

            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "ddwucom.mobile.ma01_20180986.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void setPic(){
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor;
        try {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }
        catch(Exception e){
            return ;
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            setPic();
        }
    }

    public void searchLoaction(){
        String str = etLocation.getText().toString();
        List<Address> addressList = null;
        try {
            // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
            addressList = geocoder.getFromLocationName(
                    str, // 주소
                    10); // 최대 검색 결과 개수
        }
        catch (IOException e) {
            e.printStackTrace();
            etLocation.setText("");
            etLocation.setHint("검색할 수 없는 주소입니다.");
        }
        catch(NumberFormatException e){
            e.printStackTrace();
            etLocation.setText("");
            etLocation.setHint("검색할 수 없는 주소입니다.");
        }

        if((addressList != null) && (addressList.size() > 0)){
            Address address = addressList.get(0);
            etLocation.setText(addressList.get(0).getAddressLine(0));

            // 좌표(위도, 경도) 생성
            LatLng point = new LatLng(address.getLatitude(), address.getLongitude());
            // 마커 생성
            MarkerOptions mOptions2 = new MarkerOptions();
            mOptions2.title(str);
            mOptions2.snippet(addressList.get(0).getAddressLine(0));
            mOptions2.position(point);
            // 마커 추가
            mGoogleMap.addMarker(mOptions2);
            // 해당 좌표로 화면 줌
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
        }
        else{
            etLocation.setText("");
            etLocation.setHint("현재 위치에 주소를 찾을 수가 없습니다");
        }
    }
}