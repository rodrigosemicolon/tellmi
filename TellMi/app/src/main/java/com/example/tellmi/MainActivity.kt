package com.example.tellmi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.altbeacon.beacon.*
import org.altbeacon.beacon.BeaconParser.EDDYSTONE_URL_LAYOUT
import org.altbeacon.beacon.service.ArmaRssiFilter
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor


class MainActivity : AppCompatActivity(), BeaconConsumer {

    val TAG = "BeaconTest"
    private var beaconManager: BeaconManager? = null
    val ip = "192.168.99.94:5000/"
    lateinit var age_group: String
    lateinit var lan: String
    var last_url="None"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val age: String= intent.getStringExtra("age").toString()
        lan= intent.getStringExtra("lan").toString()
        if(age.toInt() > 16){
            age_group="adult"
        }
        else{
            age_group="young"
        }
        Log.d("extratest", "age:" + age + " lan: " + lan)
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, 0)
        requestPermissions()
        val btAudio: Button = this.findViewById(R.id.btAudio)


        this.beaconManager = BeaconManager.getInstanceForApplication(this)

        this.beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(EDDYSTONE_URL_LAYOUT))
        //this.beaconManager.setRssiFilterImplClass(
        //    ArmaRssiFilter.class)
        this.beaconManager!!.bind(this)

    }


    override fun onDestroy() {
        super.onDestroy()
        this.beaconManager!!.unbind(this)
    }


    fun hasFineLocationPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasCoarseLocationPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasBackgroundLocationPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermissions() {
        var permissionsToRequest = mutableListOf<String>()
        if (!hasFineLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasBackgroundLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (!hasCoarseLocationPermission()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices)
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermissionsRequest", "${permissions[i]} granted.")
                }
        }
    }


    override fun onBeaconServiceConnect() {


        this.beaconManager!!.removeAllRangeNotifiers();
        var tvBeaconList: TextView = this.findViewById(R.id.tvBeaconList) as TextView
        this.beaconManager!!.addRangeNotifier { beacons, region ->
            var min_dist: Float=1f
            var closest_beacon: Beacon?=null

            for(beacon in beacons){
                if (beacon.serviceUuid == 0xfeaa && beacon.beaconTypeCode == 0x10) {

                    if(beacon.distance<min_dist){

                        min_dist= beacon.distance.toFloat()
                        closest_beacon=beacon
                    }
                }
            }
            if(closest_beacon!=null){
                var url = UrlBeaconUrlCompressor.uncompress(closest_beacon.id1.toByteArray());
                Log.i(TAG,
                    "The first beacon I see has url: " + url + " and is about " + closest_beacon.distance +" meters away."
                )
                tvBeaconList.text="The first beacon I see has url: " + url + " and is about " + closest_beacon.distance +" meters away."
                try {
                    val final_url =
                        "http://" + ip +  url + lan + "/" + age_group // your URL here
                    Log.d("getting_url", final_url)
                    if(final_url!=last_url){

                        last_url=final_url
                        val mediaPlayer = MediaPlayer()
                        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                        mediaPlayer.setDataSource(this,Uri.parse(final_url))
                        mediaPlayer.prepareAsync() // might take long! (for buffering, etc)
                        mediaPlayer.setOnPreparedListener(OnPreparedListener { //mp.start();
                            mediaPlayer.start()
                        })
                    }
                    //mediaPlayer.start()


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else{
                tvBeaconList.text=""
            }

        };

        try {
            this.beaconManager!!.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null));
        } catch (e: RemoteException ) {    }
    }



}

