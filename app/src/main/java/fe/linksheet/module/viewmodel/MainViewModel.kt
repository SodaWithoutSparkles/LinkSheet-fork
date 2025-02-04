package fe.linksheet.module.viewmodel

import android.app.Activity
import android.app.Application
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.getSystemService


import fe.linksheet.BuildConfig
import fe.linksheet.LinkSheetAppConfig
import fe.linksheet.R
import fe.linksheet.donationBannerAfterMinutes
import fe.linksheet.extension.android.resolveActivityCompat
import fe.linksheet.extension.android.startActivityWithConfirmation
import fe.linksheet.module.analytics.AnalyticsClient
import fe.linksheet.module.preference.app.AppPreferenceRepository
import fe.linksheet.module.preference.app.AppPreferences
import fe.linksheet.module.preference.flags.FeatureFlagRepository
import fe.linksheet.module.preference.SensitivePreference
import fe.linksheet.module.resolver.BrowserResolver
import fe.linksheet.module.resolver.KnownBrowser
import fe.linksheet.module.viewmodel.base.BaseViewModel
import fe.linksheet.util.AndroidVersion
import java.time.Duration


class MainViewModel(
    val context: Application,
    val preferenceRepository: AppPreferenceRepository,
    val browserResolver: BrowserResolver,
    featureFlagRepository: FeatureFlagRepository,
    val analyticsClient: AnalyticsClient
) : BaseViewModel(preferenceRepository) {

    val firstRun = preferenceRepository.asState(AppPreferences.firstRun)

    @OptIn(SensitivePreference::class)
    val useTimeMs = preferenceRepository.get(AppPreferences.useTimeMs)
    val showDiscordBanner = preferenceRepository.asState(AppPreferences.showDiscordBanner)
    val donateCardDismissed = preferenceRepository.asState(AppPreferences.donateCardDismissed)
    var theme = preferenceRepository.asState(AppPreferences.theme)

    private val roleManager by lazy {
        if (AndroidVersion.AT_LEAST_API_26_O) {
            context.getSystemService<RoleManager>()
        } else null
    }

    fun formatUseTime(): Pair<Int?, Int?>? {
        if (!LinkSheetAppConfig.showDonationBanner()) return null

        val duration = Duration.ofMillis(useTimeMs)
        val minutes = duration.toMinutesPart()
        if (minutes < donationBannerAfterMinutes) return null

        val hours = duration.toHoursPart()
        if (hours > 0) {
            return hours to null
        }

        return null to minutes
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getRequestRoleBrowserIntent() = roleManager!!.createRequestRoleIntent(
        RoleManager.ROLE_BROWSER
    )

    fun openDefaultBrowserSettings(
        activity: Activity
    ) = activity.startActivityWithConfirmation(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))

    fun checkDefaultBrowser() = context.packageManager
        .resolveActivityCompat(BrowserResolver.httpBrowserIntent, PackageManager.MATCH_DEFAULT_ONLY)
        ?.activityInfo?.packageName == BuildConfig.APPLICATION_ID

    enum class BrowserStatus(
        @StringRes val headline: Int,
        @StringRes val subtitle: Int,
        val containerColor: @Composable () -> Color,
        val color: @Composable () -> Color,
        val icon: ImageVector,
        @StringRes val iconDescription: Int
    ) {

        Known(
            R.string.at_least_one_known_browser_installed,
            R.string.at_least_one_known_browser_installed_explainer,
            { MaterialTheme.colorScheme.primaryContainer },
            { MaterialTheme.colorScheme.onSurface },
            Icons.Default.Public,
            R.string.success
        ),
        Unknown(
            R.string.at_least_one_unknown_browser_installer,
            R.string.at_least_one_unknown_browser_installer_explainer,
            { MaterialTheme.colorScheme.tertiaryContainer },
            { MaterialTheme.colorScheme.onTertiaryContainer },
            Icons.Default.Warning,
            R.string.warning
        ),
        None(
            R.string.no_browser_installed,
            R.string.no_browser_installed_explainer,
            { MaterialTheme.colorScheme.error },
            { MaterialTheme.colorScheme.onError },
            Icons.Default.Error,
            R.string.error
        );

        companion object {
            fun hasBrowser(browserStatus: BrowserStatus): Boolean {
                return browserStatus == Unknown || browserStatus == Known
            }
        }
    }

    fun hasBrowser(): BrowserStatus {
        val browsers = browserResolver.queryBrowsers()
        if (browsers.isEmpty()) return BrowserStatus.None
        if (browsers.any { KnownBrowser.isKnownBrowser(it.key) != null }) return BrowserStatus.Known
        return BrowserStatus.Unknown
    }
}
