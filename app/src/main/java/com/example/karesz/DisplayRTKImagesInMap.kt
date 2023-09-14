package com.example.karesz

import android.media.ExifInterface
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.karesz.data.Datasource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File

class DisplayRTKImagesInMap : Fragment() {
    private val args: DisplayRTKImagesInMapArgs by navArgs()
    private var tmpListLatLng = mutableListOf<LatLng>()
    private lateinit var listLatlng: List<LatLng>
    private var lowestLatitude: Double = 0.0
    private var lowestLongitude: Double = 0.0
    private var highestLatitude: Double = 0.0
    private var highestLongitude: Double = 0.0
    private var lowestLatLng: LatLng = LatLng(0.0,0.0)
    private var highestLatLng: LatLng = LatLng(0.0,0.0)
    private val callback = OnMapReadyCallback { googleMap ->

        val projectName = args.projectName
        val projectFolder = File(rtkFolder, projectName)
        val imageListInProjectFolder = Datasource().loadJPGFiles(projectFolder)
// TÉRKÉPEN A KÉPEK HELYEINEK MEGJELÖLÉSE
        // kilistázzuk a képeket
        imageListInProjectFolder.forEach {
            // az egyes képeknek meghatározzuk a koordinátáit
            val gpsCoordinates = extractGPSFromImage(it.path)

            // ha nem null:
            if (gpsCoordinates != null) {
                // meghatározzuk a szélességi és...
                val latitude = gpsCoordinates.first
                //... hosszúsági értékeket
                val longitude = gpsCoordinates.second
                // a kettőből lesz meg a koordinátánk:
                val latLng = LatLng(latitude, longitude)
                //kialakítunk egy listát, amibe felvesszük az összes koordinátapáros
                tmpListLatLng.add(latLng)
                // majd latitude, illetve longitude szerint rendezzük sorba
                // így megtalálhatjuk a legalacsonyabb és legmagasabb értékeket
                listLatlng = tmpListLatLng.sortedBy { it.longitude }
                lowestLongitude = listLatlng.first().longitude
                highestLongitude = listLatlng.last().longitude
                listLatlng = tmpListLatLng.sortedBy { it.latitude }
                lowestLatitude = listLatlng.first().latitude
                highestLatitude = listLatlng.last().latitude
                // ezekből készítjük el a legdélnyugatibb és legészakkeletibb koordinátákat
                lowestLatLng= LatLng(lowestLatitude,lowestLongitude)
                highestLatLng= LatLng(highestLatitude,highestLongitude)
                // ha nem lennének koordináták akkor Bp lesz
                if (highestLatLng.longitude == 0.0 || lowestLatLng.latitude == 0.0) {
                    highestLatLng = LatLng(47.497912, 19.040235)
                    lowestLatLng = LatLng(47.497912, 19.040235)
                }
                // meghazározzuk a határt:
                val mapBounds = LatLngBounds(lowestLatLng, highestLatLng)
                Log.v("LatLngBounds:", mapBounds.toString())
                // lerakjuk a marker jelet
                googleMap.addMarker(MarkerOptions().position(latLng).title("GPS Pont"))
                // és beállítjuk a középpontot, zoomszintet
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapBounds.center, 15f))

            } else {
                Log.v("Null", "Null értéke van")
            }


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_rtk_images_in_map, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.title = args.projectName
        //beállítások kezelése
        val options = GoogleMapOptions()
            .mapType(GoogleMap.MAP_TYPE_NORMAL)
            .compassEnabled(true)
            .zoomControlsEnabled(true)

        val mapFragment = SupportMapFragment.newInstance(options)
        childFragmentManager.beginTransaction()
            .replace(R.id.map, mapFragment)
            .commit()
        mapFragment.getMapAsync(callback)
    }
}

private fun extractGPSFromImage(imagePath: String?): Pair<Double, Double>? {
    val exifInterface = imagePath?.let { ExifInterface(it) }

    val latLong = exifInterface?.getLatLong()
    if (latLong != null) {
        val latitude = latLong[0].toDouble()
        val longitude = latLong[1].toDouble()
        return Pair(latitude, longitude)
    }
    return null
}

private fun ExifInterface.getLatLong(): FloatArray? {
    val latLong = FloatArray(2)
    return if (getLatLong(latLong)) {
        latLong
    } else {
        null
    }
}
