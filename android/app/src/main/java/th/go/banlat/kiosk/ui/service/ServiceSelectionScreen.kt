package th.go.banlat.kiosk.ui.service

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import th.go.banlat.kiosk.model.PatientInfo
import th.go.banlat.kiosk.model.ServiceGroup
import th.go.banlat.kiosk.model.ServiceItem
import th.go.banlat.kiosk.ui.theme.KioskColor
import th.go.banlat.kiosk.ui.theme.ServiceAccent
import th.go.banlat.kiosk.ui.theme.s
import th.go.banlat.kiosk.ui.theme.st

/**
 * หน้าเลือกบริการ
 *
 * หลักการที่ต่างจาก UI เดิม
 *  1. ไม่มีภาพพื้นหลังที่ฝังปุ่ม/ข้อความ — ทุกอย่างวาดด้วยโค้ด
 *  2. ปุ่มมากี่ปุ่มก็ได้ จัด grid เองอัตโนมัติ ปุ่มสุดท้ายที่เหลือเดี่ยวจะขยายเต็มแถว
 *  3. จัดกลุ่มบริการ แทนการใช้สีปุ่มสื่อความสำคัญแบบมั่ว
 */
@Composable
fun ServiceSelectionScreen(
    patient: PatientInfo,
    services: List<ServiceItem>,
    idleSecondsLeft: Int,
    idleTimeoutSeconds: Int,
    onSelect: (ServiceItem) -> Unit,
    onCancel: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(KioskColor.Canvas)) {

        KioskHeader()

        PatientCard(
            patient = patient,
            modifier = Modifier.padding(start = 40.s, top = 28.s, end = 40.s)
        )

        Column(Modifier.padding(start = 40.s, top = 44.s, end = 40.s, bottom = 8.s)) {
            Text(
                "เลือกบริการที่ต้องการ",
                fontSize = 64.st, fontWeight = FontWeight.Bold, color = KioskColor.Ink900
            )
            Spacer(Modifier.height(8.s))
            Text("แตะที่ปุ่มบริการเพื่อรับบัตรคิว", fontSize = 30.st, color = KioskColor.Ink600)
        }

        // ---- รายการบริการ เลื่อนได้เมื่อปุ่มเยอะเกินจอ ----
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 40.s, end = 40.s, top = 24.s, bottom = 40.s
            ),
            verticalArrangement = Arrangement.spacedBy(36.s)
        ) {
            ServiceGroup.entries.forEach { group ->
                val inGroup = services.filter { it.group == group }
                if (inGroup.isEmpty()) return@forEach

                item(key = "header-${group.name}") { GroupHeader(group.title) }

                // จับคู่ทีละ 2 ปุ่มเป็นหนึ่งแถว ตัวที่เหลือเดี่ยวให้กินเต็มแถว
                val rows = inGroup.chunked(2)
                items(rows, key = { it.first().code }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.s)) {
                        row.forEach { item ->
                            ServiceCard(
                                item = item,
                                onClick = { onSelect(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item(key = "help") { HelpBanner() }
        }

        KioskFooter(
            idleSecondsLeft = idleSecondsLeft,
            idleTimeoutSeconds = idleTimeoutSeconds,
            onCancel = onCancel
        )
    }
}

/* ------------------------------------------------------------------ */

@Composable
private fun KioskHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(KioskColor.Surface)
            .padding(horizontal = 40.s, vertical = 28.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(28.s)
    ) {
        // โลโก้โรงพยาบาล — ไฟล์ภาพจริงใส่ตรงนี้ (Image(painterResource(R.drawable.logo)))
        Box(
            Modifier
                .size(116.s)
                .clip(RoundedCornerShape(24.s))
                .background(KioskColor.Brand050)
        )
        Column {
            Text(
                "โรงพยาบาลบ้านลาด",
                fontSize = 52.st, fontWeight = FontWeight.Bold, color = KioskColor.Brand900
            )
            Text(
                "BANLAT HOSPITAL",
                fontSize = 26.st, fontWeight = FontWeight.SemiBold, color = KioskColor.Brand700
            )
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "ระบบลงทะเบียนอัตโนมัติ",
                fontSize = 30.st, fontWeight = FontWeight.SemiBold, color = KioskColor.Ink600
            )
        }
    }
    Box(Modifier.fillMaxWidth().height(6.s).background(KioskColor.Brand700))
}

@Composable
private fun PatientCard(patient: PatientInfo, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.s))
            .background(KioskColor.Surface)
            .border(1.s, KioskColor.Line, RoundedCornerShape(32.s))
            .padding(32.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.s)
    ) {
        // รูปจากบัตรประชาชน
        Box(
            Modifier
                .width(150.s).height(186.s)
                .clip(RoundedCornerShape(20.s))
                .background(KioskColor.Brand050)
        )
        Column {
            Text(
                patient.fullName,
                fontSize = 54.st, fontWeight = FontWeight.Bold, color = KioskColor.Ink900
            )
            Spacer(Modifier.height(6.s))
            Text(
                "เลขบัตรประชาชน ${patient.maskedCitizenId}",
                fontSize = 30.st, color = KioskColor.Ink600
            )
            Spacer(Modifier.height(20.s))
            Row(horizontalArrangement = Arrangement.spacedBy(14.s)) {
                InfoChip("HN", patient.hn, KioskColor.Brand050, KioskColor.Brand700)
                InfoChip("อายุ", patient.ageText, KioskColor.Muted, KioskColor.Ink600)
                InfoChip("สิทธิ", patient.insuranceRight, KioskColor.Teal050, KioskColor.Teal600)
            }
        }
    }
}

@Composable
private fun InfoChip(key: String, value: String, bg: Color, fg: Color) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 24.s, vertical = 12.s),
        horizontalArrangement = Arrangement.spacedBy(10.s)
    ) {
        Text(key, fontSize = 28.st, fontWeight = FontWeight.Medium, color = fg.copy(alpha = .75f))
        Text(value, fontSize = 28.st, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun GroupHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.s)) {
        Text(title, fontSize = 30.st, fontWeight = FontWeight.SemiBold, color = KioskColor.Ink400)
        Box(Modifier.weight(1f).height(2.s).background(KioskColor.Line))
    }
}

@Composable
private fun ServiceCard(item: ServiceItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = ServiceAccent.from(item.accent)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(24.s)

    Row(
        modifier
            .heightIn(min = 224.s)
            .clip(shape)
            .background(KioskColor.Surface)
            .border(
                width = if (pressed) 4.s else 1.s,
                color = if (pressed) accent.accent else KioskColor.Line,
                shape = shape
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // แถบสี accent ด้านซ้าย บอกหมวดโดยไม่ต้องย้อมทั้งปุ่ม
        Box(Modifier.width(12.s).fillMaxHeight().background(accent.accent))

        Row(
            Modifier.weight(1f).padding(32.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.s)
        ) {
            Box(
                Modifier.size(110.s).clip(RoundedCornerShape(26.s)).background(accent.tint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    ServiceIcons.of(item.icon), contentDescription = null,
                    tint = accent.accent, modifier = Modifier.size(62.s)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    item.label,
                    fontSize = 44.st, fontWeight = FontWeight.SemiBold, color = KioskColor.Ink900
                )
                item.note?.let {
                    Spacer(Modifier.height(8.s))
                    Text(it, fontSize = 26.st, color = KioskColor.Ink600)
                }
            }
            Box(
                Modifier.size(56.s).clip(CircleShape).background(KioskColor.Muted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ChevronRight, contentDescription = null,
                    tint = KioskColor.Ink400, modifier = Modifier.size(30.s)
                )
            }
        }
    }
}

@Composable
private fun HelpBanner() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.s))
            .background(Color(0xFFF7F9FC))
            .border(2.s, Color(0xFFCFD8E3), RoundedCornerShape(24.s))
            .padding(32.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.s)
    ) {
        Box(
            Modifier.size(84.s).clip(CircleShape).background(Color(0xFFE7EDF5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.HelpOutline, contentDescription = null,
                tint = KioskColor.Brand700, modifier = Modifier.size(46.s)
            )
        }
        Column {
            Text(
                "ไม่พบบริการที่ต้องการ?",
                fontSize = 34.st, fontWeight = FontWeight.SemiBold, color = KioskColor.Ink900
            )
            Text(
                "ติดต่อเจ้าหน้าที่ประชาสัมพันธ์ หรือกดปุ่มยกเลิกด้านล่าง",
                fontSize = 27.st, color = KioskColor.Ink600
            )
        }
    }
}

@Composable
private fun KioskFooter(idleSecondsLeft: Int, idleTimeoutSeconds: Int, onCancel: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(1.s).background(KioskColor.Line))
    Row(
        Modifier
            .fillMaxWidth()
            .background(KioskColor.Surface)
            .padding(horizontal = 40.s, vertical = 28.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(KioskColor.Danger050)
                .border(2.s, KioskColor.Danger300, CircleShape)
                .clickable(onClick = onCancel)
                .padding(horizontal = 56.s, vertical = 26.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.s)
        ) {
            Icon(
                Icons.Outlined.Close, contentDescription = null,
                tint = KioskColor.Danger600, modifier = Modifier.size(34.s)
            )
            Text(
                "ยกเลิกบริการ",
                fontSize = 38.st, fontWeight = FontWeight.SemiBold, color = KioskColor.Danger600
            )
        }

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "กลับหน้าแรกอัตโนมัติใน $idleSecondsLeft วินาที",
                fontSize = 28.st, color = KioskColor.Ink600, textAlign = TextAlign.End
            )
            Spacer(Modifier.height(12.s))
            LinearProgressIndicator(
                progress = { idleSecondsLeft.toFloat() / idleTimeoutSeconds },
                modifier = Modifier.width(320.s).height(10.s).clip(CircleShape),
                color = KioskColor.Brand500,
                trackColor = KioskColor.Line,
            )
        }
    }
}
