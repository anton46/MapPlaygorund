package com.test.app.ui.maps

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.test.app.R
import com.test.app.di.HasApplicationComponent
import com.test.app.di.PlaygroundApplicationComponent
import com.test.app.net.data.request.SearchInfo
import com.test.app.net.data.response.Venue
import com.test.app.ui.base.BaseActivity
import com.test.app.ui.maps.MapsViewModel.ViewState
import com.test.app.ui.maps.di.MapsActivityComponent
import com.test.app.ui.maps.di.MapsActivityModule
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class MapsActivity : BaseActivity(), OnMapReadyCallback, HasApplicationComponent {

    private var currentLocation: Location? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var googleMap: GoogleMap? = null
    private val compositeDisposable = CompositeDisposable()
    private val addedRestaurant = mutableSetOf<Venue>()
    private var selectedMarker: Marker? = null
    private var loadingMenu: Menu? = null
    private var loadingMenuView: View? = null

    private companion object {
        const val REQUEST_CODE = 101
    }

    private lateinit var activityComponent: MapsActivityComponent

    override fun inject() {
        activityComponent =
            (getApplicationComponent() as PlaygroundApplicationComponent).add(MapsActivityModule())
        activityComponent.inject(this)
    }

    override fun getActivityComponent() = activityComponent

    override fun getApplicationComponent() =
        (application as HasApplicationComponent).getApplicationComponent()

    private lateinit var viewModel: MapsViewModel

    @Inject
    lateinit var mapsViewModelProvider: MapsViewModelProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        provideViewModel()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menus, menu)
        loadingMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        observeStates()
    }

    private fun provideViewModel() {
        viewModel = mapsViewModelProvider.provideViewModel(this)
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun observeStates() {
        compositeDisposable.add(
            viewModel
                .observeStates()
                .subscribe { render(it) }
        )
    }

    private fun render(state: ViewState) {
        when (state) {
            is ViewState.RestaurantLoaded -> onRestaurantLoaded(state.restaurants)
            is ViewState.Loading -> showLoading()
            is ViewState.Error -> onError()
        }
    }

    private fun onRestaurantLoaded(restaurants: Set<Venue>) {
        hideLoading()
        renderRestaurant(restaurants)
    }

    private fun onError() {
        hideLoading()
        Toast.makeText(
            this,
            "Something went wrong",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading() {
        if (loadingMenuView == null) {
            loadingMenuView = View.inflate(this, R.layout.loading_menu, null)
        }
        loadingMenu?.getItem(0)?.actionView = loadingMenuView
    }

    private fun hideLoading() {
        loadingMenu?.getItem(0)?.actionView = null
    }

    private fun renderRestaurant(restaurants: Set<Venue>) {
        googleMap?.let { map ->
            restaurants.forEach { restaurant ->
                if (addedRestaurant.add(restaurant)) {
                    val latLng = LatLng(restaurant.location.lat, restaurant.location.lng)
                    val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title(restaurant.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    val marker = map.addMarker(markerOptions)
                    marker.tag = restaurant
                }
            }
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
            return
        }
        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener { location ->
            location?.let {
                currentLocation = location
                val supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                supportMapFragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true

        currentLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        }

        googleMap.setOnCameraIdleListener {
            loadRestaurants(googleMap.projection.visibleRegion.latLngBounds)
        }
        googleMap.setOnMarkerClickListener { marker ->
            showRestaurantDetailDialog(marker.tag as Venue)
            selectedMarker = marker
            updateMarkerIcon(true)
            true
        }
    }

    private fun loadRestaurants(viewBounds: LatLngBounds) {
        googleMap?.clear()
        addedRestaurant.clear()

        viewModel.loadRestaurants(
            viewBounds.northeast.toCoordinate(),
            viewBounds.southwest.toCoordinate()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when {
            requestCode == REQUEST_CODE
                    && grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                fetchLocation()
            }
        }
    }

    private fun updateMarkerIcon(selected: Boolean) {
        selectedMarker?.let { marker ->
            if (selected) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            } else {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            }
        }
    }

    private fun LatLng.toCoordinate() = SearchInfo.Coordinate(this.latitude, this.longitude)

    private fun showRestaurantDetailDialog(venue: Venue) {
        val view = layoutInflater.inflate(R.layout.dialog_restaurant_detail, null)
        val restaurantName = view.findViewById<TextView>(R.id.restaurant_name)
        val restaurantAddress = view.findViewById<TextView>(R.id.restaurant_address)
        restaurantName.text = venue.name
        restaurantAddress.text = venue.location.formattedAddress.joinToString("\n")

        val dialog = BottomSheetDialog(this)
        dialog.setOnDismissListener {
            updateMarkerIcon(false)
            selectedMarker = null
        }
        dialog.setContentView(view)
        dialog.show()
    }
}
