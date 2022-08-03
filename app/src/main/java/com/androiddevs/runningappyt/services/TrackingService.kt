package com.androiddevs.runningappyt.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.permissions.TrackingUtility
import com.androiddevs.runningappyt.ui.MainActivity
import com.androiddevs.runningappyt.utils.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var serviceKilled = false

    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject lateinit var baseNotificationBuilder: NotificationCompat.Builder
    @Inject lateinit var baseNotificationManager: NotificationManager

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
        }

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            if (isTracking.value!!) {
                locationResult.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Log.e("TAG", "Location: ${location.latitude},  ${location.longitude}")
                    }
                }
            }
        }
    }


    companion object {
        val timeRunInSecs = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    override fun onCreate() {
        super.onCreate()

        postInitialValues()
        setObservers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service..")
                        startTimer()
                    }
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setObservers() {
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })

        timeRunInSecs.observe(this, Observer {
            if (!serviceKilled) {
                val notification = currentNotificationBuilder.apply {
                    setContentText(TrackingUtility.getFormattedStopwatchTime(it * 1000L))
                }
                baseNotificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
            }
        })
    }

    private fun postInitialValues() {
        timeRunInSecs.postValue(0L)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())

        currentNotificationBuilder = baseNotificationBuilder
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = Constants.ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = Constants.ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }


        // Remove previous actions from a notification
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            currentNotificationBuilder = baseNotificationBuilder.addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            baseNotificationManager.notify(Constants.NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInSecs.postValue((timeRun + lapTime) / 1000)
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }

    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            val locationRequest = LocationRequest.create().apply {
                interval = Constants.LOCATION_REQUEST_INTERVAL
                priority = Priority.PRIORITY_HIGH_ACCURACY
                fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let { it ->
            val pos = LatLng(it.latitude, it.longitude)
            pathPoints.value?.apply {
                this.last { polyline ->
                    polyline.add(pos)
                }
                pathPoints.postValue(this)
            }
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

    }

    private fun startForegroundService() {
        isTracking.postValue(true)
        startTimer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(baseNotificationManager)
        }

        startForeground(Constants.NOTIFICATION_ID, baseNotificationBuilder.build())
    }

//    pathPoints.postValue()
}