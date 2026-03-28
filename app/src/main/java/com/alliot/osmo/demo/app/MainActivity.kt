package com.alliot.osmo.demo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alliot.osmo.demo.app.di.AppContainer
import com.alliot.osmo.demo.app.ui.AppRoot

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = AppContainer(applicationContext)
        setContent {
            AppRoot(container = container)
        }
    }
}
