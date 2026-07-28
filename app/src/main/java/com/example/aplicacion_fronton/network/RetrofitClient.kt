package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Único punto de configuración de Retrofit/OkHttp. Cambiar de servidor local a
 * producción es una sola línea (BASE_URL en app/build.gradle.kts), y el
 * interceptor de JWT + el manejo global de 401 viven acá, no repetidos en cada
 * pantalla — mismo patrón que funcionó bien en el proyecto anterior.
 */
object RetrofitClient {
    lateinit var tokenStore: TokenStore
        private set
    lateinit var duelosMostradosStore: DuelosMostradosStore
        private set
    lateinit var posicionRankingStore: PosicionRankingStore
        private set
    lateinit var divisionRankingStore: DivisionRankingStore
        private set
    lateinit var onboardingStore: OnboardingStore
        private set
    private var inicializado = false

    fun inicializar(
        tokenStoreInstancia: TokenStore,
        duelosMostradosStoreInstancia: DuelosMostradosStore,
        posicionRankingStoreInstancia: PosicionRankingStore,
        divisionRankingStoreInstancia: DivisionRankingStore,
        onboardingStoreInstancia: OnboardingStore,
    ) {
        if (inicializado) return
        tokenStore = tokenStoreInstancia
        duelosMostradosStore = duelosMostradosStoreInstancia
        posicionRankingStore = posicionRankingStoreInstancia
        divisionRankingStore = divisionRankingStoreInstancia
        onboardingStore = onboardingStoreInstancia
        inicializado = true
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }
    val versusService: VersusService by lazy { retrofit.create(VersusService::class.java) }
    val compromisosService: CompromisosService by lazy { retrofit.create(CompromisosService::class.java) }
    val rankingService: RankingService by lazy { retrofit.create(RankingService::class.java) }
    val usuariosService: UsuariosService by lazy { retrofit.create(UsuariosService::class.java) }
    val prediccionesService: PrediccionesService by lazy { retrofit.create(PrediccionesService::class.java) }
    val notificacionesService: NotificacionesService by lazy { retrofit.create(NotificacionesService::class.java) }
}
