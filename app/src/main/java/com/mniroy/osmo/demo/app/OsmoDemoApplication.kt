package com.mniroy.osmo.demo.app

import android.app.Application
import com.mniroy.osmo.demo.app.di.AppContainer

class OsmoDemoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
