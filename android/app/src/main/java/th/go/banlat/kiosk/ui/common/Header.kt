package th.go.banlat.kiosk.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import th.go.banlat.kiosk.R
import th.go.banlat.kiosk.data.thaiDate
import th.go.banlat.kiosk.ui.theme.K
import th.go.banlat.kiosk.ui.theme.KText
import th.go.banlat.kiosk.ui.theme.s
import java.time.LocalDateTime

/** ตรา BMS 88 + "Smart Hospital Kiosk" / "ระบบลงทะเบียนอัตโนมัติ" (หน้าแรกและหน้าประกันใช้ชุดเดียวกัน) */
@Composable
fun BrandBlock(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.s)) {
        Box(
            Modifier.size(88.s)
                .softShadow(44f, Shade(Color(0x1F14265A), 10f, 30f))
                .clip(CircleShape).background(Color.White).border(1.5.s, Color(0xF2FFFFFF), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(R.drawable.logo_bms), "BMS Group", Modifier.size(72.s), contentScale = ContentScale.Fit)
        }
        Column {
            KText("Smart Hospital Kiosk", 40, weight = FontWeight.Bold, lineHeight = 44f, softWrap = false)
            KText("ระบบลงทะเบียนอัตโนมัติ", 28, weight = FontWeight.SemiBold, color = K.Blue, lineHeight = 44f, letterSpacing = 1f, softWrap = false)
        }
    }
}

/** วันที่ไทย (พ.ศ.) + เวลา อัปเดตทุกวินาที */
@Composable
fun ClockBlock(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(1000L - now.nano / 1_000_000) } }
    Column(modifier, horizontalAlignment = Alignment.End) {
        KText(thaiDate(now.toLocalDate()), 28, weight = FontWeight.Medium, color = K.InkMuted, lineHeight = 44f, align = TextAlign.End, softWrap = false)
        KText("%02d:%02d:%02d".format(now.hour, now.minute, now.second), 46, weight = FontWeight.Bold,
            lineHeight = 44f, letterSpacing = 1f, tabular = true, align = TextAlign.End, softWrap = false)
    }
}
