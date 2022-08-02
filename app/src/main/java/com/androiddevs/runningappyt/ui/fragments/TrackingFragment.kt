package com.androiddevs.runningappyt.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.base.BaseFragment
import com.androiddevs.runningappyt.services.Polyline
import com.androiddevs.runningappyt.services.TrackingService
import com.androiddevs.runningappyt.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.fragment_tracking.*

class TrackingFragment : BaseFragment() {

    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        setViews()
        setListeners()
        setObservers()
    }

    // Handling the lifecycle of mapview or else use map fragment instead of map view
    private fun setViews() {
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
    }

    private fun setListeners() {
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
    }

    private fun setObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Pause"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun sendCommandToService(action: String) = Intent(requireContext(), TrackingService::class.java).also {
        it.action = action
        requireContext().startService(it)
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val lastSecondCoordinate = pathPoints.last()[pathPoints.last().size -2]
            val lastCoordinate = pathPoints.last().last()
            val polylineOptions = PolylineOptions().add(lastSecondCoordinate, lastCoordinate).color(Constants.POLYLINE_COLOR).width(Constants.POLYLINE_WIDTH);
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), Constants.MAP_ZOOM))
        }
    }

    private fun addAllPolylines() {
        if (pathPoints.isNotEmpty()) {
            for (polyline in pathPoints) {
                val polylineOptions = PolylineOptions().color(Constants.POLYLINE_COLOR).width(Constants.POLYLINE_WIDTH).addAll(polyline)
                map?.addPolyline(polylineOptions)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}