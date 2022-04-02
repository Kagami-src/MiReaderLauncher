package com.kagami.mireaderlauncher

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val _appsLiveData: MutableLiveData<List<AppData>> = MutableLiveData()
    val appsLiveData: LiveData<List<AppData>>
        get() = _appsLiveData
    fun reloadApps(){
        viewModelScope.launch {
            val apps=loadApps()
            _appsLiveData.value=apps
        }
    }
    suspend fun loadApps():List<AppData> = withContext(Dispatchers.Default){
        val packageManager = getApplication<Application>().packageManager
        val systemPackages= setOf<String>(
            "com.apps.moan",
            "com.android.settings"
        )
        val list=packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter {
                !systemPackages.contains(it.packageName)
            }
        val apps = mutableListOf<AppData>()
        list.forEach {
            var intent = packageManager.getLaunchIntentForPackage(it.packageName)
            if(intent!=null) {
                Log.e("kagamilog",it.packageName)
                val lab=packageManager.getApplicationLabel(it)
                val icon=packageManager.getApplicationIcon(it)
                val data=AppData(lab.toString(),it.packageName,icon)
                apps.add(data)
            }
        }
        return@withContext apps
    }

    data class AppData(
        var name:String,
        var appId:String,
        var icon: Drawable?
    )
}