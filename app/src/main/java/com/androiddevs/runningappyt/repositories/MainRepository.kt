package com.androiddevs.runningappyt.repositories

import com.androiddevs.runningappyt.database.Run
import com.androiddevs.runningappyt.database.RunDAO
import com.androiddevs.runningappyt.database.RunDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunsSortedByDate() = runDAO.getRunsSortedByDate()

    fun getAllRunsSortedByTime() = runDAO.getRunsSortedByTimeInMillis()

    fun getAllRunsSortedBySpeed() = runDAO.getRunSortedBySpeed()

    fun getAllRunsSortedByDistance() = runDAO.getRunsSortedByDistance()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getRunsSortedByCaloriesBurned()

    fun getTotalTime() = runDAO.getTotalTimeInMillis()

    fun getTotalDistance() = runDAO.getTotalDistanceInMeters()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeedInKMPH()
}