package com.example.cpu10661.navigationinapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
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
import com.example.cpu10661.navigationinapp.Utils.DirectionUtil;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM_LEVEL = 16;
    private static final int RC_CHOOSE_DEPARTURE = 1;
    private static final int RC_CHOOSE_DESTINATION = 2;

    private static final String DEFAULT_MODE = "driving";

    private TextView mOriginTextView, mDestinationTextView;
    private BottomSheetBehavior mBottomSheetBehavior;

    private SupportMapFragment mMapFragment;
    private Place mOrigin, mDestination;
    @NonNull
    private List<Polyline> mPolylines = new ArrayList<>();
    private int mPrimaryRouteIdx = 0;

    private int mPrimaryRouteColor;
    private int mAlternativeRoutesColor = Color.GRAY;

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initUiComponents();

        initVolleyComponents();

        mPrimaryRouteColor = ContextCompat.getColor(this, R.color.colorPrimary);
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

        // bottom sheet
        setUpBottomSheet();

        // peek height
        final LinearLayout peekLinearLayout = findViewById(R.id.ll_peek);
        peekLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    peekLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    peekLinearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                int peekHeight = peekLinearLayout.getHeight();
                mBottomSheetBehavior.setPeekHeight(peekHeight);
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

    private void setUpBottomSheet() {
        LinearLayout bottomSheetLinearLayout = findViewById(R.id.ll_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLinearLayout);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initVolleyComponents() {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        // for testing on emulator
//        LatLng latLng = new LatLng(10.7639435, 106.6540708);
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
//        showDirections("ChIJM0vYoOwudTERalKdR-Apj7k", "ChIJ3eH0BhwvdTERPZpT1PEAOQQ");

        map.setOnPolylineClickListener(this);
    }

    @Override
    public void onPolylineClick(Polyline clickedPolyline) {
        mPrimaryRouteIdx = (int) clickedPolyline.getTag();
        showDirections(mOrigin.getId(), mDestination.getId());
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
                            showDirections(mOrigin.getId(), mDestination.getId());
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

    @SuppressWarnings("SameParameterValue")
    private void moveMapTo(final LatLng latLng, final int zoom) {
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
        });
    }

    private void showDirections(String originId, String destinationId) {
        boolean drawNewRoutes = mPolylines.size() == 0;
        showDirections(originId, destinationId, DEFAULT_MODE, drawNewRoutes);
    }

    private void showDirections(String originId, String destinationId, String mode,
                                final boolean drawNewRoutes) {
        String url = DirectionUtil.getDirectionUrl(originId, destinationId, mode);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")) {
                                JSONArray routes = response.getJSONArray("routes");
                                if (drawNewRoutes) {
                                    DirectionUtil.removeAllPolylines(mPolylines);
                                    drawRoutes(routes);
                                } else {
                                    updateRoutes();
                                }
                                populatePrimaryRouteInfo(routes.getJSONObject(mPrimaryRouteIdx));
                            } else {
                                String message = DirectionUtil.getDirectionResponseError(response);
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

        jsObjRequest.setShouldCache(true);
        mRequestQueue.add(jsObjRequest);
    }

    private void drawRoutes(JSONArray routes) {
        try {
            mPolylines.clear();
            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);
                drawRoute(route, i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * add polylines in the specified route
     *
     * @param route the JSONObject containing the route info
     * @param routeIdx  the specified route's index
     *                  this parameter is for determining which polylines to update when click event is fired
     */
    private void drawRoute(JSONObject route, int routeIdx) throws JSONException {
        int color = routeIdx == mPrimaryRouteIdx ? mPrimaryRouteColor : mAlternativeRoutesColor;

        JSONObject leg = route.getJSONArray("legs").getJSONObject(0);   // only one leg for 2 waypoints
        JSONArray steps = leg.getJSONArray("steps");
        for (int stepIdx = 0; stepIdx < steps.length(); stepIdx++) {
            JSONObject step = steps.getJSONObject(stepIdx);
            String polyline = step.getJSONObject("polyline").getString("points");
            List<LatLng> latLngs = PolyUtil.decode(polyline);
            addPolyline(routeIdx, latLngs, color);
        }
    }

    /**
     * add polylines in the specified step
     *
     * @param routeIdx  the specified route's index
     *                  this parameter is for determining which polylines to update when click event is fired
     * @param latLngs latitude and longitude of current step
     * @param color route's color
     */
    private void addPolyline(final int routeIdx, final List<LatLng> latLngs, int color) {
        final PolylineOptions options = DirectionUtil.getPolylineOptions(latLngs, color)
                .zIndex(color == mPrimaryRouteColor ? 1 : 0);           // chosen route will be shown on top
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Polyline polyline = googleMap.addPolyline(options);
                polyline.setTag(routeIdx);
                mPolylines.add(polyline);
            }
        });
    }

    /**
     * highlight the newly chosen route, and revert other routes to normal state
     */
    private void updateRoutes() {
        for (Polyline polyline : mPolylines) {
            if ((int)polyline.getTag() == mPrimaryRouteIdx) {
                polyline.setColor(mPrimaryRouteColor);
                polyline.setZIndex(1);
            } else {
                polyline.setColor(mAlternativeRoutesColor);
                polyline.setZIndex(0);
            }
        }
    }

    /**
     * show current chosen route's info, including summary, duration, distance and detailed instructions
     *
     * @param route the JSONObject containing the route's info
     */
    private void populatePrimaryRouteInfo(JSONObject route) throws JSONException {
        // summary
        TextView summaryTextView = findViewById(R.id.tv_route_summary);
        String summary = route.getString("summary");
        summaryTextView.setText(summary);

        // duration + distance
        JSONObject leg = route.getJSONArray("legs").getJSONObject(0);   // only one leg for 2 waypoints
        String duration = leg.getJSONObject("duration").getString("text");
        String distance = leg.getJSONObject("distance").getString("text");
        String info = duration != null ?
                String.format("%s (%s)", duration, distance) :
                getString(R.string.unknown);
        TextView durationDistanceTextView = findViewById(R.id.tv_duration_distance);
        durationDistanceTextView.setText(info);

        // steps
        JSONArray stepsJsonArray = leg.getJSONArray("steps");
        List<Step> steps = new ArrayList<>(stepsJsonArray.length());
        for (int i = 0; i < stepsJsonArray.length(); i++) {
            JSONObject stepObj = stepsJsonArray.getJSONObject(i);
            duration = stepObj.getJSONObject("duration").getString("text");
            distance = stepObj.getJSONObject("distance").getString("text");
            String instruction = stepObj.getString("html_instructions");
            String travelMode = stepObj.getString("travel_mode");
            String maneuver = !stepObj.isNull("maneuver") ? stepObj.getString("maneuver") : "";
            Step step = new Step(duration, distance, instruction, travelMode, maneuver);
            steps.add(step);
        }

        // steps list view
        RecyclerView stepsListView = findViewById(R.id.rv_steps);
        stepsListView.setLayoutManager(new LinearLayoutManager(this));
        StepListAdapter adapter = new StepListAdapter(steps);
        stepsListView.setAdapter(adapter);
    }
}
