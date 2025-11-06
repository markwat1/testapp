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
- **Android Studio**: Flamingo (2022.2.1) 以降
- **JDK**: 11 以降 (Java 17推奨)
- **Android SDK**: API Level 24 (Android 7.0) 以降
- **Gradle**: 8.0 以降

### 必要な環境変数
- **ANDROID_HOME**: Android SDKのパス (例: `/home/user/Android/Sdk`)
- **JAVA_HOME**: JDKのパス (例: `/usr/lib/jvm/java-17-openjdk-amd64`)

### 実行環境
- **Android デバイス**: Android 7.0 (API Level 24) 以降
- **RAM**: 最低 2GB 推奨
- **ストレージ**: 約 50MB の空き容量

## 開発環境のセットアップ

### 1. Android Studioのインストール

1. **Android Studioのダウンロード**
   - [Android Studio公式サイト](https://developer.android.com/studio)からダウンロード
   - インストール後、初回セットアップウィザードを完了

2. **Android SDKの設定**
   - Android Studio起動後、SDK Managerを開く
   - Android 13 (API Level 33) 以降をインストール
   - Android SDK Build-Tools 33.0.0 以降をインストール

3. **環境変数の設定**
   ```bash
   # ~/.bashrc または ~/.zshrc に追加
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/emulator
   export PATH=$PATH:$ANDROID_HOME/tools
   export PATH=$PATH:$ANDROID_HOME/tools/bin
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   ```

### 2. Java開発環境の設定

```bash
# Java 17のインストール (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-17-jdk

# JAVA_HOMEの設定
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

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

3. **ローカル設定ファイルの作成**
   ```bash
   # local.properties.template をコピーして設定
   cp local.properties.template local.properties
   # local.properties を編集してAndroid SDKのパスを設定
   ```

4. **依存関係の同期**
   - Android Studioが自動的にGradleの同期を開始
   - 同期が完了するまで待機

5. **デバイスの準備**
   
   **実機の場合:**
   - デバイスの設定で「開発者オプション」を有効化
   - 「USBデバッグ」を有効化
   - USBケーブルでPCに接続
   
   **エミュレーターの場合:**
   - Android StudioのAVD Managerでエミュレーターを作成
   - API Level 24以降のシステムイメージを選択

6. **アプリのビルドとインストール**
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
- **Target SDK**: 33 (Android 13)
- **Compile SDK**: 33
- **Kotlin**: 1.8.10
- **Compose**: 2023.03.00
- **Android Gradle Plugin**: 7.4.2

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

**4. Android SDK エラー**
```
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable
```
このエラーが発生した場合：
- Android Studioをインストールして初回セットアップを完了
- ANDROID_HOMEを設定: `export ANDROID_HOME=/path/to/Android/Sdk`
- または `local.properties` ファイルに追加: `sdk.dir=/path/to/Android/Sdk`

**5. Java バージョン エラー**
```
Android Gradle plugin requires Java 17 to run. You are currently using Java 11.
```
このエラーが発生した場合：
- Java 17をインストール: `sudo apt install openjdk-17-jdk` (Ubuntu/Debian)
- JAVA_HOMEを設定: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`
- または `gradle.properties` に追加: `org.gradle.java.home=/path/to/java17`

**6. Kotlin Compose Plugin エラー**
```
Plugin [id: 'org.jetbrains.kotlin.plugin.compose'] was not found
```
このエラーが発生した場合：
- Android Studio Flamingo (2022.2.1) 以降を使用していることを確認
- プロジェクトの `build.gradle.kts` ファイルから該当プラグインを削除
- Compose は `buildFeatures { compose = true }` で有効化

**7. Gradle Wrapper エラー**
```
メイン・クラスorg.gradle.wrapper.GradleWrapperMainを検出およびロードできませんでした
```
このエラーが発生した場合：
- Gradle Wrapper JARファイルが不足している可能性があります
- Android Studioで「File > Sync Project with Gradle Files」を実行
- または以下のコマンドでWrapper を再生成:
```bash
gradle wrapper --gradle-version 8.4
```

**8. エミュレーターで音が出ない**
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

## プロジェクトの状態

このプロジェクトは完全に実装されており、以下の機能が含まれています：

- ✅ **完全なソースコード**: すべてのKotlinファイルが実装済み
- ✅ **UI実装**: Jetpack Composeを使用したモダンなUI
- ✅ **単体テスト**: ViewModelとStateクラスの包括的なテスト
- ✅ **UIテスト**: Composeコンポーネントのテスト
- ✅ **アクセシビリティ対応**: TalkBack対応とユーザビリティ機能
- ✅ **ドキュメント**: 詳細なREADMEとセットアップガイド

**注意**: このプロジェクトをビルドするには、適切なAndroid開発環境のセットアップが必要です。

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