package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GamePhase
import com.example.game.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.theme.SynthBackground
import com.example.ui.theme.SynthTilesTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SynthTilesTheme {
        SynthTilesApp()
      }
    }
  }
}

@Composable
fun SynthTilesApp(viewModel: GameViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val records by viewModel.allRecords.collectAsStateWithLifecycle()
  val settings by viewModel.gameSettings.collectAsStateWithLifecycle()

  // Handle system back button
  BackHandler(enabled = uiState.phase != GamePhase.MENU) {
    if (uiState.phase == GamePhase.PLAYING) {
      viewModel.pauseGame()
    } else {
      viewModel.quitToMenu()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = SynthBackground
  ) { _ ->
    when (uiState.phase) {
      GamePhase.MENU -> {
        MenuScreen(
          records = records,
          settings = settings,
          onStartTrack = { track -> viewModel.startGame(track) },
          onSaveSettings = { speed, haptics, bgm, sfx ->
            viewModel.updateSettings(speed, haptics, bgm, sfx)
          }
        )
      }
      GamePhase.PLAYING,
      GamePhase.PAUSED,
      GamePhase.GAME_OVER,
      GamePhase.VICTORY -> {
        GameScreen(
          state = uiState,
          tiles = viewModel.currentTiles,
          particles = viewModel.particles,
          speedMultiplier = settings?.speedMultiplier ?: 1.0f,
          onGameTick = { deltaMs -> viewModel.updateGameLoop(deltaMs) },
          onLaneDown = { lane, screenW, hitY -> viewModel.onLaneTouchDown(lane, screenW, hitY) },
          onLaneUp = { lane -> viewModel.onLaneTouchUp(lane) },
          onPause = { viewModel.pauseGame() },
          onResume = { viewModel.resumeGame() },
          onRestart = { viewModel.restartGame() },
          onQuitToMenu = { viewModel.quitToMenu() },
          onRevive = { viewModel.revive() }
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SynthTilesTheme { Greeting("Synth Tiles") }
}

