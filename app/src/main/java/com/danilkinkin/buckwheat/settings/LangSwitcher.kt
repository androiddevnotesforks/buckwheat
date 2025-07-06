package com.danilkinkin.buckwheat.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.appLocale
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.titleCase
import kotlinx.coroutines.launch
import switchOverrideLocale
import java.util.Locale

const val SETTINGS_CHANGE_LOCALE_SHEET = "settings.changeLocale"

@Composable
fun LangSwitcher(appViewModel: AppViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales[0]

    ButtonRow(
        icon = painterResource(R.drawable.ic_language),
        text = stringResource(R.string.locale_label),
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APP_LOCALE_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    startActivity(context, intent, null)
                } catch (error: Error) {
                    appViewModel.openSheet(PathState(SETTINGS_CHANGE_LOCALE_SHEET))
                }
            } else {
                appViewModel.openSheet(PathState(SETTINGS_CHANGE_LOCALE_SHEET))
            }
        },
        endCaption = currentLocale.displayName.titleCase(),
    )
}

@Composable
fun LangSwitcherDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val overrideLocale = LocalContext.current.appLocale
    val coroutineScope = rememberCoroutineScope()
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val scrollState = rememberScrollState()

    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    fun handleSwitchLang(localeCode: String?) {
        coroutineScope.launch {
            switchOverrideLocale(context, localeCode)
            onClose()
        }
    }

    val currentLocale = configuration.locales[0]
    val locales = listOf(
        Locale.forLanguageTag("be"),
        Locale.forLanguageTag("cs"),
        Locale.forLanguageTag("de"),
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("fr"),
        Locale.forLanguageTag("ia"),
        Locale.forLanguageTag("it"),
        Locale.forLanguageTag("ja"),
        Locale.forLanguageTag("pt-BR"),
        Locale.forLanguageTag("ro"),
        Locale.forLanguageTag("ru"),
        Locale.forLanguageTag("sr"),
        Locale.forLanguageTag("sv"),
        Locale.forLanguageTag("ta"),
        Locale.forLanguageTag("tr"),
        Locale.forLanguageTag("uk"),
        Locale.forLanguageTag("zh-CN"),
        Locale.forLanguageTag("zh-TW"),
        Locale.forLanguageTag("pl"),
        Locale.forLanguageTag("sk-SK"),
        Locale.forLanguageTag("hr"),
        Locale.forLanguageTag("nl"),
    )

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(modifier = Modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.locale_label),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState).padding(bottom = navigationBarHeight),
            ) {
                CheckedRow(
                    text = stringResource(R.string.locale_system),
                    checked = overrideLocale === null,
                    onValueChange = { handleSwitchLang(null) },
                )
                locales
                    .sortedBy { locale ->
                        locale.getDisplayName(locale).titleCase()
                    }
                    .forEach { locale ->
                        CheckedRow(
                            text = locale.getDisplayName(locale).titleCase(),
                            checked = overrideLocale !== null && locale.language === currentLocale.language,
                            onValueChange = { handleSwitchLang(locale.language) },
                        )
                    }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Surface {
            LangSwitcher()
        }
    }
}
