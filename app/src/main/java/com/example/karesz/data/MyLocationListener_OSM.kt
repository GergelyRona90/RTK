package com.example.karesz.data

import android.location.Location
import android.util.Log
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider


class MyLocationConsumer : IMyLocationConsumer{
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        if (location != null){
            Log.v("OSM Lokáció:", location.longitude.toString())
            Log.v("OSM Lokáció:", location.latitude.toString())
        }
    }

}