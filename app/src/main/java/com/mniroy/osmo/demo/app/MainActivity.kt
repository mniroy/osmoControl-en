package com.mniroy.osmo.demo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mniroy.osmo.demo.app.di.AppContainer
import com.mniroy.osmo.demo.app.ui.AppRoot

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        container = (application as OsmoDemoApplication).container
        setContent {
            AppRoot(container = container)
        }
    }
}
