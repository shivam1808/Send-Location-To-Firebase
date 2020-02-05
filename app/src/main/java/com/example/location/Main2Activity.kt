package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import android.location.LocationListener
import android.location.LocationManager



class Main2Activity : AppCompatActivity(), LocationListener {


    override fun onLocationChanged(location: Location?) {
        val addLiveLocation = FirebaseDatabase.getInstance().reference.child("liveLocations")
        addLiveLocation.child("lat").setValue(location?.getLatitude())
        addLiveLocation.child("lang")
            .setValue(location?.getLongitude())
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101
    private var mLocationPermissionGranted = false
    private val TAG = "HomeFragment"
    private var mLastKnownLocation: Location? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        getLocationPermission()


        val manager =
            Objects.requireNonNull(this).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager != null) {
            val providers = manager.allProviders
            for (provider in providers) {
                if (mLocationPermissionGranted)
                    manager.requestLocationUpdates(provider, 1, 3f, this)
            }
        }
    }


    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                Objects.requireNonNull(this).applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionGranted = true
                getDeviceLocation()
            }
        }
    }

    fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                    Objects.requireNonNull(this)
                )
                val locationResult = mFusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener {task->
                    if (task.isSuccessful) {

                        mLastKnownLocation = task.result as Location?

                        Log.i(
                            TAG,
                            mLastKnownLocation?.getLatitude().toString() + "  " + mLastKnownLocation?.getLongitude()
                        )

                        val addLiveLocation = FirebaseDatabase.getInstance().reference
                            .child("liveLocations")
                        addLiveLocation.child("lat").setValue(mLastKnownLocation?.getLatitude())
                        addLiveLocation.child("lang")
                            .setValue(mLastKnownLocation?.getLongitude())
                    }
                }
            } else {
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", Objects.requireNonNull(e.message))
        }

    }
}
