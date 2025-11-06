# 設計ドキュメント

## 概要

Androidメトロノームアプリは、Kotlinで開発されるシンプルなメトロノームアプリケーションです。ユーザーがBPMを設定し、プレイボタンで再生を制御できる機能を提供します。アプリはAndroidのMediaPlayerとHandlerを使用して正確なタイミングでオーディオビートを生成します。

## アーキテクチャ

### アプリケーション構造
- **MVVM（Model-View-ViewModel）パターン**を採用
- **Single Activity**アーキテクチャ
- **Jetpack Compose**を使用したモダンなUI

### 主要コンポーネント
```
MainActivity
├── MetronomeViewModel (ビジネスロジック)
├── MetronomeScreen (UI Composable)
└── AudioManager (オーディオ再生管理)
```

## コンポーネントとインターフェース

### 1. MainActivity
- アプリのエントリーポイント
- MetronomeScreenを表示
- システム権限の管理

### 2. MetronomeViewModel
```kotlin
class MetronomeViewModel : ViewModel() {
    // 状態管理
    private val _bpm = MutableStateFlow(120)
    private val _isPlaying = MutableStateFlow(false)
    
    // 公開プロパティ
    val bpm: StateFlow<Int>
    val isPlaying: StateFlow<Boolean>
    
    // メソッド
    fun setBpm(newBpm: Int)
    fun togglePlayback()
    fun startMetronome()
    fun stopMetronome()
}
```

### 3. MetronomeScreen (Composable)
```kotlin
@Composable
fun MetronomeScreen(viewModel: MetronomeViewModel) {
    // UI要素
    // - BPM表示とスライダー
    // - プレイ/ストップボタン
    // - 視覚的フィードバック
}
```

### 4. AudioManager
```kotlin
class AudioManager {
    private var mediaPlayer: MediaPlayer?
    private var handler: Handler
    private var beatRunnable: Runnable?
    
    fun playBeat()
    fun startBeating(intervalMs: Long)
    fun stopBeating()
    fun release()
}
```

## データモデル

### MetronomeState
```kotlin
data class MetronomeState(
    val bpm: Int = 120,
    val isPlaying: Boolean = false,
    val beatInterval: Long = calculateInterval(bpm)
) {
    companion object {
        fun calculateInterval(bpm: Int): Long = 60000L / bpm
        const val MIN_BPM = 40
        const val MAX_BPM = 300
    }
}
```

## UI設計

### レイアウト構造
```
Column (中央配置) {
    // BPM表示
    Text("120 BPM")
    
    // BPMスライダー
    Slider(40..300)
    
    // プレイボタン
    FloatingActionButton {
        Icon(Play/Pause)
    }
    
    // 視覚的ビートインジケーター
    Circle(アニメーション付き)
}
```

### デザイン仕様
- **Material Design 3**準拠
- **ダークテーマ**対応
- **アクセシビリティ**対応（TalkBack、大きなタッチターゲット）
- **レスポンシブデザイン**（様々な画面サイズに対応）

## オーディオシステム

### ビート生成メカニズム
1. **MediaPlayer**でビート音（クリック音）を再生
2. **Handler + Runnable**で正確なタイミング制御
3. **SystemClock.uptimeMillis()**で高精度タイミング

### オーディオファイル
- **assets/click.wav** - 短いクリック音（約50ms）
- **44.1kHz, 16bit, モノラル**形式

### タイミング精度
```kotlin
private fun scheduleNextBeat() {
    val nextBeatTime = lastBeatTime + beatInterval
    val delay = nextBeatTime - SystemClock.uptimeMillis()
    handler.postDelayed(beatRunnable, max(0, delay))
}
```

## エラーハンドリング

### 入力検証
- BPM範囲外の値の処理
- 無効な文字入力の防止
- UI状態の整合性チェック

### オーディオエラー
```kotlin
try {
    mediaPlayer.start()
} catch (e: IllegalStateException) {
    // MediaPlayer状態エラーの処理
    resetAudioPlayer()
} catch (e: IOException) {
    // オーディオファイル読み込みエラー
    showErrorMessage("オーディオの初期化に失敗しました")
}
```

### ライフサイクル管理
- **onPause()**: メトロノーム一時停止
- **onDestroy()**: リソース解放
- **Configuration Changes**: 状態保持

## パフォーマンス最適化

### メモリ管理
- MediaPlayerの適切な解放
- Composableの再コンポジション最適化
- ViewModelのライフサイクル管理

### バッテリー効率
- 不要なバックグラウンド処理の回避
- 効率的なタイマー実装
- CPU使用率の最小化

## テスト戦略

### 単体テスト
- ViewModelのビジネスロジック
- BPM計算の正確性
- 状態遷移の検証

### UIテスト
- Compose UIテスト
- ユーザーインタラクションの検証
- アクセシビリティテスト

### 統合テスト
- オーディオ再生の動作確認
- タイミング精度の検証
- ライフサイクルイベントの処理

## 技術スタック

### 開発環境
- **Android Studio Hedgehog**以降
- **Kotlin 1.9+**
- **Compose BOM 2023.10+**

### 主要ライブラリ
```gradle
dependencies {
    implementation "androidx.compose.ui:ui"
    implementation "androidx.compose.material3:material3"
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose"
    implementation "androidx.activity:activity-compose"
}
```

### 最小システム要件
- **Android API Level 24** (Android 7.0)
- **RAM**: 2GB以上推奨
- **ストレージ**: 10MB