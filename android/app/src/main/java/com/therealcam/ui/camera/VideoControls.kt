package com.therealcam.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoStable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.therealcam.camera.VideoQuality
import com.therealcam.ui.theme.RealColors

/** Preview box: 3:4 for photos; in VIDEO 9:16, shrunk to the space the controls leave free. */
fun ColumnScope.previewArea(video: Boolean): Modifier =
    if (video) Modifier.weight(1f, fill = false).align(Alignment.CenterHorizontally).aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
    else Modifier.fillMaxWidth().aspectRatio(3f / 4f)

/** Controls below the preview: the remaining space for photos, their natural height in VIDEO. */
fun ColumnScope.controlsArea(video: Boolean): Modifier =
    if (video) Modifier.fillMaxWidth().padding(top = 8.dp) else Modifier.fillMaxWidth().weight(1f)

private fun hasMic(context: Context) =
    context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

/** VIDEO row: resolution, frame rate and stabilization; asks for the microphone on first use. */
@Composable
fun VideoControls(video: VideoSession, rot: Float) {
    val options by video.options.collectAsState()
    val spec by video.spec.collectAsState()
    val recording by video.recording.collectAsState()
    val context = LocalContext.current
    var mic by remember { mutableStateOf(hasMic(context)) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { mic = it }
    LaunchedEffect(Unit) {
        if (!mic && !video.micAsked) {
            video.micAsked = true
            ask.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val o = options
    val s = spec
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            VideoQuality.entries.filter { o == null || it in o.qualities }.forEach { q ->
                VideoChip(q.label, s?.quality == q, enabled = !recording && o != null) { video.setQuality(q) }
            }
            Spacer(Modifier.width(8.dp))
            listOf(30, 60).forEach { f ->
                val ok = f == 30 || (o != null && s != null && s.quality in o.fps60)
                VideoChip("$f к/с", s?.fps == f, enabled = !recording && ok) { video.setFps(f) }
            }
            Spacer(Modifier.width(8.dp))
            val canStab = o?.stabilization == true
            val on = s?.stabilization == true
            Box(
                Modifier.size(34.dp).clip(CircleShape)
                    .background(if (on) RealColors.Accent else RealColors.SurfaceHigh.copy(alpha = 0.9f))
                    .clickable(enabled = !recording && canStab) { video.setStabilization(!on) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.VideoStable, if (on) "Стабилизация включена" else "Стабилизация выключена",
                    tint = when {
                        on -> Color.Black
                        canStab -> Color.White
                        else -> Color.White.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(20.dp).rotate(rot),
                )
            }
        }
        if (!mic) {
            Spacer(Modifier.height(4.dp))
            Text("Без звука: нет доступа к микрофону", fontSize = 11.sp, color = RealColors.Warn)
        }
    }
}

@Composable
private fun VideoChip(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(if (selected) RealColors.Accent else RealColors.SurfaceHigh.copy(alpha = 0.9f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text, fontSize = 12.sp, maxLines = 1,
            color = when {
                selected -> Color.Black
                !enabled -> Color.White.copy(alpha = 0.35f)
                else -> Color.White
            },
        )
    }
}

/** Record button: red circle, a red rounded square (stop) while recording. */
@Composable
fun RecordButton(recording: Boolean, onClick: () -> Unit) {
    val inner by animateDpAsState(if (recording) 30.dp else 62.dp, label = "recordInner")
    val corner by animateDpAsState(if (recording) 6.dp else 31.dp, label = "recordCorner")
    Box(
        Modifier.size(78.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(inner).clip(RoundedCornerShape(corner)).background(RealColors.Bad))
    }
}

/** Blinking red dot and the elapsed time of the running recording. */
@Composable
fun RecordingBadge(elapsedS: Int, modifier: Modifier = Modifier) {
    val blink by rememberInfiniteTransition(label = "recBlink").animateFloat(
        1f, 0.2f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "recDot",
    )
    Row(
        modifier.clip(RoundedCornerShape(10.dp)).background(RealColors.Scrim).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(RealColors.Bad.copy(alpha = blink)))
        Spacer(Modifier.width(6.dp))
        val h = elapsedS / 3600
        val text = if (h > 0) "%d:%02d:%02d".format(h, elapsedS / 60 % 60, elapsedS % 60) else "%02d:%02d".format(elapsedS / 60, elapsedS % 60)
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
