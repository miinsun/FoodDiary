package ddwucom.mobile.ma01_20180986;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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

public class InsertContactActivity extends AppCompatActivity implements OnMapReadyCallback {
    final static String TAG = "InsertActivity";
    final static int PERMISSION_REQ_CODE = 100;
    final int REQUEST_TAKE_PHOTO = 200;

    private LocationManager locationManager;
    private GoogleMap mGoogleMap;
    private Location lastKnownLocation;
    private Geocoder geocoder;

    File photoFile;
    EditText etName;
    EditText etDate;
    EditText etReview;
    EditText etLocation;
    RatingBar ratingBar;
    Button button;
    ContactDBHelper helper;
    String mCurrentPhotoPath;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_contact);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        etName = findViewById(R.id.etName);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        button = (Button)findViewById(R.id.btnsearchlocation);
        imageView = findViewById(R.id.imageView);

        mapLoad();

        if(checkPermission()) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        etDate.setText(timeStamp);

        helper = new ContactDBHelper(this);

    }

    public void onClick(View v) throws IOException {
        SQLiteDatabase db = helper.getWritableDatabase();

        switch(v.getId()) {
            case R.id.btncaptureimage:
               dispatchPictureIntent();
                break;

            case R.id.btnaddcontact:
                //		DB 데이터 삽입 작업 수행
                ContentValues row = new ContentValues();

                double rate = ratingBar.getRating();
                row.put(ContactDBHelper.COL_NAME, etName.getText().toString());
                row.put(ContactDBHelper.COL_DATE, etDate.getText().toString());
                row.put(ContactDBHelper.COL_LOCATION, etLocation.getText().toString());
                row.put(ContactDBHelper.COL_REVIEW, etReview.getText().toString());
                row.put(ContactDBHelper.COL_RATING, String.valueOf(rate));
                if(mCurrentPhotoPath != null){
                    row.put(ContactDBHelper.COL_PHOTO, mCurrentPhotoPath);
                }


                db.insert(ContactDBHelper.TABLE_NAME, null, row);
                Toast.makeText(this, "리뷰가 정상 입력되었습니다.", Toast.LENGTH_LONG).show();
                finish();
                break;

            case R.id.btnClose:
                //			DB 데이터 삽입 취소 수행
                finish();
                break;
        }

        helper.close();
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

            LatLng currentLoc = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));
        }
        else
            finish();

        geocoder = new Geocoder(this);

        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(
                    lastKnownLocation.getLatitude(), // 위도
                    lastKnownLocation.getLongitude(), // 경도
                    1); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류");
        }

        if (list != null) {
            if (list.size()==0) {
                etLocation.setText("해당되는 주소 정보는 없습니다");
            } else {
                etLocation.setText(list.get(0).getAddressLine(0));
            }
        }

        // 버튼 이벤트
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
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

        mapFragment.getMapAsync( this);
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

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

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
}
