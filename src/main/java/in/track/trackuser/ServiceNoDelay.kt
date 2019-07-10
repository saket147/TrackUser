package `in`.track.trackuser

import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.os.SystemClock
import android.content.Context
import android.R
import android.app.*
import android.app.Notification.PRIORITY_MIN
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ServiceNoDelay : Service(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mLocationCallback: LocationCallback
    private var location: Location? = null;
    private val INTERVAL = (1000 * 10).toLong()
    private val FASTEST_INTERVAL = (1000 * 10).toLong()
    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null

    private fun startForeground() {
        val channelId = createNotificationChannel("my_service", "My Background Service")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle("Location Update")
            .setContentText(location?.latitude.toString() + ", " + location?.longitude)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        if(location != null) {
            startForeground(101, notification)
        }
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (isGooglePlayServicesAvailable()) {
            initGoogleApiClient()
            createLocationCallback()
            startLocationUpdates()
        }
        return START_STICKY
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val alarmService =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val status = GoogleApiAvailability.getInstance()
        return if (ConnectionResult.SUCCESS == status.isGooglePlayServicesAvailable(this)) {
            true
        } else {
            status.getErrorDialog(
                applicationContext as Activity?,
                status.isGooglePlayServicesAvailable(this),
                0
            ).show()
            false
        }
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (p0 != null) {
                    location = p0.lastLocation
                        startForeground()
                        sendBroadcast()
                }
            }
        }
    }

    private fun sendBroadcast() {
        //local broadcast to update textview if app in foregorund

        val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        val intent = Intent("ACTIONREFRESH")
        //Set Intent data
        intent.putExtra("lat", location?.latitude.toString())
        intent.putExtra("lon", location?.longitude.toString())
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun startLocationUpdates() {
        var mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
        Log.i("Location updates", "Location update started ..............: ")
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = INTERVAL
        mLocationRequest?.fastestInterval = FASTEST_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest?.smallestDisplacement = 50f
    }

    private fun initGoogleApiClient() {
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
    }

    override fun onConnected(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}