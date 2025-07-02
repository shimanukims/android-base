# Android Base App
Androidのベースプロジェクト

## 🚀 プロジェクト概要
Modern Android Development (MAD) の技術スタックを活用したユーザー管理アプリのベースプロジェクトです。
Clean ArchitectureとMVVMパターンを採用し、拡張性と保守性を重視して設計されています。

## 📋 機能要件

### API
- **データソース**: [JSONPlaceholder](https://jsonplaceholder.typicode.com/) のユーザー一覧API
- **エンドポイント**: `https://jsonplaceholder.typicode.com/users`

### 画面機能
- **ユーザー一覧表示**: 基本的なリスト形式
  - カード形式（4行表示：名前、メール、住所、電話番号）
  - 文字が長い場合は3点リーダ（...）で省略
  - 空リスト時は「データがありません」を表示
  - リスト項目タップでRipple Effect表示
- **Pull to Refresh**: 手動データ更新機能（Material3デフォルト色）
  - 実行中の再実行は無視
- **ローディング表示**: データ取得中はProgressIndicatorを表示
- **詳細画面**: ユーザー詳細情報の表示（別画面遷移）
  - 表示項目：名前、メール、住所、電話番号（全文表示）
  - 縦スクロール可能
  - システムの戻るボタンで一覧画面へ

### エラーハンドリング
- **ネットワークエラー**: エラーダイアログ表示（リトライボタン付き）
- **タイムアウトエラー**: エラーダイアログ表示（リトライボタン付き）
- **オフライン時**: オフライン通知ダイアログ表示（OKボタンのみ）
- **不明なエラー**: エラーダイアログ表示（OKボタンのみ）

### データ永続化
- **Room DB**: ユーザーデータのローカルキャッシュ
  - キャッシュ有効期限: 24時間
  - バックグラウンド更新: なし
  - 起動時動作: 必ずAPIから最新データ取得
- **DataStore**: 設定やプリファレンスの保存

### テスト
- **Unit Test**: ビジネスロジックとRepository層のテスト
- **UI Test**: 画面遷移と基本操作のテスト

### その他機能
- **ダークモード**: システム設定に応じた自動切り替え
- **ログ出力**: Timberによる構造化ログ

## 🏗️ アーキテクチャ
- **Clean Architecture**: Domain / Data / Presentation の3層構造
- **MVVM**: ViewModelを活用したUI状態管理
- **Repository Pattern**: データアクセスの抽象化

## 🛠️ 技術スタック
- **UI**: Jetpack Compose + Material3
- **DI**: Dagger Hilt
- **非同期処理**: Kotlin Coroutines + Flow
- **ネットワーク**: Retrofit + OkHttp
- **ローカルDB**: Room
- **設定管理**: DataStore
- **ログ**: Timber
- **テスト**: JUnit4 + Espresso + MockK
- **最小SDK**: API29 (Android 10)

## 📱 アプリ設定
- **アプリ名**: Android Base App
- **パッケージ名**: com.example.androidbaseapp
- **画面向き**: Portrait（縦向き）固定
- **ビルドバリアント**: Debug/Release のみ
- **ProGuard/R8**: Release版で有効化
- **ログ出力**: Debug版のみ有効