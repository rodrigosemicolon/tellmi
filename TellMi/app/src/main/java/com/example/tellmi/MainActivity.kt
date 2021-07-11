package com.example.tellmi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.RemoteException
import android.text.format.Formatter
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.altbeacon.beacon.*
import org.altbeacon.beacon.BeaconParser.EDDYSTONE_URL_LAYOUT
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), BeaconConsumer {

    val TAG = "BeaconTest"
    val minRange = 1f
    private var beaconManager: BeaconManager? = null
    val ip = "rdrgprt:5000/" //my hostname, change this to the hostname or ip where the server is running from
    lateinit var age_group: String
    lateinit var lan: String
    var last_url="None"
    var alreadyPlayed: ArrayList<String> = ArrayList()
    var scanning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        Log.d("iptest",ipAddress)
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

        val tvBeaconList: TextView = this.findViewById(R.id.tvBeaconList) as TextView
        btAudio.setOnClickListener {
            if(last_url!="None")
                play_audio(last_url)
            else
                Toast.makeText(this,"No explanation available yet!", Toast.LENGTH_SHORT).show()
        }

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
            var min_dist: Float=minRange
            var closest_beacon: Beacon?=null

            for(beacon in beacons){

                if (beacon.serviceUuid == 0xfeaa && beacon.beaconTypeCode == 0x10) {
                    var url = UrlBeaconUrlCompressor.uncompress(beacon.id1.toByteArray());
                    if(beacon.distance<min_dist && !(url in alreadyPlayed)){

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

                try {
                    val final_url =
                        "http://" + ip +  url + lan + "/" + age_group // your URL here
                    Log.d("getting_url", final_url)
                    if(!(url in alreadyPlayed)){
                        alreadyPlayed.add(url)
                        last_url=final_url
                        play_audio(final_url)
                        tvBeaconList.text = final_url

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

fun play_audio(url: String){
    val mediaPlayer = MediaPlayer()
    //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
    mediaPlayer.setDataSource(this,Uri.parse(url))
    mediaPlayer.prepareAsync() // might take long! (for buffering, etc)
    mediaPlayer.setOnPreparedListener(OnPreparedListener { //mp.start();
        mediaPlayer.start()
    })
}

}

