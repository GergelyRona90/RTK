package com.example.karesz

import MyLocationListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.karesz.Constants.MEDIA_PERMISSION
import com.example.karesz.Constants.REQUEST_CAMERACODE_PERMISSIONS
import com.example.karesz.databinding.ActivityMainBinding
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@SuppressLint("StaticFieldLeak")
private lateinit var navController: NavController

lateinit var osmMap: MapView
lateinit var mMyLocationOverlay: MyLocationNewOverlay
lateinit var myCoordinates: GeoPoint



// csak a download mappán belül tudok létrehozni mappát, így itt lesz az RTK mappa
val downloadDirectory: File =
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

val rtkFolder = File(downloadDirectory, "RTK")
val deletedImageFolder = File(downloadDirectory, "DeletedImages")


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationListener: MyLocationListener

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkFolderIsExist(rtkFolder)
        checkFolderIsExist(deletedImageFolder)

        val osmView = layoutInflater.inflate(R.layout.fragment_osm_map, null)
        osmMap = osmView.findViewById(R.id.osmmap)
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), osmMap)
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.myLocationProvider.startLocationProvider(mMyLocationOverlay)
     //   mMyLocationOverlay.lastFix

        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true



        val permissionList =
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE

            )
        locationListener =
            MyLocationListener(this) // Átadod a MainActivity objektumot a MyLocationListener konstruktorának
        val requestCodeList = arrayOf(
            MEDIA_PERMISSION,
            REQUEST_CAMERACODE_PERMISSIONS,
        )
        // minden szükséges engedélyt engedélyeztetünk
        val permissionLauncherMultiple = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var areAllGranted = true
            Log.v("Engedélyek", result.toString())
            for (isGranted in result.values) {
                areAllGranted = areAllGranted && isGranted
            }
        }
        permissionLauncherMultiple.launch(permissionList)
        //  checkPermissions(permissionList, requestCodeList, permissionNames)


        val navHostFragments =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragments.navController
        setupActionBarWithNavController(navController)


    }

    //Ha nincs engedélyezve a permissionok akkor engedélyeztetjük
    fun checkPermissions(
        permissions: Array<String>,
        requestCodes: Array<Int>,
        permissionsNames: Array<String>
    ) {
        for (i in permissions.indices) {
            val permission = permissions[i]
            val requestCode = requestCodes[i]
            val permissionName = permissionsNames[i]

            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_DENIED

            ) {
                Toast.makeText(this, "$permissionName nincs engedélyezve", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun checkFolderIsExist(f: File) {
    // mappa létezik-e, ha nem akkor létrehozzuk
    if (!f.exists()) {
        //f.mkdir()
        Files.createDirectory(Paths.get(f.absolutePath))
    }

}