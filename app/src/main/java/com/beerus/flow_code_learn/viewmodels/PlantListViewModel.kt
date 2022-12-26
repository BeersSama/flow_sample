package com.beerus.flow_code_learn.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.beerus.flow_code_learn.MyApplication.Companion.TAG
import com.beerus.flow_code_learn.PlantRepository
import com.beerus.flow_code_learn.models.GrowZone
import com.beerus.flow_code_learn.models.NoGrowZone
import com.beerus.flow_code_learn.models.Plant
import com.bumptech.glide.Glide.init
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlantListViewModel(
    private val plantRepository: PlantRepository
) : ViewModel() {
    val snackBar: LiveData<String?>
        get() = _snackBar

    private val _snackBar = MutableLiveData<String?>()

    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _spinner = MutableLiveData<Boolean>()

    private val growZone = MutableLiveData(NoGrowZone)

    val plants: LiveData<List<Plant>> = growZone.switchMap { growZone ->
        Log.d(TAG, "growZone")
        if (growZone == NoGrowZone) {
            plantRepository.plants
        } else {
            plantRepository.getPlantsWithGrowZone(growZone)
        }
    }

    // Flow in here.
    val plantsUsingFlow: LiveData<List<Plant>> =
        plantRepository.plantsFlow.asLiveData()

    init {
        clearGrowZoneNumber()
    }

    fun clearGrowZoneNumber() {
        growZone.value = NoGrowZone
        launchDataLoad {
            plantRepository.tryUpdateRecentPlantsCache()
        }
    }

    fun setGrowZoneNumber(num: Int) {
        growZone.value = GrowZone((num))

        launchDataLoad {
            plantRepository.tryUpdateRecentPlantsCache()
        }
    }

    fun isFiltered() = growZone.value != NoGrowZone

    fun onSnackBarShown() { // Duoc goi sau khi snackbar hien thi
        _snackBar.value = null
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _snackBar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }

    companion object {
        fun factory(repository: PlantRepository) = object : ViewModelProvider.NewInstanceFactory() {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return if (modelClass.isAssignableFrom(
                        PlantListViewModel::class.java
                    )
                ) PlantListViewModel(repository) as T
                else throw IllegalArgumentException("ViewModel Unknown class!")
            }
        }
    }
}

class PlantListViewModelFactory(
    private val repository: PlantRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return if (modelClass.isAssignableFrom(
                PlantListViewModel::class.java
            )
        ) PlantListViewModel(repository) as T
        else throw IllegalArgumentException("ViewModel Unknown class!")
    }
}