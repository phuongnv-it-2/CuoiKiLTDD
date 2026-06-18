package com.project24itb156.gglens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project24itb156.gglens.navigation.AppNavigation
import com.project24itb156.gglens.ui.theme.GGLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GGLensTheme {
                AppNavigation()
            }
        }
    }
}
