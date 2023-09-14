package com.example.karesz

import android.graphics.Rect
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.karesz.data.Datasource
import com.example.karesz.databinding.FragmentOsmMapBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.shape.ShapeConverter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File


class OSMMap : Fragment(), MapListener {
    private val args: OSMMapArgs by navArgs()
    lateinit var mMap: MapView
    lateinit var controller: IMapController
    lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var binding: FragmentOsmMapBinding
    private var tmpListLatLng = mutableListOf<Pair<Double, Double>>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOsmMapBinding.inflate(inflater, container, false)
        Configuration.getInstance().load(
            requireActivity().applicationContext,
            requireActivity().getSharedPreferences(
                getString(R.string.app_name),
                AppCompatActivity.MODE_PRIVATE
            )
        )
        val projectName = args.projectName



        (activity as MainActivity).supportActionBar?.title = projectName
        val projectFolder = File(rtkFolder, projectName)
        val imageListInProjectFolder = Datasource().loadJPGFiles(projectFolder)
        val imageNameListInProjectFolder = Datasource().loadJPGFilesNames(projectFolder)
        var imageNameCounter: Int = 0
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        //mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        imageListInProjectFolder.forEach {

            val gpsCoordinates = extractGPSFromImage(it.path)
            if (gpsCoordinates != null) {


                // meghatározzuk a szélességi és...
                val latitude = gpsCoordinates.first
                //... hosszúsági értékeket
                val longitude = gpsCoordinates.second
                // a kettőből lesz meg a koordinátánk:
                var point = GeoPoint(latitude, longitude)
                val latLng: Pair<Double, Double> = Pair(latitude, longitude)
                //kialakítunk egy listát, amibe felvesszük az összes koordinátapáros
                tmpListLatLng.add(latLng)
                val marker = Marker(mMap)
                marker.position = point
                marker.title = imageNameListInProjectFolder[imageNameCounter]
                imageNameCounter++
                mMap.overlays.add(marker)
            }
        }
        if (File(projectFolder, "SHP").exists()){
            val shpFolder = File(projectFolder, "SHP")
            val folder: List<Overlay> = ShapeConverter.convert(mMap, projectFolder)
            mMap.overlayManager.addAll(folder)
            mMap.invalidate()
        }
/*
        val boundingBox: BoundingBox =
            BoundingBox(highestLatitude, highestLongitude, lowestLatitude, lowestLongitude)
        mMap.setScrollableAreaLimitDouble(boundingBox)
 */

        controller = mMap.controller
        controller.setZoom(16.0)
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mMap)
        mMyLocationOverlay.enableMyLocation()
//        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        /*    mMyLocationOverlay.runOnFirstFix {
                activity?.runOnUiThread {
                    controller.animateTo(budapestCenter)
                }
            }
         */
        controller.animateTo(mediumGPSPoint(tmpListLatLng))

        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)

        return binding.root
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        // event?.source?.getMapCenter()
        Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        //  Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}", )
        mMap.resetScrollableAreaLimitLatitude()
        mMap.resetScrollableAreaLimitLongitude()
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        //  event?.zoomLevel?.let { controller.setZoom(it) }
        mMap.resetScrollableAreaLimitLatitude()
        mMap.resetScrollableAreaLimitLongitude()

        Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
        return false;
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

    override fun onResume() {
        super.onResume()
    }

    fun mediumGPSPoint(tmpList: List<Pair<Double, Double>>): GeoPoint {
        var orderedList: List<Pair<Double, Double>>
        orderedList = tmpList.sortedBy { it.first }
        val lowestLatitude: Double = orderedList.first().first
        val highestLatitude: Double = orderedList.last().first
        orderedList = tmpList.sortedBy { it.second }
        val lowestLongitude: Double = orderedList.first().second
        val highestLongitude: Double = orderedList.last().second


        return if (lowestLatitude == 0.0 || highestLongitude == 0.0) {
            GeoPoint(47.497912, 19.040235)
        } else {
            GeoPoint(
                (lowestLatitude + highestLatitude) / 2,
                (lowestLongitude + highestLongitude) / 2
            )
        }

    }
}
