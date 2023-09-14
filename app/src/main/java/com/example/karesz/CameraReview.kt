package com.example.karesz

import MyLocationListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.karesz.data.Datasource
import com.example.karesz.databinding.FragmentCameraReviewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

var latte: Double = 0.0
var longe: Double = 0.0
var isLocationUpdated = false // Boolean változó a helyzetadatok frissülésének jelzésére

class CameraReview() : Fragment() {

    private lateinit var locationManager: LocationManager
    private lateinit var myLocationListener: MyLocationListener

    private lateinit var cameraController: LifecycleCameraController
    private val args: CameraReviewArgs by navArgs()
    private lateinit var binding: FragmentCameraReviewBinding
    private lateinit var outputDirectory: File
    private lateinit var photoFile: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var textCntOfImage: TextView
    private var photoName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraReviewBinding.inflate(inflater, container, false)

        cameraExecutor = Executors.newSingleThreadExecutor()
        val projectjFolderName = args.projectName
        outputDirectory = File(rtkFolder, projectjFolderName)
        textCntOfImage = binding.cntOfImage

        val numberOfImages = Datasource().loadJPGFilesNames(outputDirectory).size
        if (numberOfImages % 2 == 0) {
            textCntOfImage.text =
                "A jelenlegi képek száma: " + String.format("%03d", (numberOfImages / 2))
        } else {
            textCntOfImage.text = (numberOfImages - 1).toString()
            textCntOfImage.text =
                "A jelenlegi képek száma: " + String.format("%03d", ((numberOfImages + 1) / 2))
        }

        val textViewLatitude = binding.textViewLatitude
        val textViewLongitude = binding.textViewLongitude


        textViewLatitude.text = "Szélességi fok: $latte"
        textViewLongitude.text = "Hosszúsági fok: $longe"

       if (mMyLocationOverlay.myLocation != null) {
           myCoordinates = mMyLocationOverlay.myLocation
       }
        startCamera2()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        myLocationListener = MyLocationListener(requireContext())
        myLocationListener.startLocationUpdates()


        // Kép készítése hangerőgombokkal
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keykode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keykode) {
                    KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        photoName = createNewImageName()
                        photoFile = File(outputDirectory, photoName)
                        takePhoto2()
                        Toast.makeText(context, "Kész a kép $photoName", Toast.LENGTH_LONG).show()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        val btn: Button = binding.btnTakePhoto
        btn.setOnClickListener {
            photoName = createNewImageName()
            photoFile = File(outputDirectory, photoName)
            takePhoto2()
            Toast.makeText(context, "Kész a kép $photoName", Toast.LENGTH_LONG).show()
        }
    }


    private fun createNewImageName(): String {
        var lastImage: String = "image.jpg"
        val tmpNewImageName: String

        if (projectFolderIsEmpty(outputDirectory)) {
            return args.projectName + "_001_K.jpg"
        } else {
            val tmpFileList = outputDirectory.listFiles().sorted()
            for (i in tmpFileList.reversed()) {
                lastImage = i.name
                if (lastImage.endsWith(".jpg")) {
                    if (lastImage.contains("_") && lastImage.contains("BP")) {
                        val tmpImageName = lastImage.split("_")
                        var tmpPlaceOfTheImageInTheList = tmpImageName[2].toInt()
                        val closeOrFar: String
                        if (lastImage.contains("_T")) {
                            tmpPlaceOfTheImageInTheList += 1
                            closeOrFar = "_K"
                        } else {
                            closeOrFar = "_T"
                        }
                        val finalNumber = String.format("%03d", tmpPlaceOfTheImageInTheList)
                        tmpNewImageName = "${args.projectName}_$finalNumber$closeOrFar.jpg"
                        textCntOfImage.text = "A jelenlegi képek száma: $finalNumber"
                        return tmpNewImageName
                    }
                }
            }
        }
        return lastImage
    }

    private fun startCamera2() {
        val previewView: PreviewView = binding.viewFinder
        cameraController = LifecycleCameraController(requireContext())
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        previewView.controller = cameraController
    }

    private fun takePhoto2() {
        val photoFile = File(outputDirectory, photoName)

        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        cameraController.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val compressedFile = File(outputDirectory, "compressed_$photoName")
                    compressImage(photoFile, compressedFile)

                    // Meglévő képfájl törlése
                    photoFile.delete()

                    // Ideiglenes fájl átnevezése a meglévő fájl helyére
                    compressedFile.renameTo(photoFile)


                    //  saveGpsCoordinates(photoFile.path, latte, longe)
                    if (latte != 0.0 && longe != 0.0) {
                        saveGpsCoordinates(
                            photoFile.path,
                          /*
                             myCoordinates.latitude,
                             myCoordinates.longitude
                           */
                            latte,
                            longe
                        )
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        myLocationListener.stopLocationUpdates()
        cameraExecutor.shutdown()
    }


    //kép méretének csökkentése
    fun compressImage(sourceFile: File, compressedFile: File) {
        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
        val outputStream = FileOutputStream(compressedFile)
        bitmap.compress(
            Bitmap.CompressFormat.JPEG, 80, outputStream
        ) // 80: tömörítés minősége (0-100)
        outputStream.flush()
        outputStream.close()
    }

    fun projectFolderIsEmpty(directoryFolder: File): Boolean {
        val file = directoryFolder.listFiles()
        return file.isNullOrEmpty()
    }


    fun saveGpsCoordinates(imagePath: String, latitude: Double, longitude: Double) {
        try {
            Log.v(
                "Az eredetii valuek:",
                imagePath + "  " + latitude.toString() + " " + longitude.toString()
            )
            val exifInterface = ExifInterface(imagePath)
            exifInterface.setAttribute(
                ExifInterface.TAG_GPS_LATITUDE,
                formatGpsCoordinates(latitude)
            )
            exifInterface.setAttribute(
                ExifInterface.TAG_GPS_LONGITUDE,
                formatGpsCoordinates(longitude)
            )
            exifInterface.setAttribute(
                ExifInterface.TAG_GPS_LATITUDE_REF,
                if (latitude >= 0) "N" else "S"
            )
            exifInterface.setAttribute(
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                if (longitude >= 0) "E" else "W"
            )
            exifInterface.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun formatGpsCoordinates(coordinates: Double): String {
        val absCoordinates = Math.abs(coordinates)
        val degrees = absCoordinates.toInt()
        val minutesDecimal = (absCoordinates - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val secondsDecimal = (minutesDecimal - minutes) * 60
        val seconds = secondsDecimal.toInt()
        return "$degrees/1,$minutes/1,$seconds/1"
    }

}


/*
    fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
            val location: Location? = task.result
            if (location == null) {
                Toast.makeText(requireContext(), "Null received", Toast.LENGTH_LONG).show()
            } else {
                lat = location.latitude
                lon = location.longitude
            //    val photoFile = File(outputDirectory, photoName)
                saveGpsCoordinates(photoFile.toString(), lat, lon)

            }
        }
    }



    //    locationManager =
      //      (activity as MainActivity).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if ((ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5f, this)

        val photoFile = File(outputDirectory, photoName)
        saveGpsCoordinates(photoFile.toString(), lat, lon)
    }

    override fun onLocationChanged(location: Location) {
        val textViewLatitude = view?.findViewById<TextView>(R.id.textViewLatitude)
        val textViewLongitude = view?.findViewById<TextView>(R.id.textViewLongitude)
        textViewLatitude?.text = location.latitude.toString()
        textViewLongitude?.text = location.longitude.toString()
        lat = location.latitude
        lon = location.longitude
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }


    }
*/