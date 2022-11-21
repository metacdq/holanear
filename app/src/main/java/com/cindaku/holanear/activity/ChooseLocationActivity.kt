package com.cindaku.holanear.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.cindaku.holanear.APP_NAME
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.api.PlaceAPIService
import com.cindaku.holanear.databinding.ActivityChooseLocationBinding
import com.cindaku.holanear.model.Place
import com.cindaku.holanear.ui.adapter.LocationListAdapter
import com.cindaku.holanear.ui.inf.OnChooseLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ChooseLocationActivity : AppCompatActivity(), OnMapReadyCallback,OnChooseLocation {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityChooseLocationBinding
    private val MAP="map"
    lateinit var toolbar: Toolbar
    lateinit var back: ImageView
    lateinit var myLocation: Button
    private var lastLocation: Location?=null
    private var choosedLocation: com.cindaku.holanear.model.Location=com.cindaku.holanear.model.Location()
    lateinit var listLocation: RecyclerView
    lateinit var listLocatioAdapter: LocationListAdapter
    private val permissionList= arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var API_KEY: String
    @Inject
    lateinit var placeAPI: PlaceAPIService
    private lateinit var lastMarker: ArrayList<Place>
    private var lastPlace: Place?=null
    private var isMyLocation=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as BaseApp).appComponent.inject(this)
        setContentView(R.layout.activity_choose_location)
        toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        back=toolbar.findViewById(R.id.backImageView)
        myLocation=findViewById(R.id.buttonMyLocation)
        listLocation=findViewById(R.id.listLocation)
        listLocation.layoutManager=LinearLayoutManager(this)
        listLocatioAdapter= LocationListAdapter(baseContext)
        listLocation.adapter=listLocatioAdapter
        listLocatioAdapter.setOnChoose(this)
        binding = ActivityChooseLocationBinding.inflate(layoutInflater)
        back.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        requestPermissions(permissionList,1001)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        myLocation.setOnClickListener {
            getMyLocation()
        }
        API_KEY=resources.getString(R.string.API_KEY)
        val transaction=supportFragmentManager.beginTransaction()
        val fragment=SupportMapFragment.newInstance()
        transaction.replace(R.id.map,fragment,MAP)
        transaction.commit()
        fragment.getMapAsync(this)
        checkLocationOn()
    }
    fun checkLocationOn() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            // notify user
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setCancelable(false)
            dialog.setMessage("Please turn on location")
            dialog.setPositiveButton("Turn On"
            ) { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            dialog.setNegativeButton("Cancel"
            ) { paramDialogInterface, paramInt ->
                paramDialogInterface.dismiss()
                finish()
            }
            dialog.show()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = false
        mMap.uiSettings.isScrollGesturesEnabled = false
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.setOnMarkerClickListener {
            true
        }
        getMyLocation()
    }
    fun getMyLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    lifecycleScope.launch {
                        lastLocation=it
                        choosedLocation=com.cindaku.holanear.model.Location()
                        choosedLocation!!.lat=it.latitude
                        choosedLocation!!.lng=it.longitude
                        choosedLocation!!.description=""
                        val bias=""+it.latitude+","+it.longitude
                        val response=placeAPI.nearby(50,bias,"",true,API_KEY).execute()
                        if(response.isSuccessful){
                            response.body()?.run{
                                listLocatioAdapter.setData(this.results)
                                reDrawMarker(this.results,false,false)
                            }
                        }else{
                            response.errorBody()?.run {
                                Log.e("NEARBY",this.string())
                            }
                        }
                        myLocation.setBackgroundColor(resources.getColor(R.color.primaryTrans,null))
                    }
                }
            }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    fun reDrawMarker(arrayList: ArrayList<Place>, isOnChoose: Boolean,selectedOnly: Boolean){
        val circleDrawable = resources.getDrawable(R.drawable.circle_marker,null)
        val circleDrawableSelected = resources.getDrawable(R.drawable.circle_marker_selected,null)
        val markerIcon = getMarkerIconFromDrawable(circleDrawable)
        val markerIconSelected = getMarkerIconFromDrawable(circleDrawableSelected)
        mMap.clear()
        val myLocationMarker = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
        val myLocationOptions=MarkerOptions().position(myLocationMarker).title("My Location")
        mMap.setMinZoomPreference(18f)
        arrayList.forEach {
            val placeMarker = LatLng(it.geometry!!.location!!.lat,it.geometry!!.location!!.lng)
            val placeOptions=MarkerOptions().position(placeMarker)
                .title(it.name)
            if(lastPlace!=null && it==lastPlace){
                placeOptions.icon(markerIconSelected)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(placeMarker))
                mMap.addMarker(placeOptions)
            }else if(!selectedOnly){
                placeOptions.icon(markerIcon)
                mMap.addMarker(placeOptions)
            }
        }
        if(!isOnChoose){
            myLocationOptions.icon(markerIconSelected)
            mMap.addMarker(myLocationOptions)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationMarker))
        }else if(!selectedOnly){
            myLocationOptions.icon(markerIcon)
            mMap.addMarker(myLocationOptions)
        }
        lastMarker=arrayList
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1001){
            if(permissions.size!=grantResults.size){
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.location, menu)
        menu?.let {
            val search=it.findItem(R.id.search)
            val send=it.findItem(R.id.done)
            val searchView= search.actionView as SearchView
            search.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    send.isVisible=false
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    send.isVisible=true
                    invalidateOptionsMenu()
                    return true
                }

            })
            searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener,
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    p0?.let {
                        val querySearch=it
                        lastLocation?.also {
                            lifecycleScope.launch {
                                val bias=""+it.latitude+","+it.longitude
                                val response=placeAPI.nearby(50,bias,querySearch,true,API_KEY).execute()
                                if(response.isSuccessful){
                                    response.body()?.run{
                                        listLocatioAdapter.setData(this.results)
                                        reDrawMarker(this.results,false,false)
                                    }
                                }else{
                                    response.errorBody()?.run {
                                        Log.e("NEARBY",this.string())
                                    }
                                }
                            }
                        }
                    }
                    search.collapseActionView()
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    return true
                }

            })
        }
        return true
    }

    @SuppressLint("SimpleDateFormat")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.done){
            reDrawMarker(lastMarker,true,true)
            mMap.snapshot {
                it?.run {
                    val bitmap=this
                    lifecycleScope.launch(Dispatchers.IO) {
                        choosedLocation.let {
                            getExternalFilesDir(null)?.let {
                                try {
                                    val dir=File(it.absolutePath+"/"+
                                            APP_NAME +"/Data")
                                    if(!dir.exists()){
                                        dir.mkdirs()
                                    }
                                    val format= SimpleDateFormat("yyyyMMddHHmmss")
                                    val filename=format.format(Calendar.getInstance().time)+"_location"
                                    val file = File(dir.absolutePath+"/"+filename)
                                    val bos = ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 70, bos);
                                    val bitmapdata = bos.toByteArray();
                                    val fos = FileOutputStream(file);
                                    fos.write(bitmapdata);
                                    fos.flush();
                                    fos.close()
                                    val i=Intent()
                                    i.putExtra("image",file.path)
                                    i.putExtra("location",choosedLocation)
                                    setResult(RESULT_OK,i)
                                    finish()
                                }catch (e: Exception){
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onChoose(place: Place) {
        myLocation.setBackgroundColor(resources.getColor(android.R.color.transparent,null))
        place.let {
            lastPlace=it
            choosedLocation= com.cindaku.holanear.model.Location()
            choosedLocation.lat=it.geometry!!.location!!.lat
            choosedLocation.lng=it.geometry!!.location!!.lng
            choosedLocation.description=it.name+"\n"+it.formatted_address
            reDrawMarker(this.lastMarker,true,false)
        }
    }
}