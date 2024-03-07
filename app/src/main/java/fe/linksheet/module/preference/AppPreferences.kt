package fe.linksheet.module.preference


import com.google.gson.JsonArray
import fe.android.preference.helper.PreferenceDefinition
import fe.gson.dsl.jsonObject
import fe.gson.util.jsonArrayItems
import fe.linksheet.module.analytics.TelemetryLevel
import fe.linksheet.module.redactor.PackageProcessor
import fe.linksheet.module.redactor.Redactor
import fe.linksheet.module.resolver.BrowserHandler
import fe.linksheet.module.resolver.InAppBrowserHandler
import fe.linksheet.ui.Theme
import java.util.*

object AppPreferences : PreferenceDefinition(
    "enable_copy_button",
    "single_tap",
    "enable_send_button",
    "show_new_bottom_sheet_banner"
) {
    val hideAfterCopying = booleanPreference("hide_after_copying")
    val usageStatsSorting = booleanPreference("usage_stats_sorting")

    val browserMode = mappedPreference(
        "browser_mode",
        BrowserHandler.BrowserMode.AlwaysAsk,
        BrowserHandler.BrowserMode
    )

    @SensitivePreference
    val selectedBrowser = stringPreference("selected_browser")

    val inAppBrowserMode = mappedPreference(
        "in_app_browser_mode",
        BrowserHandler.BrowserMode.AlwaysAsk,
        BrowserHandler.BrowserMode
    )

    @SensitivePreference
    val selectedInAppBrowser = stringPreference("selected_in_app_browser")
    val unifiedPreferredBrowser = booleanPreference("unified_preferred_browser", true)

    val inAppBrowserSettings = mappedPreference(
        "in_app_browser_setting",
        InAppBrowserHandler.InAppBrowserMode.UseAppSettings,
        InAppBrowserHandler.InAppBrowserMode
    )
    val alwaysShowPackageName = booleanPreference("always_show_package_name")
    val urlCopiedToast = booleanPreference("url_copied_toast", true)
    val downloadStartedToast = booleanPreference("download_started_toast", true)
    val openingWithAppToast = booleanPreference("opening_with_app_toast", true)
    val resolveViaToast = booleanPreference("resolve_via_toast", true)
    val resolveViaFailedToast = booleanPreference("resolve_via_failed_toast", true)

    val gridLayout = booleanPreference("grid_layout")
    val useClearUrls = booleanPreference("use_clear_urls")
    val useFastForwardRules = booleanPreference("fast_forward_rules")
    val enableLibRedirect = booleanPreference("enable_lib_redirect")

    val followRedirects = booleanPreference("follow_redirects")
    val followRedirectsLocalCache = booleanPreference("follow_redirects_local_cache", true)
    val followRedirectsExternalService = booleanPreference("follow_redirects_external_service")
    val followOnlyKnownTrackers = booleanPreference("follow_only_known_trackers")
    val followRedirectsBuiltInCache = booleanPreference("follow_redirects_builtin_cache", true)
    val followRedirectsAllowDarknets = booleanPreference("follow_redirects_allow_darknets", false)


    val theme = mappedPreference("theme", Theme.System, Theme)
    val dontShowFilteredItem = booleanPreference("dont_show_filtered_item")

    @Deprecated("No longer used")
    val useTextShareCopyButtons = booleanPreference("use_text_share_copy_buttons")
    val previewUrl = booleanPreference("preview_url", true)
    val enableDownloader = booleanPreference("enable_downloader")
    val downloaderCheckUrlMimeType = booleanPreference("downloaderCheckUrlMimeType")

    val enableIgnoreLibRedirectButton = booleanPreference("enable_ignore_lib_redirect_button")

    val requestTimeout = intPreference("follow_redirects_timeout", 15)

    val enableAmp2Html = booleanPreference("enable_amp2html")
    val amp2HtmlLocalCache = booleanPreference("amp2html_local_cache", true)
    val amp2HtmlExternalService = booleanPreference("amp2html_external_service")
    val amp2HtmlBuiltInCache = booleanPreference("amp2html_builtin_cache", true)
    val amp2HtmlAllowDarknets = booleanPreference("amp2html_allow_darknets", false)

    val enableRequestPrivateBrowsingButton = booleanPreference(
        "enable_request_private_browsing_button"
    )

    @SensitivePreference
    val useTimeMs = longPreference("use_time", 0)

    val showLinkSheetAsReferrer = booleanPreference("show_as_referrer")
    val devModeEnabled = booleanPreference("dev_mode_enabled")

    @SensitivePreference
    val logKey = stringPreference("log_key") {
        Redactor.createHmacKey()
    }

    val firstRun = booleanPreference("first_run", true)
    val showDiscordBanner = booleanPreference("show_discord_banner", true)

    val devBottomSheetExperimentCard = booleanPreference("show_dev_bottom_sheet_experiment_card", true)

    val useDevBottomSheet = booleanPreference("use_dev_bottom_sheet")
    val donateCardDismissed = booleanPreference("donate_card_dismissed")

    val devBottomSheetExperiment = booleanPreference("dev_bottom_sheet_experiment", true)
    val resolveEmbeds = booleanPreference("resolve_embeds")
    val hideBottomSheetChoiceButtons = booleanPreference("hide_bottom_sheet_choice_buttons")

    val telemetryIdentity = stringPreference("telemetry_identity") {
        UUID.randomUUID().toString()
    }

    val telemetryLevel = mappedPreference("telemetry_level", TelemetryLevel.Basic, TelemetryLevel)
    val lastVersion = intPreference("last_version", -1)


    init {
        finalize()
    }

    @SensitivePreference
    val sensitivePreferences = setOf(
        useTimeMs, logKey,
    )

    @SensitivePreference
    private val sensitivePackagePreferences = setOf(
        selectedBrowser, selectedInAppBrowser
    )

    @OptIn(SensitivePreference::class)
    fun logPackages(redactor: Redactor, repository: AppPreferenceRepository): Map<String, String?> {
        return sensitivePackagePreferences.associate { pref ->
            val value = repository.get(pref)
            val strValue = value?.let { redactor.processToString(it, PackageProcessor) } ?: "<null>"

            pref.key to strValue
        }
    }

    fun toJsonArray(preferences: Map<String, String?>): JsonArray {
        val objs = preferences.map { (key, value) ->
            jsonObject {
                "name" += key
                "value" += value
            }
        }

        return jsonArrayItems(objs)
    }
}


