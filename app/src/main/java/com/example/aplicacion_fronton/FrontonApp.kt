package com.example.aplicacion_fronton

import android.app.Application
import com.example.aplicacion_fronton.network.DivisionRankingStore
import com.example.aplicacion_fronton.network.DuelosMostradosStore
import com.example.aplicacion_fronton.network.OnboardingStore
import com.example.aplicacion_fronton.network.PosicionRankingStore
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.TokenStore

class FrontonApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.inicializar(
            TokenStore(applicationContext),
            DuelosMostradosStore(applicationContext),
            PosicionRankingStore(applicationContext),
            DivisionRankingStore(applicationContext),
            OnboardingStore(applicationContext),
        )
    }
}
