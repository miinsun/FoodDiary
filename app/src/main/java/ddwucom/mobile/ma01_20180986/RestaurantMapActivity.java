package ddwucom.mobile.ma01_20180986;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class RestaurantMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    final static String TAG = "AllMapReview";
    final static int PERMISSION_REQ_CODE = 100;

    /*UI*/
    private EditText etKeyword;
    private GoogleMap mGoogleMap;
    private MarkerOptions markerOptions;

    TextView tvName;
    TextView tvAddress;
    TextView tvPhone;
    ImageView imageView;

    /*DATA*/
    private PlacesClient placesClient;
    private LocationManager locationManager;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        imageView = findViewById(R.id.imageView2);
        mapLoad();

        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        placesClient = Places.createClient(this);

        if(checkPermission()) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        else
            finish();

        searchStart();
    }

    /*입력된 유형의 주변 정보를 검색*/
    private void searchStart() {
        new NRPlaces.Builder().listener(placesListener)
                .key(getResources().getString(R.string.api_key))
                .latlng(Double.valueOf(lastKnownLocation.getLatitude()),
                        Double.valueOf(lastKnownLocation.getLongitude()))
//                .latlng(37.556,
//                        126.94) //이태원 주소
                .radius(100)
                .type(PlaceType.RESTAURANT)
                .build()
                .execute();
    }

    PlacesListener placesListener = new PlacesListener() {
        @Override
        public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
            Log.d(TAG, "Adding Markers");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //마커 추가
                    for(noman.googleplaces.Place place : places){
                        markerOptions.title(place.getName());
                        markerOptions.position(new LatLng(place.getLatitude(), place.getLongitude()));
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        Marker newMarker = mGoogleMap.addMarker(markerOptions);
                        newMarker.setTag(place.getPlaceId());
                        Log.d(TAG, place.getPlaceId() + ":" + place.getName());
                    }
                }
            });
        }

        @Override
        public void onPlacesFailure(PlacesException e) { }

        @Override
        public void onPlacesStart() { }

        @Override
        public void onPlacesFinished() { }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        markerOptions = new MarkerOptions();
        Log.d(TAG, "Map Ready");

        if(checkPermission()){
            mGoogleMap.setMyLocationEnabled(true); //내 위치 보여주는 버튼 보이기
        }
        else
            finish();

        LatLng currentLoc = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String placeId = marker.getTag().toString();
                getPlaceDetail(placeId);

                return false;
            }
        });
    }

    /*Place ID의 장소에 대한 세부정보 획득*/
    private void getPlaceDetail(String placeId){
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();


        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();

            tvAddress.setText(place.getAddress());
            tvName.setText(place.getName());
            tvPhone.setText(place.getPhoneNumber());

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Toast.makeText(this, "사진 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }

            try {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((photoResponse) -> {
                    Bitmap placeBitmap = photoResponse.getBitmap();
                    imageView.setImageBitmap(placeBitmap);
                }).addOnFailureListener(e -> {
                });
            } catch (NullPointerException e) {
            }
        });

    }

    /*구글맵을 멤버변수로 로딩*/
    private void mapLoad() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.reviewMap);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQ_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 퍼미션을 획득하였을 경우 맵 로딩 실행
                mapLoad();
            } else {
                // 퍼미션 미획득 시 액티비티 종료
                Toast.makeText(this, "앱 실행을 위해 권한 허용이 필요함", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
