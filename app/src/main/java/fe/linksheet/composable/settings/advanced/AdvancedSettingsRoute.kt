package fe.linksheet.composable.settings.advanced

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fe.linksheet.R
import fe.linksheet.composable.settings.SettingsScaffold
import fe.linksheet.composable.util.ColoredIcon
import fe.linksheet.composable.util.SettingsItemRow
import fe.linksheet.shizukuSettingsRoute

@Composable
fun AdvancedSettingsRoute(
    navController: NavHostController,
    onBackPressed: () -> Unit,
) {
    SettingsScaffold(R.string.advanced, onBackPressed = onBackPressed) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 5.dp)
        ) {
            item(key = "shizuku") {
                SettingsItemRow(
                    navController = navController,
                    navigateTo = shizukuSettingsRoute,
                    headlineId = R.string.shizuku,
                    subtitleId = R.string.shizuku_explainer,
                    image = {
                        ColoredIcon(icon = Icons.Default.Cable, descriptionId = R.string.advanced)
                    }
                )
            }
        }
    }
}