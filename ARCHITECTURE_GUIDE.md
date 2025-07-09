# Android Base App - アーキテクチャガイド

> 新規参画者向け基本構成説明および機能拡張時の設計方針

## Architecture1: 全体アーキテクチャ（Clean Architecture + MVVM）

```mermaid
graph TB
    %% =================================================================
    %% PRESENTATION LAYER - UI表示とユーザー操作
    %% =================================================================
    subgraph PresentationLayer ["🖥️ Presentation Layer - UI表示とユーザー操作"]
        direction TB
        
        subgraph Screens ["📱 画面 (Composable)"]
            Screen["Screen<br/>Jetpack Compose UI<br/>・リスト表示<br/>・フォーム入力<br/>・ナビゲーション"]
        end
        
        subgraph ViewModels ["🧠 ドメインロジック管理"]
            ViewModel["ViewModel<br/>StateFlow + UiState管理<br/>・画面状態制御<br/>・Domain層連携"]
        end
        
        subgraph UIState ["📊 UI状態定義"]
            UiStateClasses["UiState<br/>Loading/Success/Error/Empty<br/>・画面表示状態<br/>・エラー情報"]
        end
        
        Screen --> ViewModel
        ViewModel --> UiStateClasses
    end
    
    %% =================================================================
    %% DOMAIN LAYER - ビジネスルールと抽象化
    %% =================================================================
    subgraph DomainLayer ["🎯 Domain Layer - ビジネスルールと抽象化"]
        direction TB
        
        subgraph Interfaces ["🔌 インターフェース定義"]
            Repository["Repository Interface<br/>データアクセス抽象化<br/>・CRUD操作定義<br/>・Flow型戻り値"]
        end
        
        subgraph Models ["📋 ドメインモデル"]
            DomainModel["Domain Models<br/>ドメインオブジェクト<br/>・エンティティクラス<br/>・ピュアなデータ"]
            ValueObject["Value Objects<br/>値オブジェクト<br/>・不変オブジェクト<br/>・ドメインルール"]
            ErrorModel["Error Models<br/>エラー定義<br/>・型安全なエラー<br/>・エラー分類"]
        end
        
        subgraph UseCases ["⚙️ ユースケース (オプション)"]
            UseCase["Use Cases<br/>複雑なドメインロジック<br/>・複数Repository連携<br/>・トランザクション処理"]
        end
        
        Repository --> Models
        UseCase --> Repository
        UseCase --> Models
    end
    
    %% =================================================================
    %% DATA LAYER - データ取得と永続化
    %% =================================================================
    subgraph DataLayer ["💾 Data Layer - データ取得と永続化"]
        direction TB
        
        subgraph Implementation ["🏗️ Repository実装"]
            RepositoryImpl["Repository Implementation<br/>データソース調整<br/>・キャッシュ戦略<br/>・オフライン対応"]
        end
        
        subgraph RemoteDataSource ["🌐 リモートデータソース"]
            API["API Service (Retrofit)<br/>REST API呼び出し<br/>・HTTP通信<br/>・認証処理"]
            DTO["DTOs<br/>API レスポンス/リクエスト<br/>・JSON シリアライゼーション<br/>・API仕様準拠"]
            ErrorHandler["Error Handler<br/>エラー変換<br/>・HTTP エラー<br/>・ネットワークエラー"]
        end
        
        subgraph LocalDataSource ["🗄️ ローカルデータソース"]
            DAO["DAO (Room)<br/>データベース操作<br/>・CRUD Query<br/>・Flow対応"]
            Entity["Entities<br/>データベーステーブル<br/>・テーブル定義<br/>・リレーション"]
            Database["Database (Room)<br/>DB設定とマイグレーション<br/>・スキーマ管理<br/>・バージョン管理"]
        end
        
        subgraph DataTransformation ["🔄 データ変換"]
            Mapper["Data Mappers<br/>DTO/Entity ↔ Domain変換<br/>・型安全な変換<br/>・データ正規化"]
        end
        
        RepositoryImpl --> API
        RepositoryImpl --> DAO
        API --> DTO
        DAO --> Entity
        Entity --> Database
        DTO --> Mapper
        Entity --> Mapper
        Mapper --> Models
        API --> ErrorHandler
        ErrorHandler --> ErrorModel
    end
    
    %% =================================================================
    %% DI LAYER - 依存性注入設定
    %% =================================================================
    subgraph DILayer ["⚙️ DI Layer - 依存性注入設定"]
        direction LR
        NetworkModule["Network Module<br/>Retrofit/OkHttp設定<br/>・タイムアウト<br/>・インターセプター"]
        DatabaseModule["Database Module<br/>Room DB設定<br/>・マイグレーション<br/>・DAO提供"]
        RepositoryModule["Repository Module<br/>Repository binding<br/>・実装バインド<br/>・Singleton管理"]
    end
    
    %% =================================================================
    %% 依存関係の定義
    %% =================================================================
    %% レイヤー間の依存関係
    ViewModel --> Repository
    ViewModel -.optional.-> UseCase
    
    Repository --> RepositoryImpl
    
    %% DI による提供
    NetworkModule -.provides.-> API
    DatabaseModule -.provides.-> DAO
    DatabaseModule -.provides.-> Database
    RepositoryModule -.binds.-> RepositoryImpl
    
    %% =================================================================
    %% スタイリング定義 - VSCode対応で濃い色に変更
    %% =================================================================
    classDef presentationStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef domainStyle fill:#6a1b9a,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef dataStyle fill:#2e7d32,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef diStyle fill:#ef6c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    classDef interfaceStyle fill:#c2185b,stroke:#880e4f,stroke-width:3px,color:#ffffff
    
    class Screen,ViewModel,UiStateClasses presentationStyle
    class Models,DomainModel,ValueObject,ErrorModel domainStyle
    class RepositoryImpl,API,DTO,DAO,Entity,Database,Mapper,ErrorHandler dataStyle
    class NetworkModule,DatabaseModule,RepositoryModule diStyle
    class Repository,Interfaces interfaceStyle
    class UseCase,UseCases domainStyle
```

### 📋 各層の詳細説明

#### 🖥️ Presentation Layer (プレゼンテーション層)
- **責任**: 
  - UI表示、ユーザー操作、画面状態管理
- **Screen (Composable)**: 
  - UI描画とユーザーインタラクション
  - Pull to Refresh、リスト表示、ナビゲーション制御
- **ViewModel**: 
  - ドメインロジックとUI状態の仲介
  - StateFlow による状態管理、Domain層との連携
- **UiState**: 
  - 画面状態の定義 (Loading/Success/Error/Empty)

#### 🎯 Domain Layer (ドメイン層)
- **責任**: 
  - ドメインルール、ドメインモデル、抽象化
- **Repository Interface**: 
  - データアクセスの抽象化
  - Data層の実装詳細を隠蔽
- **Domain Models**: 
  - ドメイン概念の表現
  - User, Address, AppError等のピュアなデータクラス
- **Use Cases (オプション)**: 
  - 複雑なドメインロジック
  - 複数Repositoryを組み合わせる場合や複雑な処理

#### 💾 Data Layer (データ層)
- **責任**: 
  - データ取得、永続化、データソース管理
- **Repository Implementation**: 
  - Domain層インターフェースの実装
  - Remote/Local データソースの調整、キャッシュ戦略
- **Remote Data Source**: 
  - API通信
  - Retrofit, DTOs, エラーハンドリング
- **Local Data Source**: 
  - ローカル永続化
  - Room DB, Entities, DAO
- **Data Mappers**: 
  - データ変換
  - DTO ↔ Domain, Entity ↔ Domain

#### ⚙️ DI Layer (依存性注入層)
- **責任**: 
  - 依存関係の設定と提供
- **各Module**: 
  - コンポーネントの生成と提供
  - Singleton管理、テスト時の差し替え


## Architecture2: データフロー設計原則

```mermaid
sequenceDiagram
    participant UI as UI Screen
    participant VM as ViewModel
    participant Repo as Repository
    participant API as Remote API
    participant DB as Local DB
    
    Note over UI,DB: 標準的なデータフロー
    
    UI->>VM: User Action
    VM->>Repo: Business Logic Request
    
    alt Fresh Data Needed
        Repo->>API: Fetch from Remote
        API-->>Repo: Response Data
        Repo->>DB: Cache Data
    else Cache Valid
        Repo->>DB: Read from Cache
    end
    
    DB-->>Repo: Domain Models
    Repo-->>VM: Flow DomainModel
    VM->>VM: Transform to UiState
    VM-->>UI: StateFlow UiState
    UI->>UI: Recomposition
```

## Architecture3: エラーハンドリングフロー

### 📊 エラー処理の階層構造とデータフロー

```mermaid
flowchart TB
    subgraph DataLayer ["💾 Data層（エラー発生源）"]
        API[("🌐 API通信<br/>・Network Exception<br/>・HTTP Error Codes<br/>・Timeout")]
        DB[("🗄️ Database<br/>・SQL Exception<br/>・Constraint Violation")]
        Cache[("📦 Cache<br/>・Expired Data<br/>・Invalid State")]
    end
    
    subgraph ErrorCatchLayer ["🔄 Repository層（Result API）"]
        ResultAPI["Result API<br/>・runCatchingで安全実行<br/>・safeApiCall拡張関数"]
        ErrorMapper["Error Mapper<br/>・Exception → AppError変換<br/>・mapWithError拡張関数"]
    end
    
    subgraph DomainErrorLayer ["🎯 Domain層（エラー表現）"]
        AppErrorClass["sealed class AppError<br/>├─ NetworkError<br/>├─ TimeoutError<br/>├─ OfflineError<br/>└─ UnknownError"]
        ErrorAttributes["エラー属性<br/>・message: String<br/>・code: Int?<br/>・isRetryable: Boolean"]
    end
    
    subgraph ViewModelLayer ["🧠 ViewModel層（エラー処理）"]
        ErrorState["Error State管理<br/>・UiState.Error(appError)<br/>・canRetry判定"]
        RetryLogic["リトライロジック<br/>・再実行可能性チェック<br/>・リトライ回数管理"]
    end
    
    subgraph UILayer ["🖥️ UI層（エラー表示）"]
        ErrorDialog["AlertDialog<br/>・エラーメッセージ表示<br/>・リトライボタン有無"]
    end
    
    %% データフロー定義
    API --> ResultAPI
    DB --> ResultAPI
    Cache --> ResultAPI
    
    ResultAPI --> ErrorMapper
    ErrorMapper --> AppErrorClass
    AppErrorClass --> ErrorAttributes
    
    AppErrorClass --> ErrorState
    ErrorAttributes --> ErrorState
    ErrorState --> RetryLogic
    
    ErrorState --> ErrorDialog
    RetryLogic --> ErrorDialog
    
    %% スタイリング - VSCode対応
    classDef dataStyle fill:#d32f2f,stroke:#b71c1c,stroke-width:3px,color:#ffffff
    classDef catchStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    classDef domainStyle fill:#7b1fa2,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef viewModelStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef uiStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    
    class API,DB,Cache dataStyle
    class ResultAPI,ErrorMapper catchStyle
    class AppErrorClass,ErrorAttributes domainStyle
    class ErrorState,RetryLogic viewModelStyle
    class ErrorDialog uiStyle
```

### 🔍 エラー処理の詳細説明

1. **Data層（発生源）**: 
   - 各種データソースで例外が発生
   - Network、Database、Cacheそれぞれ固有のエラー

2. **Repository層（Result APIでエラーハンドリング）**:
   - **try-catchを使わず**、Kotlin標準のResult APIを活用
   - `runCatching`でエラーを安全にキャッチ
   - 拡張関数でException種別に応じてAppErrorに変換
   ```kotlin
   // Result APIを使用した実装例
   suspend fun refreshUsers(): Result<Unit> {
       // 図の「Result API」層 - safeApiCallで安全実行
       return safeApiCall { userApi.getUsers() }
           // 図の「Error Mapper」層 - mapWithErrorでエラー変換
           .mapWithError { userDtos ->
               // データ変換とDB保存処理
               val users = userDtos.map { it.toDomain() }
               val entities = users.map { it.toEntity() }
               
               userDao.deleteAllUsers()
               userDao.insertUsers(entities)
               Unit
           }
   }
   ```

3. **Domain層（表現）**:
   - sealed classで型安全なエラー表現
   - ドメインロジックに応じた分類

4. **ViewModel層（処理）**:
   - Result型のonSuccess/onFailureで処理を分岐
   - **ErrorMessageProvider**でAppErrorをエラーメッセージに変換
   - リトライ可能性の判定
   ```kotlin
   // ErrorMessageProviderを使用した実装例
   .onFailure { throwable ->
       // 図の「ViewModel層」 - Error State管理
       val appError = if (throwable is AppErrorException) {
           throwable.appError
       } else {
           // 図の「Domain層」 - AppErrorクラスでエラー表現
           AppError.UnknownError(throwable.message ?: "Unknown error")
       }
       // 図の「UI層」に渡すエラーメッセージ生成
       _errorMessage.value = errorMessageProvider.getErrorMessage(appError)
       // 図の「ViewModel層」 - リトライロジック
       _canRetry.value = appError.canRetry()
   }
   ```

5. **UI層（表示）**:
   - エラー種別に応じた適切なUI表示
   - ユーザーアクション（リトライ等）の処理

### 📋 ErrorMessageProviderパターン

**Clean Architectureに準拠したエラーメッセージ管理**:

- **Domain層**: `ErrorMessageProvider` インターフェースで抽象化
- **Presentation層**: `AndroidErrorMessageProvider` でString Resources使用
- **DI**: 依存関係の逆転でPlatform固有実装を注入
- **利点**: Domain層がAndroid固有に依存せず、テストも容易、メッセージの一元管理

## Architecture4: DI設計方針（Hilt）

### 🔌 依存性注入の基本概念

**DI（Dependency Injection）** は、クラスが必要とする依存関係を外部から注入する設計パターンです。以下の利点があります：

- **テスタビリティ**: Mock実装を簡単に差し替え可能
- **疎結合**: 具体的な実装に依存しない
- **再利用性**: 同じインターフェースで複数の実装を使い分け
- **保守性**: 設定を一箇所で管理

```mermaid
flowchart LR
    subgraph WithoutDI ["❌ DI使用前（問題あり）"]
        VM1["ViewModel"] -->|直接生成| Repo1["Repository実装"]
        Repo1 -->|直接生成| API1["API実装"]
        Repo1 -->|直接生成| DB1["DB実装"]
        
        Note1["問題点:<br/>・テスト困難<br/>・実装の差し替え不可<br/>・依存関係が密結合"]
    end
    
    subgraph WithDI ["✅ DI使用後（Hilt）"]
        VM2["ViewModel"] -->|注入される| RepoInterface["Repository<br/>インターフェース"]
        
        Hilt["Hilt<br/>（DIコンテナ）"] -->|実装を提供| RepoInterface
        
        RepoImpl["Repository実装"] -.->|実装| RepoInterface
        RepoImpl -->|注入される| APIInterface["API"]
        RepoImpl -->|注入される| DBInterface["DB"]
        
        Note2["利点:<br/>・テスト時はMock注入<br/>・実装の差し替え可能<br/>・疎結合"]
    end
    
    %% スタイリング
    classDef problemStyle fill:#d32f2f,stroke:#b71c1c,stroke-width:3px,color:#ffffff
    classDef solutionStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef interfaceStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef hiltStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    
    class VM1,Repo1,API1,DB1,Note1 problemStyle
    class VM2,RepoImpl,Note2 solutionStyle
    class RepoInterface,APIInterface,DBInterface interfaceStyle
    class Hilt hiltStyle
```

### 🎯 DI（依存性注入）とは？

**クラスが必要とする部品を外部から渡す仕組み**です。

#### 例：ViewModelがRepositoryを使う場合

**❌ DI使用前（問題あり）**
```kotlin
class UserListViewModel {
    // 図の「❌ DI使用前」 - ViewModelが直接Repositoryを生成
    private val repository = UserRepositoryImpl(
        UserApi(),      // 直接生成（図の問題点：密結合）
        UserDao()       // 直接生成（図の問題点：テスト困難）
    )
}
```

**✅ DI使用後（Hilt）**
```kotlin
@HiltViewModel
class UserListViewModel @Inject constructor(
    // 図の「✅ DI使用後」 - インターフェースを注入される
    private val repository: UserRepository  // インターフェースを注入
) : ViewModel()

// 図の「Hilt（DIコンテナ）」が実装を管理
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl  // 図の「Repository実装」
    ): UserRepository  // 図の「Repository インターフェース」
}
```

### 🔧 Hiltを使った実装構成

```mermaid
flowchart TD
    subgraph HiltModules ["Hiltモジュール構成"]
        NetworkModule["NetworkModule<br/>・Retrofit提供<br/>・API設定"]
        DatabaseModule["DatabaseModule<br/>・Room DB提供<br/>・DAO提供"]
        RepositoryModule["RepositoryModule<br/>・Repository実装を<br/>インターフェースに結合"]
        ProviderModule["ProviderModule<br/>・ErrorMessageProvider<br/>などの提供"]
    end
    
    subgraph Usage ["使用例"]
        NetworkModule -->|API提供| RepositoryImpl["Repository実装"]
        DatabaseModule -->|DAO提供| RepositoryImpl
        RepositoryModule -->|Repository提供| ViewModel
        ProviderModule -->|Provider提供| ViewModel
    end
    
    %% スタイリング
    classDef moduleStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef usageStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    
    class NetworkModule,DatabaseModule,RepositoryModule,ProviderModule moduleStyle
    class RepositoryImpl,ViewModel usageStyle
```

### 📋 重要なポイント

1. **依存関係は自動管理**: `@Inject`を付けるだけで必要な部品が注入される
2. **テストが簡単**: テスト時はMock実装に差し替え可能
3. **設定は一箇所**: Module内で依存関係を一元管理
4. **実装の詳細を隠蔽**: インターフェースを使って疎結合を実現

## Architecture5: UI状態管理パターン

### 🎯 状態管理の基本原則

**UI状態管理** は、アプリケーションの画面状態を予測可能で一貫性のある方法で管理する仕組みです。以下の原則に従います：

- **Single Source of Truth**: 単一の真実の情報源
- **Unidirectional Data Flow**: 単方向データフロー
- **Immutable State**: 不変の状態オブジェクト
- **Reactive Programming**: リアクティブな状態変更

```mermaid
flowchart TD
    StateManagementPrinciples["📐 状態管理原則<br/><br/><b>UI状態管理の3つの基本ルール</b><br/><br/>1. 単一の真実の情報源<br/>　　- ViewModel内で状態管理<br/>　　- 重複状態の回避<br/><br/>2. 単方向データフロー<br/>　　- ViewModel → UI<br/>　　- UI → ViewModel (Actions)<br/><br/>3. 不変状態<br/>　　- StateFlow + data class<br/>　　- 状態変更は新しいオブジェクト<br/><br/><i>なぜ必要か：複数の場所で状態を管理すると<br/>データの不整合やバグが発生しやすくなる</i>"]
    
    UiStateDesignPattern["🏗️ UiState Design Pattern<br/><br/><b>画面が取りうる4つの状態を統一的に表現</b><br/><br/>UiState Data Class<br/>├─ Loading State (isLoading: Boolean)<br/>├─ Success State (data: List&lt;T&gt;)<br/>├─ Error State (errorMessage: String?, canRetry: Boolean)<br/>└─ Empty State (isEmpty: Boolean)"]
    
    StateFlowPattern["🔄 StateFlow Pattern<br/><br/><b>ViewModelとUIの間で状態を安全に共有</b><br/><br/>実装フロー：<br/>1. _uiState: MutableStateFlow (Private)<br/>2. uiState: StateFlow (Public読み取り専用)<br/>3. combine for complex state (複数StateFlowの結合)<br/>4. UI Observes StateFlow (collectAsStateでObserve)"]
    
    SideEffects["⚡ Side Effects<br/><br/><b>UI描画以外の処理を適切に管理</b><br/><br/>LaunchedEffect → One-time events<br/>　　API呼び出し、Navigation<br/><br/>DisposableEffect → Cleanup actions<br/>　　リソース解放、リスナー削除<br/><br/>SideEffect → Non-compose calls<br/>　　Analytics、Logging"]
    
    %% スタイリング - VSCode対応
    classDef principleStyle fill:#c2185b,stroke:#880e4f,stroke-width:3px,color:#ffffff
    classDef uiStateStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef stateFlowStyle fill:#7b1fa2,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef sideEffectStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    
    class StateManagementPrinciples principleStyle
    class UiStateDesignPattern uiStateStyle
    class StateFlowPattern stateFlowStyle
    class SideEffects sideEffectStyle
```

### 📊 実装例の詳細

#### 図2: UiState Design Pattern 実装例
```kotlin
// 図2で示した4つの状態をViewModelで使用
when {
    uiState.isLoading -> ShowLoadingIndicator()        // Loading State対応
    uiState.errorMessage != null -> ShowError(uiState.errorMessage)  // Error State対応
    uiState.isEmpty -> ShowEmptyMessage()              // Empty State対応
    else -> ShowUserList(uiState.users)                // Success State対応
}
```

#### 図3: StateFlow Pattern 実装例
```kotlin
// 図3の実装フローに対応
class UserListViewModel : ViewModel() {
    // 1. _uiState: MutableStateFlow (Private) - 図3-1対応
    private val _uiState = MutableStateFlow(UserListUiState())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    // 2. uiState: StateFlow (Public読み取り専用) - 図3-2対応
    // 3. combine for complex state (複数StateFlowの結合) - 図3-3対応
    val uiState: StateFlow<UserListUiState> = combine(
        _uiState,
        _isLoading,
        _errorMessage
    ) { state, loading, error ->
        state.copy(
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserListUiState()
    )
    
    // 状態の更新
    fun loadUsers() {
        _isLoading.value = true
        // データ取得処理...
    }
}

// 4. UI Observes StateFlow (collectAsStateでObserve) - 図3-4対応
@Composable
fun UserListScreen(viewModel: UserListViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // uiStateの変更に応じて自動的に再描画
}
```

#### 図4: Side Effects 実装例
```kotlin
// LaunchedEffect → One-time events (図4対応)
@Composable
fun UserScreen(userId: String) {
    LaunchedEffect(userId) {
        // API呼び出し例：ユーザーIDが変わった時だけ再実行
        viewModel.loadUser(userId)
    }
}

// DisposableEffect → Cleanup actions (図4対応)
@Composable
fun LocationScreen() {
    DisposableEffect(Unit) {
        val listener = startLocationUpdates()  // リソース取得
        onDispose {
            stopLocationUpdates(listener)      // リソース解放
        }
    }
}

// SideEffect → Non-compose calls (図4対応)
@Composable
fun AnalyticsScreen(screenName: String) {
    SideEffect {
        // Analytics例：画面が表示される度に記録
        analytics.logScreenView(screenName)
    }
}
```

### 🔧 状態管理ガイドライン

1. **UiState設計**: 画面の全状態を1つのdata classで表現
2. **StateFlow活用**: リアクティブな状態変更をStateFlowで管理
3. **Side Effect分離**: 副作用は適切なCompose Effectで処理
4. **状態の最小化**: 必要最小限の状態のみを保持

## Architecture6: Unit Test Guidelines

### 🧪 単体テスト設計ガイドライン

```mermaid
flowchart LR
    subgraph TestPyramid ["📐 テストピラミッド"]
        direction TB
        A["Unit Tests<br/>・高速実行<br/>・多数実装<br/>・単一機能テスト"]
        B["Integration Tests<br/>・中程度実行<br/>・適度な数<br/>・結合テスト"]
        C["UI Tests<br/>・低速実行<br/>・少数実装<br/>・E2Eテスト"]
        
        A --> B
        B --> C
    end
    
    subgraph UnitTestTargets ["🎯 Unit Test対象"]
        D["ViewModel Tests<br/>・StateFlow動作確認<br/>・ドメインロジック検証<br/>・エラーハンドリング"]
        E["Repository Tests<br/>・データ変換確認<br/>・キャッシュロジック<br/>・Mock API/DB使用"]
        F["UseCase Tests<br/>・ビジネスルール検証<br/>・複雑ロジック<br/>・境界値テスト"]
        G["Mapper Tests<br/>・データ変換確認<br/>・null安全性<br/>・型変換"]
    end
    
    subgraph TestPatterns ["🔧 テストパターン"]
        H["Given-When-Then<br/>・前提条件設定<br/>・実行<br/>・結果検証"]
        I["Arrange-Act-Assert<br/>・準備<br/>・実行<br/>・検証"]
        J["Mock Strategy<br/>・外部依存Mock<br/>・状態検証<br/>・振る舞い検証"]
    end
    
    subgraph TestTools ["🛠️ テストツール"]
        K["JUnit5<br/>・テストフレームワーク<br/>・アサーション<br/>・ライフサイクル"]
        L["MockK<br/>・Mockライブラリ<br/>・Kotlin専用<br/>・Coroutines対応"]
        M["Turbine<br/>・Flow/StateFlowテスト<br/>・時系列検証<br/>・タイムアウト設定"]
    end
    
    %% 図の配置順序を明示的に指定
    TestPyramid --> UnitTestTargets
    UnitTestTargets --> TestPatterns
    TestPatterns --> TestTools
    
    %% スタイリング - VSCode対応
    classDef pyramidStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef targetStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef patternStyle fill:#7b1fa2,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef toolStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    
    class A,B,C pyramidStyle
    class D,E,F,G targetStyle
    class H,I,J patternStyle
    class K,L,M toolStyle
```

### 📋 Unit Test実装ガイドライン

#### 1. ViewModel テスト - StateFlow状態変化のテスト
```kotlin
// このテストの目的: ViewModelのloadUsers()メソッドが正しい順序で状態を変化させることを検証
@Test
fun `loadUsers should emit loading then success`() = runTest {
    // 図の「Given-When-Then」パターン - Given（前提条件設定）
    val users = listOf(mockUser) // 期待する成功時のデータ
    // 図の「Mock Strategy」 - 外部依存Mock
    coEvery { repository.getUsers() } returns flowOf(users)
    
    // 図の「Given-When-Then」パターン - When（実行）
    viewModel.loadUsers() // ユーザーリスト取得処理を実行
    
    // 図の「Given-When-Then」パターン - Then（結果検証）
    // 図の「ViewModel Tests」 - StateFlow動作確認
    viewModel.uiState.test {
        // 最初にLoading状態が出力されることを確認（読み込み開始）
        assertEquals(UiState.Loading, awaitItem())
        // 次にSuccess状態が出力されることを確認（読み込み完了）
        assertEquals(UiState.Success(users), awaitItem())
    }
    // このテストにより、UI上で正しくローディング表示→成功表示の流れが確認できる
}
```

#### 2. Repository テスト - キャッシュ戦略のテスト
```kotlin
// このテストの目的: キャッシュが有効な時にリモートAPIを呼ばずにキャッシュデータを返すことを検証
@Test
fun `getUsers should return cached data when cache is valid`() = runTest {
    // 図の「Arrange-Act-Assert」パターン - Arrange（準備）
    val cachedUsers = listOf(mockUser) // キャッシュに保存されているデータ
    // 図の「Mock Strategy」 - 外部依存Mock
    coEvery { localDataSource.getUsers() } returns cachedUsers
    // 図の「Repository Tests」 - キャッシュロジック検証
    coEvery { localDataSource.isCacheValid() } returns true
    
    // 図の「Arrange-Act-Assert」パターン - Act（実行）
    val result = repository.getUsers().first() // 最初に出力される値を取得
    
    // 図の「Arrange-Act-Assert」パターン - Assert（検証）
    // 返されたデータがキャッシュデータと一致することを確認
    assertEquals(cachedUsers, result)
    // 図の「Mock Strategy」 - 振る舞い検証
    coVerify(exactly = 0) { remoteDataSource.getUsers() }
    // このテストにより、オフライン時やパフォーマンス向上のためのキャッシュ機能が正しく動作することを確認
}
```

#### 3. エラーハンドリングテスト例
```kotlin
// このテストの目的: ネットワークエラー時に適切なエラー状態になることを検証
@Test
fun `loadUsers should emit error when network fails`() = runTest {
    // 図の「Given-When-Then」パターン - Given（前提条件設定）
    val networkException = IOException("Network error")
    // 図の「Mock Strategy」 - 外部依存Mock
    coEvery { repository.getUsers() } throws networkException
    
    // 図の「Given-When-Then」パターン - When（実行）
    viewModel.loadUsers()
    
    // 図の「Given-When-Then」パターン - Then（結果検証）
    // 図の「ViewModel Tests」 - エラーハンドリング検証
    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem()) // まずLoading状態
        // エラー状態になり、リトライ可能であることを確認
        val errorState = awaitItem() as UiState.Error
        assertEquals(true, errorState.canRetry) // ネットワークエラーはリトライ可能
        assertTrue(errorState.message.contains("Network")) // エラーメッセージにNetwork含まれる
    }
    // このテストにより、ネットワーク障害時にユーザーに適切なエラーメッセージとリトライオプションが表示されることを確認
}
```

#### 4. Result APIを使ったRepositoryテスト例
```kotlin
// このテストの目的: Result APIを使ったエラーハンドリングが正しく動作することを検証
@Test
fun `refreshUsers should return failure Result when API call fails`() = runTest {
    // 図の「Given-When-Then」パターン - Given（前提条件設定）
    val networkError = IOException("Network error")
    // 図の「Mock Strategy」 - Mock API/DB使用
    coEvery { userApi.getUsers() } throws networkError
    
    // 図の「Given-When-Then」パターン - When（実行）
    val result = repository.refreshUsers()
    
    // 図の「Given-When-Then」パターン - Then（結果検証）
    // 図の「Repository Tests」 - データ変換確認
    assertTrue(result.isFailure) // 失敗していることを確認
    
    // エラーメッセージがAppErrorに変換されていることを確認
    result.onFailure { throwable ->
        assertTrue(throwable.message?.contains("ネットワークエラー") == true)
    }
    
    // 図の「Mock Strategy」 - 振る舞い検証
    coVerify(exactly = 0) { userDao.deleteAllUsers() }
    coVerify(exactly = 0) { userDao.insertUsers(any()) }
    
    // このテストにより、Result APIベースのエラーハンドリングが
    // 適切にエラーを伝播し、副作用（DB更新）を防ぐことを確認
}

// Result.successのテスト例
@Test
fun `refreshUsers should return success Result when API call succeeds`() = runTest {
    // 図の「Given-When-Then」パターン - Given（前提条件設定）
    val userDtos = listOf(mockUserDto)
    // 図の「Mock Strategy」 - Mock API/DB使用
    coEvery { userApi.getUsers() } returns userDtos
    coEvery { userDao.deleteAllUsers() } just Runs
    coEvery { userDao.insertUsers(any()) } just Runs
    
    // 図の「Given-When-Then」パターン - When（実行）
    val result = repository.refreshUsers()
    
    // 図の「Given-When-Then」パターン - Then（結果検証）
    // 図の「Repository Tests」 - データ変換確認
    assertTrue(result.isSuccess)
    
    // 図の「Mock Strategy」 - 振る舞い検証
    coVerify(exactly = 1) { userDao.deleteAllUsers() }
    coVerify(exactly = 1) { userDao.insertUsers(any()) }
}
```

#### 5. テスト実行方法

##### Android Studio での実行
1. **単一テストの実行**: テストメソッド横の緑色の▶️ボタンをクリック
2. **クラス全体の実行**: テストクラス名横の▶️ボタンをクリック
3. **モジュール全体の実行**: Project パネルでテストディレクトリを右クリック → "Run Tests"

##### コマンドラインでの実行
```bash
# 全てのUnit Testを実行
./gradlew testDebugUnitTest

# 特定のテストクラスのみ実行
./gradlew testDebugUnitTest --tests "com.example.UserViewModelTest"

# 特定のテストメソッドのみ実行
./gradlew testDebugUnitTest --tests "com.example.UserViewModelTest.loadUsers should emit loading then success"

# テスト結果レポート生成
./gradlew testDebugUnitTest --html
# → build/reports/tests/testDebugUnitTest/index.html でレポート確認可能
```

#### 6. テスト設計原則
- **単一責任**: 1つのテストで1つの機能のみ検証
- **独立性**: テスト間で状態を共有しない
- **可読性**: テスト名と構造で意図を明確に
- **高速実行**: 外部依存は全てMockで置き換え

## 開発時の重要原則

### 設計原則
- **Single Responsibility**: 各クラスは単一の責任を持つ
- **Dependency Inversion**: 抽象に依存し、具象に依存しない
- **Testability**: テストしやすい設計を心がける
- **Consistency**: 既存パターンとの一貫性を保つ

### データフロー原則
- **Unidirectional**: データは一方向に流れる
- **Immutable State**: 状態は不変オブジェクトで管理
- **Reactive**: Flow/StateFlowによるリアクティブプログラミング
- **Error Handling**: 統一されたエラーハンドリング

### パフォーマンス原則
- **Lazy Loading**: 必要な時に必要なデータを読み込む
- **Caching Strategy**: 適切なキャッシュ戦略
- **Background Processing**: UI スレッドをブロックしない
- **Memory Management**: メモリリークの防止