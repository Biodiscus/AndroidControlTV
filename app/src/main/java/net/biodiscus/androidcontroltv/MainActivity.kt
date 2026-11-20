package net.biodiscus.androidcontroltv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidControlTVTheme {
                AndroidControlTVApp()
            }
        }
    }
}
