package com.smartrg.smartrgapp.Classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;

public class HeatMap {

    private String createdDate;
    private ArrayList<TestPoint> testPointList;
    private ArrayList<RouterPoint> routerPointList;
    private int testPinCount;
    private int routerPinCount;

    public HeatMap() {
        testPointList = new ArrayList<>();
        routerPointList = new ArrayList<>();
        createdDate = "2/25/2017";
        testPinCount = 0;
        routerPinCount = 0;
    }

    public HeatMap(String date) {
        testPointList = new ArrayList<>();
        routerPointList = new ArrayList<>();
        createdDate = date;
        testPinCount = 0;
        routerPinCount = 0;
    }

    public HeatMap(ArrayList<TestPoint> testList, ArrayList<RouterPoint> routerList, String date) {
        testPointList = testList;
        routerPointList = routerList;
        createdDate = date;
    }

    public void addTestPin(TestPoint testPoint) {
        testPointList.add(testPoint);
        testPinCount++;
    }

    public void addRouterPin(RouterPoint routerPoint) {
        routerPointList.add(routerPoint);
        routerPinCount++;
    }

    public void removeTestPin(TestPoint testPoint) {
        routerPointList.remove(testPoint);
        testPinCount--;
    }

    public void removeRouterPin(RouterPoint routerPoint) {
        routerPointList.remove(routerPoint);
        routerPinCount--;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public ArrayList<TestPoint> getTestPointList() {
        return testPointList;
    }

    public ArrayList<RouterPoint> getRouterPointList() {
        return routerPointList;
    }

    public ArrayList<WeightedLatLng> createWeightedList() {
        ArrayList<WeightedLatLng> weightedList = new ArrayList<>();
        for (int j = 0; j < routerPointList.size(); j++) {
            weightedList.add(new WeightedLatLng(new LatLng(routerPointList.get(j).getLatitude(), routerPointList.get(j).getLongitude()), 1.0));
        }
        for (int i = 0; i < testPointList.size(); i++) {
            weightedList.add(new WeightedLatLng(new LatLng(testPointList.get(i).getLatitude(), testPointList.get(i).getLongitude()),testPointList.get(i).getIntensity()));
        }
        return weightedList;
    }

}