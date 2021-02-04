package com.template.directionsapikotlinsample

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng as MapsLatLng


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val ZOOM_SIZE = 14f
        private const val POLYLINE_WIDTH = 12f
    }

    private var mMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private val overview = 0
    private val viewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.directionsResult.observe(this, Observer {
            Log.d(MapsActivity::class.java.simpleName, "result: $it")
            updatePolyline(it, mMap)
            // カメラ移動.
            moveCamera()
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val fromTokyo = MapsLatLng(35.68183, 139.76715)
        val toKanda = MapsLatLng(35.69274, 139.77114)
        viewModel.execute(fromTokyo, toKanda)
    }

    private fun moveCamera() {
        // Add a marker in Sydney and move the camera
        val tokyo = LatLng(35.68183, 139.76715)
        mMap?.apply {
            addMarker(MarkerOptions().position(tokyo).title("Marker in Tokyo"))
            // moveCamera(CameraUpdateFactory.newLatLng(tokyo))
            moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, ZOOM_SIZE))
        }
    }

    private fun updatePolyline(directionsResult: DirectionsResult?, googleMap: GoogleMap?) {
        googleMap ?: return
        directionsResult ?: return
        removePolyline()
        addPolyline(directionsResult, googleMap)
    }

    // 線を消す.
    private fun removePolyline() {
        if (mMap != null && polyline != null) {
            polyline?.remove()
        }
    }

    // 線を引く
    private fun addPolyline(directionsResult: DirectionsResult, map: GoogleMap) {
        val polylineOptions = PolylineOptions()
        polylineOptions.width(POLYLINE_WIDTH)
        // ARGB32bit形式.
        val colorPrimary = ContextCompat.getColor(this, R.color.map_polyline_stroke)
        polylineOptions.color(colorPrimary)
        val decodedPath = PolyUtil.decode(directionsResult.routes[overview].overviewPolyline.encodedPath)
        polyline = map.addPolyline(polylineOptions.addAll(decodedPath))
    }

    // 線を引く
    private fun addPolyline2(directionsResult: DirectionsResult, map: GoogleMap) {
        val bounds = LatLngBounds.builder()
        val route = directionsResult.routes[0]
        val polylineOptions = PolylineOptions()
        for (latLng in route.overviewPolyline.decodePath()) {
            polylineOptions.add(LatLng(latLng.lat, latLng.lng))
            bounds.include(LatLng(latLng.lat, latLng.lng))
        }
        polylineOptions.width(POLYLINE_WIDTH)
        val colorPrimary = ContextCompat.getColor(this, R.color.map_polyline_stroke)
        polylineOptions.color(colorPrimary)
        polyline = mMap?.addPolyline(polylineOptions)
    }
}