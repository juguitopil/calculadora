package com.tuapp.calculadora

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.tuapp.calculadora.ui.theme.CalculadoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Reproductor de video de fondo responsivo con soporte para video personalizado desde la galería
 * y control opcional de silencio (Mute/Unmute).
 */
@Composable
fun BackgroundVideoPlayer(
    videoName: String,
    customVideoUri: Uri?,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    defaultVolume: Float = 0.15f
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    val currentVolume = if (isMuted) 0f else defaultVolume

    val videoResId = if (customVideoUri == null) {
        context.resources.getIdentifier(videoName, "raw", context.packageName)
    } else 1

    LaunchedEffect(isMuted) {
        mediaPlayerRef?.setVolume(currentVolume, currentVolume)
    }

    if (isPreview || (customVideoUri == null && videoResId == 0)) {
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3A0D56),
                        Color(0xFF13092D),
                        Color(0xFF080214)
                    )
                )
            )
        )
    } else {
        AndroidView(
            factory = { ctx ->
                object : VideoView(ctx) {
                    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                        val width = getDefaultSize(0, widthMeasureSpec)
                        val height = getDefaultSize(0, heightMeasureSpec)
                        setMeasuredDimension(width, height)
                    }
                }.apply {
                    if (customVideoUri != null) {
                        setVideoURI(customVideoUri)
                    } else {
                        val videoUri = Uri.parse("android.resource://${ctx.packageName}/$videoResId")
                        setVideoURI(videoUri)
                    }
                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        mp.isLooping = true
                        mp.setVolume(currentVolume, currentVolume)
                        start()
                    }
                }
            },
            update = {
                mediaPlayerRef?.setVolume(currentVolume, currentVolume)
            },
            modifier = modifier
        )
    }
}

/**
 * Diálogo de configuración para seleccionar videos y sonidos personalizados desde la galería del teléfono.
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onSelectVideo: () -> Unit,
    onSelectAudio: () -> Unit,
    onResetDefaults: () -> Unit,
    hasCustomVideo: Boolean,
    hasCustomAudio: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xEE1E1E28),
        title = {
            Text("Personalizar Multimedia", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Elige un video de fondo o un sonido personalizado desde la galería de tu teléfono.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Button(
                    onClick = onSelectVideo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (hasCustomVideo) " Cambiar Video de Fondo" else " Elegir Video de Fondo", color = Color.White)
                }

                Button(
                    onClick = onSelectAudio,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (hasCustomAudio) " Cambiar Sonido Personalizado" else " Elegir Sonido de Botón", color = Color.White)
                }

                if (hasCustomVideo || hasCustomAudio) {
                    TextButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Restaurar predeterminados", color = Color(0xFFFF5252))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color(0xFFFF9F0A), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val calculator = remember { CalculatorLogic() }
    var displayText by remember { mutableStateOf("0") }
    var expressionText by remember { mutableStateOf("") }

    // Estados para multimedia personalizada desde la galería
    var customVideoUri by remember { mutableStateOf<Uri?>(null) }
    var customAudioUri by remember { mutableStateOf<Uri?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Bloqueos opcionales (Mute/Unmute) para video de fondo y sonidos de botones
    var isBgMuted by remember { mutableStateOf(false) }
    var isSfxMuted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val view = LocalView.current

    // Launchers para abrir la galería del teléfono (Videos y Audios)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            customVideoUri = uri
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            customAudioUri = uri
        }
    }

    // Reproductor de sonido personalizado (prioriza URI de galería, sino usa res/raw/)
    fun playSound(symbol: String) {
        if (isSfxMuted) return

        view.playSoundEffect(SoundEffectConstants.CLICK)

        try {
            if (customAudioUri != null) {
                val mediaPlayer = MediaPlayer.create(context, customAudioUri)
                mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                mediaPlayer?.start()
            } else {
                val soundName = when (symbol) {
                    "=" -> "yamade_kudasai"
                    "C" -> "cat"
                    "DEL" -> "mikudayo_made_with_voicemod"
                    "%" -> "miracle333_ara_ara_sound_effect_127279"
                    "-", "÷" -> "onin_chan"
                    "×" -> "uwu"
                    "+" -> "baka"
                    else -> "mambo"
                }

                val soundResId = context.resources.getIdentifier(soundName, "raw", context.packageName)
                if (soundResId != 0) {
                    val mediaPlayer = MediaPlayer.create(context, soundResId)
                    mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                    mediaPlayer?.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = maxHeight

        // Cálculos dinámicos de responsividad según el alto de la pantalla del dispositivo
        val buttonHeight: Dp = (screenHeight * 0.082f).coerceIn(48.dp, 76.dp)
        val buttonFontSize: TextUnit = if (screenHeight < 600.dp) 18.sp else 22.sp
        val displayFontSize: TextUnit = if (screenHeight < 600.dp) 32.sp else 44.sp
        val rowPadding: Dp = if (screenHeight < 600.dp) 3.dp else 5.dp

        // 1. Video de fondo (personalizado de galería o predeterminado "f") con control de silencio
        BackgroundVideoPlayer(
            videoName = "f",
            customVideoUri = customVideoUri,
            modifier = Modifier.fillMaxSize(),
            isMuted = isBgMuted,
            defaultVolume = 0.15f
        )

        // 2. Capa de tinte oscuro para mejor contraste y legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )

        // 3. Contenido Principal de la Calculadora
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Barra superior con botones de configuración (Galería) y bloqueo de audio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para abrir el selector de galería (Video / Audio)
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ajustes_svg),
                        contentDescription = "Ajustes",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de bloqueo para la música/video de fondo
                IconButton(
                    onClick = { isBgMuted = !isBgMuted },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isBgMuted) Color(0x66FF5252) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_musica_fondo),
                        contentDescription = "Bloquear Música de fondo",
                        tint = if (isBgMuted) Color(0xFFFF8A80) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de bloqueo para los tonos de los botones
                IconButton(
                    onClick = { isSfxMuted = !isSfxMuted },
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = if (isSfxMuted) Color(0x66FF5252) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tono_svg),
                        contentDescription = "Bloquear Tonos de botones",
                        tint = if (isSfxMuted) Color(0xFFFF8A80) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Pantalla de salida con alta transparencia (Efecto Cristal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(
                        color = Color(0x3312121A), // Alta transparencia (20% de opacidad)
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.30f)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    // Historial / Expresión en curso
                    Text(
                        text = expressionText,
                        fontSize = (displayFontSize.value * 0.48f).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.70f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Número actual / Resultado
                    Text(
                        text = displayText,
                        fontSize = displayFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            // Cuadrícula de Botones Transparentes Responsivos
            val buttons = listOf(
                listOf("C", "DEL", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            for (row in buttons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = rowPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (symbol in row) {
                        val weight = if (symbol == "=") 2f else 1f

                        CalculatorButton(
                            symbol = symbol,
                            buttonHeight = buttonHeight,
                            fontSize = buttonFontSize,
                            modifier = Modifier.weight(weight)
                        ) {
                            playSound(symbol)

                            displayText = when (symbol) {
                                "C" -> calculator.onClearClick()
                                "DEL" -> calculator.onDeleteClick()
                                "+", "-", "×", "÷", "%" -> calculator.onOperatorClick(symbol)
                                "=" -> calculator.onEqualsClick()
                                else -> calculator.onNumberClick(symbol)
                            }
                            expressionText = calculator.expression
                        }
                    }
                }
            }
        }
    }

    // Diálogo de configuración para elegir multimedia de la galería
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onSelectVideo = { videoPickerLauncher.launch("video/*") },
            onSelectAudio = { audioPickerLauncher.launch("audio/*") },
            onResetDefaults = {
                customVideoUri = null
                customAudioUri = null
                showSettingsDialog = false
            },
            hasCustomVideo = customVideoUri != null,
            hasCustomAudio = customAudioUri != null
        )
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    buttonHeight: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isEquals = symbol == "="
    val isOperator = symbol in listOf("÷", "×", "-", "+")
    val isControl = symbol in listOf("C", "DEL", "%")

    val backgroundColor = when {
        isEquals -> Color(0x40FFFFFF)
        isControl || isOperator -> Color(0x33FFFFFF)
        else -> Color(0x22FFFFFF)
    }

    val borderColor = when {
        isEquals -> Color.White.copy(alpha = 0.85f)
        isOperator -> Color.White.copy(alpha = 0.45f)
        else -> Color.White.copy(alpha = 0.30f)
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(buttonHeight),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, borderColor),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    ) {
        Text(
            text = symbol,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    CalculadoraTheme {
        CalculatorScreen()
    }
}