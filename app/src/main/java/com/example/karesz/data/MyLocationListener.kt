import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import com.example.karesz.*

class MyLocationListener(private val context: Context) : LocationListener {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1L,
                0.0001f,
                this
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        // Helyzet frissítése történt, itt kezelheted a kapott helyzetet
        latte = location.latitude
        longe = location.longitude
        val tvLatte = (context as MainActivity).findViewById<TextView>(R.id.textViewLatitude)
        val tvLonge = (context).findViewById<TextView>(R.id.textViewLongitude)
        if (tvLatte != null && tvLonge != null) {
            tvLatte.text = "Szélességi fok: $latte"
            tvLonge.text = "Hosszúsági fok: $longe"
            isLocationUpdated = true // Beállítod a helyzetadatok frissült jelzőjét
        }
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}
