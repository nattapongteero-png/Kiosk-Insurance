package th.go.banlat.kiosk.ui.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import th.go.banlat.kiosk.ui.theme.KioskScaleProvider

/**
 * กด Preview ใน Android Studio เพื่อดูองค์ประกอบ
 * แต่อนิเมชันจะนิ่ง ต้องกด "Start Interactive Mode" หรือรันบนเครื่องจริงถึงจะเห็นบัตรเคลื่อนไหว
 */
@Preview(widthDp = 540, heightDp = 960)
@Composable
private fun WelcomePreview() {
    var lang by remember { mutableStateOf(KioskLanguage.Thai) }
    KioskScaleProvider {
        WelcomeScreen(
            language = lang,
            onLanguageChange = { lang = it },
            onEnterHn = {},
            onFaceScan = {},
        )
    }
}
