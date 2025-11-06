# Android Metronome App

シンプルで使いやすいAndroidメトロノームアプリです。正確なタイミングでビートを刻み、音楽練習をサポートします。

## 機能

- **BPM設定**: 40-300 BPMの範囲で自由に設定可能
- **正確なタイミング**: 高精度なビート再生
- **直感的なUI**: Material Design 3を採用したモダンなインターフェース
- **アクセシビリティ対応**: TalkBack対応で視覚障害者にも配慮
- **視覚的フィードバック**: ビートに合わせたアニメーション効果

## 必要な環境

### 開発環境
- **Android Studio**: Hedgehog (2023.1.1) 以降
- **JDK**: 8 以降
- **Android SDK**: API Level 24 (Android 7.0) 以降

### 実行環境
- **Android デバイス**: Android 7.0 (API Level 24) 以降
- **RAM**: 最低 2GB 推奨
- **ストレージ**: 約 50MB の空き容量

## インストール方法

### 方法1: Android Studioを使用した開発者インストール

1. **プロジェクトのクローン**
   ```bash
   git clone <repository-url>
   cd android-metronome
   ```

2. **Android Studioでプロジェクトを開く**
   - Android Studioを起動
   - "Open an Existing Project"を選択
   - プロジェクトフォルダを選択

3. **依存関係の同期**
   - Android Studioが自動的にGradleの同期を開始
   - 同期が完了するまで待機

4. **デバイスの準備**
   
   **実機の場合:**
   - デバイスの設定で「開発者オプション」を有効化
   - 「USBデバッグ」を有効化
   - USBケーブルでPCに接続
   
   **エミュレーターの場合:**
   - Android StudioのAVD Managerでエミュレーターを作成
   - API Level 24以降のシステムイメージを選択

5. **アプリのビルドとインストール**
   - Android Studioの「Run」ボタン（緑の三角形）をクリック
   - または `Shift + F10` キーを押下
   - ターゲットデバイスを選択してOK

### 方法2: APKファイルからの直接インストール

1. **APKファイルのビルド**
   ```bash
   ./gradlew assembleDebug
   ```
   
2. **APKファイルの場所**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

3. **デバイスへのインストール**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 使用方法

### 基本操作

1. **BPMの設定**
   - スライダーをドラッグしてBPMを調整
   - 「-5」「+5」ボタンで細かい調整が可能
   - 範囲: 40-300 BPM

2. **メトロノームの開始/停止**
   - 中央の大きな再生ボタンをタップ
   - 再生中は一時停止ボタンに変化
   - 再度タップで停止

3. **視覚的フィードバック**
   - 上部の円がビートに合わせて拡大/縮小
   - ボタンの色が再生状態に応じて変化
   - ステータスカードで現在の状態を確認

### アクセシビリティ機能

- **TalkBack対応**: 全ての操作要素に音声説明を追加
- **大きなタッチターゲット**: 操作しやすいボタンサイズ
- **明確な状態表示**: 現在の設定と状態を音声で確認可能

## 開発者向け情報

### プロジェクト構造

```
app/src/main/java/com/example/metronome/
├── MainActivity.kt              # メインアクティビティ
├── MetronomeScreen.kt          # メインUI画面
├── MetronomeViewModel.kt       # ビジネスロジック
├── MetronomeState.kt          # 状態管理
└── AudioManager.kt            # オーディオ制御
```

### テストの実行

**単体テスト:**
```bash
./gradlew testDebugUnitTest
```

**UIテスト:**
```bash
./gradlew connectedDebugAndroidTest
```

### ビルド設定

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Kotlin**: 1.9.10
- **Compose**: 2023.10.01

## トラブルシューティング

### よくある問題と解決方法

**1. アプリが起動しない**
- デバイスのAndroidバージョンが7.0以降であることを確認
- ストレージ容量が十分であることを確認
- アプリを一度アンインストールして再インストール

**2. 音が出ない**
- デバイスの音量設定を確認
- サイレントモードが無効になっていることを確認
- 他のアプリが音声を占有していないか確認

**3. ビルドエラーが発生する**
- Android StudioとSDKが最新版であることを確認
- プロジェクトをクリーンしてリビルド: `./gradlew clean build`
- Gradleキャッシュをクリア: `./gradlew --refresh-dependencies`

**4. エミュレーターで音が出ない**
- エミュレーターの音声設定を確認
- ホストPCの音量設定を確認
- エミュレーターを再起動

### パフォーマンス最適化

- **バッテリー使用量**: バックグラウンドでの自動停止機能
- **メモリ使用量**: 効率的なリソース管理
- **CPU使用量**: 最適化されたタイミング制御

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## 貢献

バグ報告や機能要望は、GitHubのIssuesページでお知らせください。
プルリクエストも歓迎します。

## 更新履歴

### v1.0.0
- 初回リリース
- 基本的なメトロノーム機能
- Material Design 3 UI
- アクセシビリティ対応
- 包括的なテストスイート

## サポート

技術的な質問やサポートが必要な場合は、以下の方法でお問い合わせください：

- GitHub Issues: バグ報告や機能要望
- Email: [開発者メールアドレス]

---

**注意**: このアプリは音楽練習用のツールです。正確なタイミングが重要な場面では、専用のハードウェアメトロノームの使用も検討してください。