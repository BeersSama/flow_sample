package com.beerus.flow_code_learn.utils

import android.content.Context
import com.beerus.flow_code_learn.PlantRepository
import com.beerus.flow_code_learn.locals.database.AppDatabase
import com.beerus.flow_code_learn.network.NetworkService
import com.beerus.flow_code_learn.viewmodels.PlantListViewModelFactory

interface ViewModelFactoryProvider {
    fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory
}

val Injector: ViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider : ViewModelFactoryProvider {
    private fun getPlantRepository(context: Context): PlantRepository {
        return PlantRepository.getInstance(
            plantDao(context),
            plantService()
        )
    }

    private fun plantService() = NetworkService()

    private fun plantDao(context: Context) =
        AppDatabase.getInstance(context.applicationContext).plantDao()

    override fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory {
        val repository = getPlantRepository(context)
        return PlantListViewModelFactory(repository)
    }
}

@Volatile
private var currentInjector: ViewModelFactoryProvider =
    DefaultViewModelProvider