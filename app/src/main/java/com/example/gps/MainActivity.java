package com.example.gps;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng = null;
    private LatLng startLatLng = null;
    private LatLng endLatLng = null;

    private Marker currentMarker, startMarker, endMarker;
    private Handler handler = new Handler();
    private float moveSpeed = 0.00005f;
    private int moveDuration = 10000;

    private Button btnSelectStart, btnSelectEnd, btnStartMove, btnToggleJoystick;
    private JoystickView joystick;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectStart = findViewById(R.id.btnSelectStart);
        btnSelectEnd = findViewById(R.id.btnSelectEnd);
        btnStartMove = findViewById(R.id.btnStartMove);
        btnToggleJoystick = findViewById(R.id.btnToggleJoystick); // ✅ 버튼 연결
        joystick = findViewById(R.id.joystick);
        mainLayout = findViewById(R.id.mainLayout);

        joystick.setVisibility(JoystickView.GONE); // 기본은 숨김

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSelectStart.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                startLatLng = selectedLatLng;
                if (startMarker != null) startMarker.remove();
                startMarker = mMap.addMarker(new MarkerOptions().position(startLatLng).title("출발지"));
            }
        });

        btnSelectEnd.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                endLatLng = selectedLatLng;
                if (endMarker != null) endMarker.remove();
                endMarker = mMap.addMarker(new MarkerOptions().position(endLatLng).title("도착지"));
            }
        });

        btnStartMove.setOnClickListener(v -> {
            if (startLatLng != null && endLatLng != null) {
                startAutoMove();
            } else {
                Toast.makeText(this, "출발지와 도착지를 먼저 설정하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ 버튼 클릭 시 조이스틱 토글
        btnToggleJoystick.setOnClickListener(v -> {
            if (joystick.getVisibility() == JoystickView.VISIBLE) {
                joystick.setVisibility(JoystickView.GONE);
                btnToggleJoystick.setText("조이스틱 켜기");
            } else {
                joystick.setVisibility(JoystickView.VISIBLE);
                btnToggleJoystick.setText("조이스틱 끄기");
            }
        });

        joystick.setJoystickListener((xPercent, yPercent) -> {
            if (currentMarker != null) {
                double lat = currentMarker.getPosition().latitude - yPercent * moveSpeed;
                double lng = currentMarker.getPosition().longitude + xPercent * moveSpeed;
                LatLng newPos = new LatLng(lat, lng);
                currentMarker.setPosition(newPos);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng korea = new LatLng(37.5665, 126.9780);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(korea, 15));

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            Toast.makeText(this, "선택 위치: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
        });
    }

    private void startAutoMove() {
        if (startLatLng == null || endLatLng == null) return;

        if (currentMarker != null) currentMarker.remove();
        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title("현재위치")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        final long interval = 100;
        final int totalSteps = moveDuration / (int) interval;
        final double latDiff = (endLatLng.latitude - startLatLng.latitude) / totalSteps;
        final double lngDiff = (endLatLng.longitude - startLatLng.longitude) / totalSteps;

        handler.post(new Runnable() {
            int step = 0;
            LatLng cur = startLatLng;

            @Override
            public void run() {
                if (step < totalSteps) {
                    cur = new LatLng(cur.latitude + latDiff, cur.longitude + lngDiff);
                    currentMarker.setPosition(cur);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(cur));
                    step++;
                    handler.postDelayed(this, interval);
                } else {
                    Toast.makeText(MainActivity.this, "이동 완료", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
