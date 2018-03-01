package com.example.cpu10661.navigationinapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cpu10661.navigationinapp.Utils.PolyUtil;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM_LEVEL = 16;
    private static final int RC_CHOOSE_DEPARTURE = 1;
    private static final int RC_CHOOSE_DESTINATION = 2;
    private static final Uri.Builder mBaseMapsUriBuilder =
            Uri.parse("https://maps.googleapis.com/maps/api/directions/json?").buildUpon();

    private TextView mOriginTextView, mDestinationTextView;

    private SupportMapFragment mMapFragment;
    private Place mOrigin, mDestination;
    private List<Polyline> mPolylines;

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initUiComponents();

        initVolleyComponents();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initUiComponents() {
        // map fragment
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fm_map);
        mMapFragment.getMapAsync(this);

        // place TextViews
        mOriginTextView = findViewById(R.id.tv_origin);
        mOriginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAutoCompleteActivity(RC_CHOOSE_DEPARTURE);
            }
        });
        mDestinationTextView = findViewById(R.id.tv_destination);
        mDestinationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAutoCompleteActivity(RC_CHOOSE_DESTINATION);
            }
        });


    }

    private void launchAutoCompleteActivity(int requestCode) {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, requestCode);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void initVolleyComponents() {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
    }

    @SuppressWarnings("SameParameterValue")
    private void moveMapTo(final LatLng latLng, final int zoom) {
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
        });
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        // for testing on emulator
        LatLng latLng = new LatLng(10.7639435, 106.6540708);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
        showDirection("ChIJM0vYoOwudTERalKdR-Apj7k", "ChIJ3eH0BhwvdTERPZpT1PEAOQQ", "driving");

        map.setOnPolylineClickListener(this);
//        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_CHOOSE_DEPARTURE:
            case RC_CHOOSE_DESTINATION:
                switch (resultCode) {
                    case RESULT_OK:
                        Place place = PlaceAutocomplete.getPlace(this, data);
                        // update with new selection
                        if (requestCode == RC_CHOOSE_DEPARTURE) {
                            mOriginTextView.setText(place.getName());
                            mOrigin = place;
                        } else {
                            mDestinationTextView.setText(place.getName());
                            mDestination = place;
                        }
                        // show new direction
                        if (!mDestinationTextView.getText().toString().equals(getString(R.string.choose_destination))) {
                            moveMapTo(mOrigin.getLatLng(), DEFAULT_ZOOM_LEVEL);
                            showDirection(mOrigin.getId(), mDestination.getId());
                        }
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Status status = PlaceAutocomplete.getStatus(this, data);
                        Log.w(TAG, status.getStatusMessage());
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(this, R.string.user_cancel, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.d(TAG, "onActivityResult - choose location: unexpected result");
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showDirection(String originId, String destinationId) {
        showDirection(originId, destinationId, "driving");
    }

    private void showDirection(String originId, String destinationId, String mode) {
        String url = mBaseMapsUriBuilder
                .appendQueryParameter("origin", "place_id:" + originId)
                .appendQueryParameter("destination", "place_id:" + destinationId)
                .appendQueryParameter("mode", mode)                         // driving, walking, bicycling, transit
                .appendQueryParameter("alternatives", "true")                // alternative routes
                .appendQueryParameter("key", getString(R.string.google_maps_key))
                .toString();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")) {
                                JSONArray routes = response.getJSONArray("routes");
                                drawDirections(routes);
                            } else {
                                String message = response.getString("error_message");
                                if (message == null) {
                                    message = status;
                                }
                                Log.w(TAG, message);
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "onErrorResponse: " + error.getMessage());
                        error.printStackTrace();
                    }
                });
        mRequestQueue.add(jsObjRequest);
    }

    private void drawDirections(JSONArray routes) {
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        try {
            mPolylines = new ArrayList<>();
            for (int legIdx = 0; legIdx < routes.length(); legIdx++) {
                JSONObject firstLeg = routes.getJSONObject(legIdx).getJSONArray("legs").getJSONObject(0);
                JSONArray steps = firstLeg.getJSONArray("steps");
                int polylineColor = legIdx == 0 ? colorPrimary : Color.GRAY;
                for (int stepIdx = 0; stepIdx < steps.length(); stepIdx++) {
                    JSONObject step = steps.getJSONObject(stepIdx);
                    String polyline = step.getJSONObject("polyline").getString("points");
                    List<LatLng> latLngs = PolyUtil.decode(polyline);
                    addPolyline(legIdx, latLngs, polylineColor);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addPolyline(final int legIdx, final List<LatLng> latLngs, int color) {
        final PolylineOptions options = new PolylineOptions()
                .color(color)
                .width(20)                              // TODO: 3/1/18 should be responsive with zoom level
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .zIndex(color != Color.GRAY ? 1 : 0)    // chosen route will be shown on top
                .clickable(true)
                .addAll(latLngs);
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Polyline polyline = googleMap.addPolyline(options);
                polyline.setTag(legIdx);
                mPolylines.add(polyline);
            }
        });
    }

    @Override
    public void onPolylineClick(Polyline clickedPolyline) {
        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int alternativeColor = Color.GRAY;

        int clickedTag = (int) clickedPolyline.getTag();
        for (Polyline polyline : mPolylines) {
            if ((int)polyline.getTag() == clickedTag) {
                polyline.setColor(primaryColor);
                polyline.setZIndex(1);
            } else {
                polyline.setColor(alternativeColor);
                polyline.setZIndex(0);
            }
        }
    }
}
