package com.example.cpu10661.navigationinapp.Utils;

import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.cpu10661.navigationinapp.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by cpu10661 on 3/2/18.
 */

public class DirectionUtil {
    private static final String TAG = DirectionUtil.class.getSimpleName();

    private static final String MAPS_API_KEY = "AIzaSyDxrHjTi9Nak8rdvdyro15aGB6X0XPrxyo";
    private static final Uri mBaseMapsUri =
            Uri.parse("https://maps.googleapis.com/maps/api/directions/json?");
    private static final String DEFAULT_MODE = "driving";

    public static void removeAllPolylines(List<Polyline> polylines) {
        if (polylines != null) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines.clear();
        }
    }

    @NonNull
    public static String getDirectionUrl(@NonNull LatLng originLatLng, @NonNull LatLng destLatLng,
                                         @Nullable String mode) {
        if (mode == null) {
            mode = DEFAULT_MODE;
        }
        String origin = String.format("%s,%s", originLatLng.latitude, originLatLng.longitude);
        String destination = String.format("%s,%s", destLatLng.latitude, destLatLng.longitude);
        return mBaseMapsUri.buildUpon()
                .appendQueryParameter("origin", origin)
                .appendQueryParameter("destination", destination)
                .appendQueryParameter("mode", mode)                     // driving, walking, bicycling, transit
                .appendQueryParameter("alternatives", "true")           // alternative routes
                .appendQueryParameter("key", MAPS_API_KEY)
                .toString();
    }

    @NonNull
    public static PolylineOptions getPolylineOptions(@NonNull List<LatLng> latLngs, int color) {
        return new PolylineOptions()
                .color(color)
                .width(20)                              // TODO: 3/1/18 should be responsive with zoom level
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .clickable(true)
                .addAll(latLngs);
    }

    @NonNull
    public static String getDirectionResponseError(@NonNull JSONObject response) throws JSONException {
        String status = response.getString("status");
        String message = response.getString("error_message");
        if (message == null) {
            message = status;
        }
        Log.w(TAG, message);
        return message;
    }

    private static HashMap<String, Integer> mDirectionIconRes;
    @DrawableRes
    public static int getDirectionIconRes(@NonNull String maneuver) {
        if (mDirectionIconRes == null) {
            mDirectionIconRes = new HashMap<>();
            mDirectionIconRes.put("", R.drawable.direction_turn_straight);
            mDirectionIconRes.put("straight", R.drawable.direction_turn_straight);
            mDirectionIconRes.put("merge", R.drawable.direction_merge_straight);
            mDirectionIconRes.put("place", R.drawable.ic_place_black_24dp);

            mDirectionIconRes.put("turn-left", R.drawable.direction_turn_left);
            mDirectionIconRes.put("turn-slight-left", R.drawable.direction_turn_slight_left);
            mDirectionIconRes.put("turn-sharp-left", R.drawable.direction_turn_sharp_left);
            mDirectionIconRes.put("ramp-left", R.drawable.direction_on_ramp_left);
            mDirectionIconRes.put("fork-left", R.drawable.direction_fork_left);
            mDirectionIconRes.put("keep-left", R.drawable.direction_keep_left);
            mDirectionIconRes.put("uturn_left", R.drawable.direction_uturn);
            mDirectionIconRes.put("roundabout-left", R.drawable.direction_roundabout_left);

            mDirectionIconRes.put("turn-right", R.drawable.direction_turn_right);
            mDirectionIconRes.put("turn-slight-right", R.drawable.direction_turn_slight_right);
            mDirectionIconRes.put("turn-sharp-right", R.drawable.direction_turn_sharp_right);
            mDirectionIconRes.put("ramp-right", R.drawable.direction_on_ramp_right);
            mDirectionIconRes.put("fork-right", R.drawable.direction_fork_right);
            mDirectionIconRes.put("keep-right", R.drawable.direction_keep_right);
            mDirectionIconRes.put("uturn_right", R.drawable.direction_uturn);
            mDirectionIconRes.put("roundabout-right", R.drawable.direction_roundabout_right);
        }

        Integer result = mDirectionIconRes.get(maneuver);
        return result != null ? result : R.drawable.ic_place_black_24dp;
    }
}
