# Android Base Project - Claude Code Instructions

このドキュメントは、Claude Codeがこのプロジェクトで作業する際の指示と知識を記録するためのものです。

## 📚 ドキュメント自動更新システム

このプロジェクトでは、開発中に得られた知識を体系的に管理し、既存ドキュメントに反映させるシステムを採用しています。

### 参照すべきドキュメント

作業開始時に必ず以下のドキュメントを確認してください：

- `README.md` - プロジェクトの概要（現在は最小限の内容）
- `.clauder/rules/` - プロジェクト固有のルール（今後作成予定）
- `docs/` - 詳細なドキュメント（今後作成予定）

### 更新ルール

#### 提案タイミング
以下の状況で、ドキュメント更新を提案してください：

1. **エラーや問題を解決した時**
2. **効率的な実装パターンを発見した時**
3. **新しいAPI/ライブラリの使用方法を確立した時**
4. **既存ドキュメントの情報が古い/不正確だと判明した時**
5. **頻繁に参照される情報を発見した時**
6. **コードレビューの修正を終わらせた時**

#### 提案フォーマット
```
💡 ドキュメント更新の提案： [状況の説明]
【更新内容】 [具体的な追加/修正内容]
【更新候補】
[ファイルパス1] - [理由]
[ファイルパス2] - [理由]
新規ファイル作成 - [理由]
どこに追加しますか？（番号を選択 or skip）
```

#### 承認プロセス
1. ユーザーが更新先を選択
2. 実際の更新内容をプレビュー表示
3. ユーザーが最終承認（yes/edit/no）
4. 承認後、ファイルを更新

### 既存ドキュメントとの連携

- 既存の記載形式やスタイルを踏襲すること
- 関連する既存内容がある場合は参照を明記すること
- 日付（YYYY-MM-DD形式）を含めて更新履歴を残すこと

### 重要な制約

1. **ユーザーの承認なしにファイルを更新しない**
2. **既存の内容を削除・変更せず、追加のみ行う**
3. **機密情報（APIキー、パスワード等）は記録しない**
4. **プロジェクトの慣習やスタイルガイドに従う**

### ドキュメントの分割管理

CLAUDE.mdが肥大化することを防ぐため、以下の基準で適切にファイルを分割してください：

- **100行を超えた場合**: 関連する内容を別ファイルに分離することを提案
- **推奨される分割方法**:
  - `.clauder/rules/update-system.md` - 更新システムのルール
  - `.clauder/rules/project-specific.md` - プロジェクト固有の設定
  - `.clauder/rules/references.md` - 参照すべきドキュメントのリスト
- **CLAUDE.mdには概要とリンクのみ残す**: 詳細は個別ファイルへ

## 📱 プロジェクト設定

### アーキテクチャ構成（2025-07-01確定）

**MVVM + Clean Architecture**
- Presentation: Compose UI + ViewModel
- Domain: UseCase + Repository Interface  
- Data: Repository Implementation + DataSource

### 技術スタック

- **最小SDK**: API 29（Android 10）
- **モジュール構成**: シングルモジュール
- **ビルド**: Kotlin DSL（build.gradle.kts）
- **状態管理**: StateFlow + SharedFlow
- **依存性注入**: Hilt
- **UI**: Jetpack Compose + Material3
- **ネットワーク**: Retrofit + OkHttp + Kotlinx Serialization
- **ローカルDB**: Room
- **設定保存**: DataStore（SharedPreferences代替）
- **非同期処理**: Coroutines

### 主要依存関係

```kotlin
// UI
implementation platform('androidx.compose:compose-bom:2024.02.00')
implementation 'androidx.compose.ui:ui'
implementation 'androidx.compose.material3:material3'
implementation "androidx.navigation:navigation-compose:2.7.6"

// 依存性注入
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"
implementation "androidx.hilt:hilt-navigation-compose:1.1.0"

// 非同期処理
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"

// ネットワーク
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"
implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0"

// ローカルDB
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// DataStore
implementation "androidx.datastore:datastore-preferences:1.0.0"
```

### コーディング規約

- **テストライブラリ**: 不要（プロジェクト方針）
- **ViewModel**: @HiltViewModel + StateFlow/SharedFlow使用
- **Repository**: インターフェース + 実装クラス分離
- **依存関係**: @Module + @InstallIn でHilt設定

### REST API実装方針（2025-07-01確定）

#### Repository + UseCase パターン
```kotlin
// 直接Repositoryを呼び出さない理由：
// 1. ビジネスロジックの分離 - ViewModelにビジネスロジックを混在させない
// 2. 再利用性 - 複数のViewModelから同じビジネスロジックを利用可能
// 3. テスタビリティ - UseCaseを単独でテスト可能
// 4. 単一責任の原則 - 各層が明確な責任を持つ
```

#### エラーハンドリング
```kotlin
// Result型パターン
// try-catchを使わない理由：
// 1. 宣言的なエラーハンドリング - 成功/失敗が型として表現される
// 2. 例外の伝播防止 - 予期しない例外でアプリがクラッシュしない
// 3. 関数型プログラミング - map, flatMapなどでチェーン処理可能
// 4. 明示的なエラー処理 - エラー処理の強制により、処理漏れを防ぐ

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Throwable) : Result<T>()
}
```

#### キャッシュ戦略
**Room + Network（Single Source of Truth）**
- ローカルDBを単一の信頼できるデータソースとして扱う
- UIは常にローカルDBを監視
- ネットワークからのデータは一度DBに保存してからUIに反映
- オフライン対応とデータの一貫性を保証

## 🎯 ベースプロジェクト仕様（2025-07-01確定）

### アプリ概要：ユーザー管理アプリ（User Directory）

シンプルで理解しやすく、全アーキテクチャ要素を活用できるサンプルアプリケーション。

### 主要機能

1. **ユーザー一覧表示**
   - Pull to Refresh機能
   - ローディング状態表示
   - エラーハンドリング（ネットワークエラー時はキャッシュ表示）
   - ページネーション対応

2. **ユーザー詳細表示**
   - 詳細情報の表示
   - お気に入り登録（ローカル保存）
   - 画面遷移アニメーション

3. **ユーザー検索**
   - リアルタイム検索（ローカルフィルタリング）
   - 検索履歴保存（DataStore使用）

4. **設定画面**
   - ダークモード切替（DataStore）
   - キャッシュクリア機能
   - 通知設定

### 使用API：JSONPlaceholder

```kotlin
// Base URL
const val BASE_URL = "https://jsonplaceholder.typicode.com/"

// エンドポイント
@GET("users")
suspend fun getUsers(
    @Query("_page") page: Int = 1,
    @Query("_limit") limit: Int = 20
): List<UserDto>

@GET("users/{id}")
suspend fun getUser(@Path("id") userId: Int): UserDto

// レスポンスサンプル
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874"
  },
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org",
  "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
  }
}
```

### データモデル

```kotlin
// Entity（Room）
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val companyName: String,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

// DTO（API）
@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val address: AddressDto,
    val phone: String,
    val website: String,
    val company: CompanyDto
)

// Domain Model
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val companyName: String,
    val isFavorite: Boolean = false
)
```

### 画面構成

1. **MainActivity** - Single Activity構成
2. **UserListScreen** - ユーザー一覧（ホーム画面）
3. **UserDetailScreen** - ユーザー詳細
4. **SettingsScreen** - 設定画面
5. **SearchScreen** - 検索画面（オプション）

### アーキテクチャ図

- `docs/architecture-diagram.svg` - 全体構成図
- `docs/data-flow-diagram.svg` - データフロー詳細図