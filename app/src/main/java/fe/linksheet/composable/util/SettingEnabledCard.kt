package fe.linksheet.composable.util

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fe.android.preference.helper.BasePreference
import fe.android.preference.helper.compose.RepositoryState
import fe.linksheet.R
import fe.linksheet.module.viewmodel.base.BaseViewModel
import fe.linksheet.ui.HkGroteskFontFamily

@Composable
fun SettingEnabledCardColumn(
    state: RepositoryState<Boolean, Boolean, BasePreference.Preference<Boolean>>,
    viewModel: BaseViewModel,
    @StringRes headlineId: Int,
    @StringRes subtitleId: Int,
    @StringRes contentTitleId: Int,
) {
    SettingEnabledCardColumn(
        state = state,
        viewModel = viewModel,
        headline = stringResource(id = headlineId),
        subtitle = stringResource(id = subtitleId),
        contentTitle = stringResource(id = contentTitleId)
    )
}

@Composable
fun SettingEnabledCardColumn(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    headline: String,
    subtitle: String? = null,
    subtitleBuilder: @Composable ((enabled: Boolean) -> Unit)? = if (subtitle != null) {
        { SubtitleText(subtitle = subtitle) }
    } else null,
    contentTitle: String? = null,
) {
    SettingEnabledCardColumnCommon(contentTitle = contentTitle) {
        SwitchRow(
            checked = checked,
            onChange = onChange,
            headline = headline,
            subtitle = subtitle,
            subtitleBuilder = subtitleBuilder,
        )
    }
}

@Composable
fun SettingEnabledCardColumn(
    state: RepositoryState<Boolean, Boolean, BasePreference.Preference<Boolean>>,
    viewModel: BaseViewModel,
    headline: String,
    subtitle: String? = null,
    subtitleBuilder: @Composable ((enabled: Boolean) -> Unit)? = if (subtitle != null) {
        { SubtitleText(subtitle = subtitle) }
    } else null,
    contentTitle: String,
) {
    SettingEnabledCardColumnCommon(contentTitle = contentTitle) {
        SwitchRow(
            state = state,
            viewModel = viewModel,
            headline = headline,
            subtitle = subtitle,
            subtitleBuilder = subtitleBuilder,
        )
    }
}

@Composable
private fun SettingEnabledCardColumnCommon(
    contentTitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            content()
        }

        if (contentTitle != null) {
            Spacer(modifier = Modifier.height(10.dp))
            SettingSpacerText(contentTitle = contentTitle)
        }
    }
}