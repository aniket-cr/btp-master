/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import com.google.android.gms.analytics.Logger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by DC
 */


public class TopView extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, GoogleMap.OnMapLongClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {


    private static LatLng gpsLocation;

//    private static final LatLng INDIA = new LatLng(-33.86365, 151.20589);
//
//    private static final LatLng PAK = new LatLng(-33.88365, 151.20389);
//
//    private static final LatLng ENG = new LatLng(-33.87365, 151.21689);
//
//    private static final LatLng DC = new LatLng(-33.86165, 151.21892);


    private static final double DEFAULT_RADIUS = 1;

    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    private static final int WIDTH_MAX = 50;

    private static final int HUE_MAX = 360;

    private static final int ALPHA_MAX = 255;

    private GoogleMap mMap;

    private List<DraggableCircle> mCircles = new ArrayList<DraggableCircle>(1);

    private SeekBar mColorBar;

    private SeekBar mAlphaBar;

    private SeekBar mWidthBar;

    private int mStrokeColor;

    private int mFillColor;

    private int mWidth;

    private class DraggableCircle {

        //private final Marker centerMarker;

        //private final Marker radiusMarker;

        private final Circle circle;

        private double radius;

        public DraggableCircle(LatLng center, double radius) {
            this.radius = radius;
//            centerMarker = mMap.addMarker(new MarkerOptions()
//                    .position(center)
//                    .draggable(true));
//            radiusMarker = mMap.addMarker(new MarkerOptions()
//                    .position(toRadiusLatLng(center, radius))
//                    .draggable(true)
//                    .icon(BitmapDescriptorFactory.defaultMarker(
//                            BitmapDescriptorFactory.HUE_AZURE)));
            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(mWidth)
                    .strokeColor(mStrokeColor)
                    .fillColor(mFillColor));
        }

        public DraggableCircle(LatLng center, LatLng radiusLatLng) {
            this.radius = toRadiusMeters(center, radiusLatLng);
//            centerMarker = mMap.addMarker(new MarkerOptions()
//                    .position(center)
//                    .draggable(true));
//            radiusMarker = mMap.addMarker(new MarkerOptions()
//                    .position(radiusLatLng)
//                    .draggable(true)
//                    .icon(BitmapDescriptorFactory.defaultMarker(
//                            BitmapDescriptorFactory.HUE_AZURE)));
            circle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeWidth(mWidth)
                    .strokeColor(mStrokeColor)
                    .fillColor(mFillColor));
        }

//        public boolean onMarkerMoved(Marker marker) {
//            if (marker.equals(centerMarker)) {
//                circle.setCenter(marker.getPosition());
//                radiusMarker.setPosition(toRadiusLatLng(marker.getPosition(), radius));
//                return true;
//            }
//            if (marker.equals(radiusMarker)) {
//                radius = toRadiusMeters(centerMarker.getPosition(), radiusMarker.getPosition());
//                circle.setRadius(radius);
//                return true;
//            }
//            return false;
//        }

        public void onStyleChange() {
            circle.setStrokeWidth(mWidth);
            circle.setFillColor(mFillColor);
            circle.setStrokeColor(mStrokeColor);
        }
    }


    /** Generate LatLng of radius marker */
    private static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }


    GeolocationService gps;

    private CheckBox mMyLocationCheckbox;

    protected LocationListener locationListener;
    protected LocationManager locationManager;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_demo);

        mMyLocationCheckbox = (CheckBox) findViewById(R.id.my_location);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //GeolocationService Locationfetcher = new GeolocationService(,getApplicationContext());
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     */
    @Override
    public void onMapReady(GoogleMap map) {
        //   map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

//        updateMyLocation();

        gps = new GeolocationService(TopView.this);


        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            final LatLng playerLocation = new LatLng(latitude, longitude);


            gpsLocation = new LatLng(latitude,longitude);

            Log.d("location",latitude+ " ");

            mMap = map;
            updateMapType();
            mMap.setOnMapLongClickListener(this);

            mFillColor = Color.RED;
            mStrokeColor = Color.BLACK;
            mWidth = 2;
           // PolygonOptions options = new PolygonOptions().addAll(createRectangle(playerLocation, 500, 8));

            map.addPolygon(new PolygonOptions()
                    .addAll(createRectangle(new LatLng(latitude, longitude), .00025, .00025))
                    .strokeColor(Color.BLUE)
                    .strokeWidth(5));

            DraggableCircle circle = new DraggableCircle(gpsLocation, DEFAULT_RADIUS);
            UpdateBubbles(latitude,longitude);
             mCircles.add(circle);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 19.5f));

            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }


    }


    private List<LatLng> createRectangle(LatLng center, double halfWidth, double halfHeight) {
        return Arrays.asList(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Don't do anything here.
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Don't do anything here.
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mFillColor = Color.RED;

        for (DraggableCircle draggableCircle : mCircles) {
            draggableCircle.onStyleChange();
        }
    }

    private void updateMapType(){

        mMap.setMapType(2);
        // 2 IS CONSTANT VALUE FOR SATELLITE MAP

    }

    private void UpdateBubbles(double lat,double lon){

        mMap.setOnMapLongClickListener(this);

        mFillColor = Color.BLUE;
        mStrokeColor = Color.BLACK;
        mWidth = 2;

        DraggableCircle circle2 = new DraggableCircle(new LatLng(lat+0.00006, lon+0.00009),2* DEFAULT_RADIUS);
        DraggableCircle circle3 = new DraggableCircle(new LatLng(lat-0.00006, lon+0.00015),1.1* DEFAULT_RADIUS);
        DraggableCircle circle4 = new DraggableCircle(new LatLng(lat-0.00010, lon-0.00015), 1.5*DEFAULT_RADIUS);
        DraggableCircle circle9 = new DraggableCircle(new LatLng(lat+0.00020, lon+0.00019), 1.5*DEFAULT_RADIUS);

        mFillColor = Color.YELLOW;
        mStrokeColor = Color.BLACK;
        mWidth = 2;


        DraggableCircle circle5 = new DraggableCircle(new LatLng(lat+0.00010, lon-0.00012),0.8* DEFAULT_RADIUS);
        DraggableCircle circle6 = new DraggableCircle(new LatLng(lat-0.0002, lon+0.00012),0.5* DEFAULT_RADIUS);
        DraggableCircle circle7 = new DraggableCircle(new LatLng(lat+0.00011, lon),2.5* DEFAULT_RADIUS);
        DraggableCircle circle8 = new DraggableCircle(new LatLng(lat+0.00021, lon-0.00020),1.4* DEFAULT_RADIUS);

      //  Log.d("location",longitude+ " ");
        //mCircles.add(circle);
        mCircles.add(circle2);
        mCircles.add(circle3);

        mCircles.add(circle4);

        mCircles.add(circle5);
    //    Log.d("location4234",longitude+ " 111 ");
        //UpdateBubbles(latitude,longitude);
        mCircles.add(circle6);
        mCircles.add(circle7);
        mCircles.add(circle8);

//            DraggableCircle circle2 = new DraggableCircle(INDIA, DEFAULT_RADIUS);
//            DraggableCircle circle3 = new DraggableCircle(PAK, DEFAULT_RADIUS);
//            DraggableCircle circle4 = new DraggableCircle(ENG, DEFAULT_RADIUS);
//            DraggableCircle circle5 = new DraggableCircle(DC, DEFAULT_RADIUS);
//        mCircles.add(circle);

    }



    /*
    var marker = new google.maps.Marker({
  map: map,
  position: new google.maps.LatLng(53, -2.5),
  title: 'Some location'
});

// Add circle overlay and bind to marker
var circle = new google.maps.Circle({
  map: map,
  radius: 16093,    // 10 miles in metres
  fillColor: '#AA0000'
});
circle.bindTo('center', marker, 'position');


     */

    public void onMyLocationToggled(View view) {
        updateMyLocation();
    }

    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateMyLocation() {
        if (!checkReady()) {
            return;
        }

        if (!mMyLocationCheckbox.isChecked()) {
            mMap.setMyLocationEnabled(false);
            return;
        }

        // Enable the location layer. Request the location permission if needed.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Uncheck the box until the layer has been enabled and request missing permission.
            mMyLocationCheckbox.setChecked(false);
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
    }


    @Override
    public void onMapLongClick(LatLng point) {
        // We know the center, let's place the outline at a point 3/4 along the view.
        View view = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getView();
        LatLng radiusLatLng = mMap.getProjection().fromScreenLocation(new Point(
                view.getHeight() * 3 / 4, view.getWidth() * 3 / 4));

        // ok create it
        DraggableCircle circle = new DraggableCircle(point, radiusLatLng);
        mCircles.add(circle);
    }



}