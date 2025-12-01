package com.example.splitify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SplitifyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}