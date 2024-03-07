package fe.linksheet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.material.color.DynamicColors
import fe.gson.GlobalGsonContext
import fe.gson.globalGsonModule
import fe.gson.typeadapter.ExtendedTypeAdapter
import fe.linksheet.activity.CrashHandlerActivity
import fe.linksheet.extension.koin.androidApplicationContext
import fe.linksheet.extension.koin.applicationLifecycle
import fe.linksheet.module.analytics.AnalyticsClient
import fe.linksheet.module.analytics.AnalyticsEvent
import fe.linksheet.module.analytics.analyticsModule
import fe.linksheet.module.analytics.client.DebugLogAnalyticsClient
import fe.linksheet.module.database.dao.module.daoModule
import fe.linksheet.module.database.databaseModule
import fe.linksheet.module.downloader.downloaderModule
import fe.linksheet.module.lifecycle.AppLifecycleObserver
import fe.linksheet.module.lifecycle.Service
import fe.linksheet.module.log.file.entry.LogEntry
import fe.linksheet.module.log.file.entry.LogEntryDeserializer
import fe.linksheet.module.log.file.logFileServiceModule
import fe.linksheet.module.log.impl.defaultLoggerModule
import fe.linksheet.module.redactor.redactorModule
import fe.linksheet.module.network.networkStateServiceModule
import fe.linksheet.module.preference.AppPreferenceRepository
import fe.linksheet.module.preference.AppPreferences
import fe.linksheet.module.preference.preferenceRepositoryModule
import fe.linksheet.module.repository.module.repositoryModule
import fe.linksheet.module.request.requestModule
import fe.linksheet.module.resolver.module.resolverModule
import fe.linksheet.module.resolver.urlresolver.amp2html.amp2HtmlResolveRequestModule
import fe.linksheet.module.resolver.urlresolver.base.allRemoteResolveRequest
import fe.linksheet.module.resolver.urlresolver.cachedRequestModule
import fe.linksheet.module.resolver.urlresolver.redirect.redirectResolveRequestModule
import fe.linksheet.module.shizuku.shizukuHandlerModule
import fe.linksheet.module.statistic.statisticsModule
import fe.linksheet.module.viewmodel.module.viewModelModule
import fe.linksheet.util.AndroidVersion
import fe.linksheet.util.Timer
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.lsposed.hiddenapibypass.HiddenApiBypass
import kotlin.system.exitProcess


open class LinkSheetApp : Application() {
    private lateinit var lifecycleObserver: AppLifecycleObserver
    private lateinit var timer: Timer

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()

        lifecycleObserver = AppLifecycleObserver(ProcessLifecycleOwner.get())
        lifecycleObserver.attach()

        timer = Timer.startTimer()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val crashIntent = Intent(this, CrashHandlerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(CrashHandlerActivity.EXTRA_CRASH_EXCEPTION, Log.getStackTraceString(throwable))
            }

            startActivity(crashIntent)
            exitProcess(2)
        }

        GlobalGsonContext.configure {
            ExtendedTypeAdapter.Default.register(this)
            registerTypeAdapter(LogEntry::class.java, LogEntryDeserializer)
        }

        if (AndroidVersion.AT_LEAST_API_28_P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }

        DynamicColors.applyToActivitiesIfAvailable(this)

        val koinApplication = startKoin {
            androidLogger()
            androidApplicationContext<LinkSheetApp>(this@LinkSheetApp)
            applicationLifecycle(lifecycleObserver)
            modules(
                networkStateServiceModule,
                logFileServiceModule,
                shizukuHandlerModule,
                globalGsonModule,
                preferenceRepositoryModule,
                redactorModule,
                defaultLoggerModule,
                databaseModule,
                daoModule,
                cachedRequestModule,
                redirectResolveRequestModule,
                amp2HtmlResolveRequestModule,
                allRemoteResolveRequest,
                resolverModule,
                repositoryModule,
                viewModelModule,
                requestModule,
                downloaderModule,
                if (BuildConfig.DEBUG) analyticsModule else DebugLogAnalyticsClient.module,
                statisticsModule
            )
        }

        lifecycleObserver.bootServices()

        if (BuildConfig.DEBUG) {
            // TODO: Remove once user is given the choice to opt in/out
            val analyticsClient = koinApplication.koin.get<AnalyticsClient>()
            val preferenceRepository = koinApplication.koin.get<AppPreferenceRepository>()

            val lastVersion = preferenceRepository.get(AppPreferences.lastVersion)
            analyticsClient.enqueue(createAppStartEvent(lastVersion))
        }
    }

    fun manageService(service: Service) {
        lifecycleObserver.register(service)
    }

    private fun createAppStartEvent(lastVersion: Int): AnalyticsEvent {
        return if (lastVersion == -1) AnalyticsEvent.FirstStart
        else if (BuildConfig.VERSION_CODE > lastVersion) AnalyticsEvent.AppUpdated(lastVersion)
        else AnalyticsEvent.AppStarted(BuildConfig.VERSION_CODE)
    }


//    override fun onStop(owner: LifecycleOwner) {
//        super.onStop(owner)
//
//        runCatching {
////            val analyticsClient = get<AnalyticsClient>()
////            analyticsClient.shutdown()
//
//            val preferenceRepository = get<AppPreferenceRepository>()
//
//            val currentUseTime = preferenceRepository.getLong(AppPreferences.useTimeMs)
//            val usedFor = timer.stop()
//
//            preferenceRepository.writeLong(AppPreferences.useTimeMs, currentUseTime + usedFor)
//            preferenceRepository.writeInt(AppPreferences.lastVersion, BuildConfig.VERSION_CODE)
//        }
//
//        get<AnalyticsClient>().shutdown()
//        get<NetworkState>().unregisterListener()
//        get<FileAppLogger>().writeLog()
//    }
}
