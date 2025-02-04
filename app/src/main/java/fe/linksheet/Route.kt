package fe.linksheet


import androidx.annotation.Keep
import fe.android.compose.route.util.Route1
import fe.android.compose.route.util.Route2
import fe.android.compose.route.util.RouteData
import fe.android.compose.route.util.route


const val mainRoute = "main_route"
const val settingsRoute = "settings_route"
const val appsSettingsRoute = "apps_settings_route"
const val browserSettingsRoute = "browser_settings_route"

const val inAppBrowserSettingsDisableInSelectedRoute = "inapp_browser_settings_route"
const val whitelistedBrowsersSettingsRoute = "whitelisted_browsers_settings_route"

//@Keep
//data class InAppBrowserSettingsDisableInSelectedRoute(
//
//) : RouteData {
//
//}
//
//val inAppBrowserSettingsDisableInSelectedRoute = route(
//    "inapp_browser_settings_route",
//    route = InAppBrowserSettingsDisableInSelectedRoute
//)

//@Keep
//data class WhitelistedBrowserSettingsRoute(
//
//) : RouteData {
//
//}
//
//val whitelistedBrowserSettingsRoute = route(
//    "whitelisted_browser_settings_route",
//    route = WhitelistedBrowserSettingsRoute
//)


const val aboutSettingsRoute = "about_settings_route"
const val creditsSettingsRoute = "credits_settings_route"
const val donateSettingsRoute = "donate_settings_route"
const val devBottomSheetExperimentRoute = "dev_bottom_sheet_experiment_route"

const val themeSettingsRoute = "theme_settings_route"

const val advancedSettingsRoute = "advanced_settings_route"
const val shizukuSettingsRoute = "shizuku_settings_route"
const val featureFlagSettingsRoute = "feature_flag_settings_route"

const val exportImportSettingsRoute = "export_import_settings_route"

const val debugSettingsRoute = "debug_settings_route"
const val logViewerSettingsRoute = "log_viewer_settings_route"
const val loadDumpedPreferences = "log_dumped_reference_settings_route"

@Keep
data class ExperimentSettingsRouteArg(val experiment: String?) : RouteData {
    companion object : Route1<ExperimentSettingsRouteArg, String?>(
        Argument(ExperimentSettingsRouteArg::experiment),
        { experiment -> ExperimentSettingsRouteArg(experiment) }
    )
}

val experimentSettingsRoute = route("experiment_settings_route", route = ExperimentSettingsRouteArg)

@Keep
data class LogTextViewerRoute(
    val timestamp: String,
    val fileName: String?,
) : RouteData {
    companion object : Route2<LogTextViewerRoute, String, String?>(
        Argument(LogTextViewerRoute::timestamp),
        Argument(LogTextViewerRoute::fileName),
        { timestamp, fileName -> LogTextViewerRoute(timestamp, fileName) }
    )
}

val logTextViewerSettingsRoute = route(
    "log_text_viewer_settings_route",
    route = LogTextViewerRoute
)

const val linksSettingsRoute = "link_settings_route"
const val libRedirectSettingsRoute = "lib_redirect_settings_route"

@Keep
data class LibRedirectServiceRoute(val serviceKey: String) : RouteData {
    companion object : Route1<LibRedirectServiceRoute, String>(
        Argument(LibRedirectServiceRoute::serviceKey),
        { LibRedirectServiceRoute(it) }
    )
}

val libRedirectServiceSettingsRoute = route(
    "lib_redirect_service_settings_route",
    route = LibRedirectServiceRoute
)

const val followRedirectsSettingsRoute = "follow_redirects_settings_route"
const val downloaderSettingsRoute = "downloader_settings_route"
const val amp2HtmlSettingsRoute = "amp2html_settings_route"


const val generalSettingsRoute = "general_settings_route"
const val privacySettingsRoute = "privacy_settings_route"
const val notificationSettingsRoute = "notification_settings_route"
const val bottomSheetSettingsRoute = "bottom_sheet_settings_route"
const val preferredBrowserSettingsRoute = "preferred_browser_settings_route"
const val inAppBrowserSettingsRoute = "in_app_browser_settings_route"
const val preferredAppsSettingsRoute = "preferred_apps_settings_route"
const val appsWhichCanOpenLinksSettingsRoute = "apps_which_can_open_links_settings_route"
const val pretendToBeAppRoute = "pretend_to_be_app"
const val devModeRoute = "dev_mode"
