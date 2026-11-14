package com.kartik.pocketagent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault()
        }
        setContent { PocketAgentScreen(
            onCommand = { command -> AgentActions.run(this, command) },
            onSpeak = { text -> tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pocket-agent") },
            onFloating = {
                if (!Settings.canDrawOverlays(this)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else startService(Intent(this, FloatingAgentService::class.java))
            }
        ) }
    }
    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}

@Composable
private fun PocketAgentScreen(
    onCommand: (String) -> String,
    onSpeak: (String) -> Unit,
    onFloating: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("Hi. I'm Pocket Agent. Try: “open YouTube”, “open Wi-Fi settings”, or “search for…”.") }
    var showKey by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val messages = remember { mutableStateListOf("Agent: $answer") }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) input = spoken
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Pocket Agent")
            }
            speechLauncher.launch(intent)
        }
    }
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pocket Agent", style = MaterialTheme.typography.headlineMedium)
                Text("Personal Android assistant · v0.1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Card(Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(messages) { line -> Text(line, style = MaterialTheme.typography.bodyLarge) }
                    }
                }
                OutlinedTextField(
                    value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tell your agent what to do") }, maxLines = 3,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                }
                                speechLauncher.launch(i)
                            } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }) { Icon(Icons.Default.Mic, "Voice input") }
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(modifier = Modifier.weight(1f), onClick = {
                        val command = input.trim()
                        if (command.isNotEmpty()) {
                            messages.add("You: $command")
                            val result = onCommand(command)
                            answer = result
                            messages.add("Agent: $result")
                            onSpeak(result)
                            input = ""
                        }
                    }) { Icon(Icons.Default.Send, null); Spacer(Modifier.width(6.dp)); Text("Run command") }
                    OutlinedButton(onClick = onFloating) { Text("Floating button") }
                }
                TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Hide AI setup" else "AI setup (coming next)") }
                if (showKey) {
                    Text("Gemini API connection is the next development step. Do not paste an API key into a public repository.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
