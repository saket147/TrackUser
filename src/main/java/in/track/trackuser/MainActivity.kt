package `in`.track.trackuser

import android.Manifest
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.Location
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.Context


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerRefreshReceiver()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startService()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        }

    }

    var refershReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val loc = intent.getStringExtra("lat") + ", " + intent.getStringExtra("lon")
            findViewById<TextView>(R.id.tv_loc).text = loc
        }
    }

    private fun registerRefreshReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("ACTIONREFRESH")
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(refershReceiver, intentFilter)
    }

    private fun startService() {
        val mServiceIntent = Intent(applicationContext, ServiceNoDelay::class.java)
        startForegroundService(mServiceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService()
            }
        }
    }
}

