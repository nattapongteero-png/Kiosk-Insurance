package th.go.banlat.kiosk.ui.service

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import th.go.banlat.kiosk.ui.theme.KioskScaleProvider

/** ดูผลใน Android Studio ได้เลย ไม่ต้องต่อ API */
@Preview(widthDp = 540, heightDp = 960, showBackground = true)
@Composable
private fun ServiceSelectionPreview() {
    KioskScaleProvider {
        ServiceSelectionScreen(
            patient = SampleData.patient,
            services = SampleData.services,
            idleSecondsLeft = 43,
            idleTimeoutSeconds = 60,
            onSelect = {},
            onCancel = {},
        )
    }
}

/** เคสปุ่มน้อย ตรวจว่าหน้าจอไม่โหว่ */
@Preview(widthDp = 540, heightDp = 960, showBackground = true)
@Composable
private fun ServiceSelectionFewPreview() {
    KioskScaleProvider {
        ServiceSelectionScreen(
            patient = SampleData.patient,
            services = SampleData.services.filterIndexed { i, _ -> i == 3 || i == 4 || i == 7 },
            idleSecondsLeft = 58,
            idleTimeoutSeconds = 60,
            onSelect = {},
            onCancel = {},
        )
    }
}
