package com.androiddevs.runningappyt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.database.Run
import com.androiddevs.runningappyt.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    val runSortedByDate = mainRepository.getAllRunsSortedByDate()
    val runSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    val runSortedBySpeed = mainRepository.getAllRunsSortedBySpeed()
    val runSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    val runSortedByTime = mainRepository.getAllRunsSortedByTime()

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}