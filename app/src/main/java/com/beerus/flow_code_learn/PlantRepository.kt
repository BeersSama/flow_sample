package com.beerus.flow_code_learn

import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.beerus.flow_code_learn.locals.database.PlantDao
import com.beerus.flow_code_learn.models.ComparablePair
import com.beerus.flow_code_learn.models.GrowZone
import com.beerus.flow_code_learn.models.Plant
import com.beerus.flow_code_learn.network.NetworkService
import com.beerus.flow_code_learn.utils.CacheOnSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlantRepository(
    private val plantDao: PlantDao,
    private val plantService: NetworkService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val plantsListSortOrderCache = CacheOnSuccess(onErrorFallback = { listOf() }) {
        plantService.customPlantSortOrder()
    }

    val plants: LiveData<List<Plant>> = liveData {
        val plantsLiveData = plantDao.getPlants()
        val customSortOrder = plantsListSortOrderCache.getOrAwait()
        emitSource(plantsLiveData.map { plantList ->
            plantList.applySort(customSortOrder)
        })
    }

    /***
     * c1:
     * fun getPlantsWithGrowZone(growZone: GrowZone) = liveData {
            val plantsGrowZone = plantDao.getPlantsWithGrowZoneNumber(growZone.number)
            val customSortOrder = plantsListSortOrderCache.getOrAwait()
            emitSource(plantsGrowZone.map { plantList ->
            plantList.applySort(customSortOrder)
            })
        }
     */

    fun getPlantsWithGrowZone(growZone: GrowZone) =
        plantDao.getPlantsWithGrowZoneNumber(growZone.number)
            .switchMap { plantList ->
                liveData {
                    val customSortOrder = plantsListSortOrderCache.getOrAwait()
                    emit(plantList.applyMainSafeSort(customSortOrder))
                }
            }

    // Flow in here.
    val plantsFlow: Flow<List<Plant>> =
        plantDao.getPlantsFlow()

    fun getPlantsWithGrowZoneFlow(growZone: GrowZone) =
        plantDao.getPlantsWithGrowZoneNumberFlow(growZone.number)

    // apply sort (Plants with custom sorting - service + dao)
    private fun List<Plant>.applySort(customSortOrder: List<String>): List<Plant> {
        return sortedBy { plant ->
            val positionForItem = customSortOrder.indexOf(plant.plantId).let { order ->
                if (order > -1) order else Int.MAX_VALUE
            }
            ComparablePair(positionForItem, plant.name)
        }
    }

    // apply sort safe main thread call.
    @AnyThread
    suspend fun List<Plant>.applyMainSafeSort(customSortOrder: List<String>) =
        withContext(defaultDispatcher) {
            this@applyMainSafeSort.applySort(customSortOrder)
        }


    private fun shouldUpdatePlantsCache(): Boolean {
        return true
    }

    // update the plants cache.
    suspend fun tryUpdateRecentPlantsCache() {
        if (shouldUpdatePlantsCache()) fetchRecentPlants()
    }

    suspend fun tryUpdateRecentPlantsForGrowZoneCache(growZoneNumber: GrowZone) {
        if (shouldUpdatePlantsCache()) fetchPlantsForGrowZone(growZoneNumber)
    }

    private suspend fun fetchRecentPlants() {
        val plants = plantService.allPlants()
        plantDao.insertAll(plants)
    }

    private suspend fun fetchPlantsForGrowZone(growZone: GrowZone) {
        val plants = plantService.plantsByGrowZone(growZone)
        plantDao.insertAll(plants)
    }

    companion object {

        // For singleton instantiation
        private var instance: PlantRepository? = null

        fun getInstance(plantDao: PlantDao, plantService: NetworkService) =
            instance ?: synchronized(this) {
                instance ?: PlantRepository(plantDao, plantService).also { instance = it }
            }
    }
}