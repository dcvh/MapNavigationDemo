package com.example.cpu10661.navigationinapp;

/**
 * Created by cpu10661 on 3/2/18.
 */

public class Step {
    private String mDuration;           // TODO: 3/2/18 should save real value instead of human readable String
    private String mDistance;
    private String mInstruction;
    private String mTravelMode;
    private String mManeuver;

    public Step(String duration, String distance, String instruction, String travelMode, String maneuver) {
        mDuration = duration;
        mDistance = distance;
        mInstruction = instruction;
        mTravelMode = travelMode;
        mManeuver = maneuver;
    }

    public String getDuration() {
        return mDuration;
    }

    public String getDistance() {
        return mDistance;
    }

    public String getInstruction() {
        return mInstruction;
    }

    public String getTravelMode() {
        return mTravelMode;
    }

    public String getManeuver() {
        return mManeuver;
    }
}
