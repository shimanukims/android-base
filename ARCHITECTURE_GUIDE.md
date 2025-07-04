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
        
        subgraph ViewModels ["🧠 ビジネスロジック管理"]
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
            DomainModel["Domain Models<br/>ビジネスオブジェクト<br/>・エンティティクラス<br/>・ピュアなデータ"]
            ValueObject["Value Objects<br/>値オブジェクト<br/>・不変オブジェクト<br/>・ビジネスルール"]
            ErrorModel["Error Models<br/>エラー定義<br/>・型安全なエラー<br/>・エラー分類"]
        end
        
        subgraph UseCases ["⚙️ ユースケース (オプション)"]
            UseCase["Use Cases<br/>複雑なビジネスロジック<br/>・複数Repository連携<br/>・トランザクション処理"]
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
        DomainModule["Domain Module<br/>UseCase提供<br/>・ビジネスロジック<br/>・依存関係"]
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
    DomainModule -.provides.-> UseCase
    
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
    class NetworkModule,DatabaseModule,RepositoryModule,DomainModule diStyle
    class Repository,Interfaces interfaceStyle
    class UseCase,UseCases domainStyle
```

### 📋 各層の詳細説明

#### 🖥️ Presentation Layer (プレゼンテーション層)
- **責任**: UI表示、ユーザー操作、画面状態管理
- **Screen (Composable)**: UI描画とユーザーインタラクション
  - Pull to Refresh、リスト表示、ナビゲーション制御
- **ViewModel**: ビジネスロジックとUI状態の仲介
  - StateFlow による状態管理、Domain層との連携
- **UiState**: 画面状態の定義 (Loading/Success/Error/Empty)

#### 🎯 Domain Layer (ドメイン層)
- **責任**: ビジネスルール、ドメインモデル、抽象化
- **Repository Interface**: データアクセスの抽象化
  - Data層の実装詳細を隠蔽
- **Domain Models**: ビジネス概念の表現
  - User, Address, AppError等のピュアなデータクラス
- **Use Cases (オプション)**: 複雑なビジネスロジック
  - 複数Repositoryを組み合わせる場合や複雑な処理

#### 💾 Data Layer (データ層)
- **責任**: データ取得、永続化、データソース管理
- **Repository Implementation**: Domain層インターフェースの実装
  - Remote/Local データソースの調整、キャッシュ戦略
- **Remote Data Source**: API通信
  - Retrofit, DTOs, エラーハンドリング
- **Local Data Source**: ローカル永続化
  - Room DB, Entities, DAO
- **Data Mappers**: データ変換
  - DTO ↔ Domain, Entity ↔ Domain

#### ⚙️ DI Layer (依存性注入層)
- **責任**: 依存関係の設定と提供
- **各Module**: コンポーネントの生成と提供
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
        ErrorDialog["Error Dialog<br/>・エラーメッセージ表示<br/>・リトライボタン有無"]
        Snackbar["Snackbar/Toast<br/>・軽微なエラー<br/>・一時的な通知"]
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
    ErrorState --> Snackbar
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
    class ErrorDialog,Snackbar uiStyle
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
       return safeApiCall { userApi.getUsers() }
           .mapWithError { userDtos ->
               // データ変換とDB保存処理
               val users = userDtos.map { it.toDomain() }
               saveToDatabase(users)
           }
   }
   ```

3. **Domain層（表現）**:
   - sealed classで型安全なエラー表現
   - ビジネスロジックに応じた分類

4. **ViewModel層（処理）**:
   - Result型のonSuccess/onFailureで処理を分岐
   - **ErrorMessageProvider**でAppErrorをエラーメッセージに変換
   - リトライ可能性の判定
   ```kotlin
   // ErrorMessageProviderを使用した実装例
   .onFailure { throwable ->
       val appError = if (throwable is AppErrorException) {
           throwable.appError
       } else {
           AppError.UnknownError(throwable.message ?: "Unknown error")
       }
       _errorMessage.value = errorMessageProvider.getErrorMessage(appError)
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
graph TB
    subgraph DIConcepts ["🧠 DI基本概念"]
        A["依存関係の外部化<br/>・コンストラクタ注入<br/>・フィールド注入"]
        B["インターフェース活用<br/>・実装の抽象化<br/>・Mock差し替え"]
        C["ライフサイクル管理<br/>・Singleton<br/>・Scoped"]
    end
    
    subgraph ModuleDesignPrinciples ["📐 Module設計原則"]
        D[機能別Module分割]
        E[Scope適切設定]
        F[Interface活用]
        G[Test可能性確保]
    end
    
    subgraph ModulePatterns ["🏗️ Moduleパターン"]
        direction TB
        
        subgraph CoreModules ["Core Modules"]
            NetMod["NetworkModule<br/>・Retrofit設定<br/>・OkHttp設定<br/>・Singleton"]
            DBMod["DatabaseModule<br/>・Room設定<br/>・DAO提供<br/>・Singleton"]
        end
        
        subgraph FeatureModules ["Feature Modules"]
            UserMod["UserModule<br/>・Repository + UseCase<br/>・機能固有設定"]
            AuthMod["AuthModule<br/>・認証関連<br/>・将来追加想定"]
            SettingMod["SettingsModule<br/>・設定関連<br/>・将来追加想定"]
        end
        
        subgraph TestModules ["Test Modules"]
            TestNetMod["TestNetworkModule<br/>・Mock API<br/>・テスト用設定"]
            TestDBMod["TestDatabaseModule<br/>・In-Memory DB<br/>・テスト用設定"]
        end
    end
    
    subgraph ScopeStrategy ["🎯 Scope戦略"]
        SingletonScope["Singleton<br/>・DB, API, Repository<br/>・アプリ全体で共有"]
        ViewModelScope["ViewModelScoped<br/>・ViewModel専用<br/>・画面ごとに管理"]
        ActivityScope["ActivityScoped<br/>・Activity専用<br/>・画面遷移で管理"]
    end
    
    %% スタイリング - VSCode対応
    classDef conceptStyle fill:#c2185b,stroke:#880e4f,stroke-width:3px,color:#ffffff
    classDef principleStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef coreStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef featureStyle fill:#7b1fa2,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef testStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    classDef scopeStyle fill:#d32f2f,stroke:#b71c1c,stroke-width:3px,color:#ffffff
    
    class A,B,C conceptStyle
    class D,E,F,G principleStyle
    class NetMod,DBMod coreStyle
    class UserMod,AuthMod,SettingMod featureStyle
    class TestNetMod,TestDBMod testStyle
    class SingletonScope,ViewModelScope,ActivityScope scopeStyle
```

### 🔧 DI設計ガイドライン

1. **Module分割**: 関連する機能をグループ化
2. **Scope選択**: オブジェクトの生存期間を適切に設定
3. **Interface優先**: 具体的な実装より抽象化を重視
4. **テスト考慮**: Mock実装への差し替えやすさを確保

## Architecture5: UI状態管理パターン

### 🎯 状態管理の基本原則

**UI状態管理** は、アプリケーションの画面状態を予測可能で一貫性のある方法で管理する仕組みです。以下の原則に従います：

- **Single Source of Truth**: 単一の真実の情報源
- **Unidirectional Data Flow**: 単方向データフロー
- **Immutable State**: 不変の状態オブジェクト
- **Reactive Programming**: リアクティブな状態変更

```mermaid
graph LR
    subgraph StateManagementPrinciples ["📐 状態管理原則"]
        A["単一の真実の情報源<br/>・ViewModel内で状態管理<br/>・重複状態の回避"]
        B["単方向データフロー<br/>・ViewModel → UI<br/>・UI → ViewModel (Actions)"]
        C["不変状態<br/>・StateFlow + data class<br/>・状態変更は新しいオブジェクト"]
    end
    
    subgraph UiStateDesignPattern ["🏗️ UiState Design Pattern"]
        D[UiState Data Class] --> E[Loading State]
        D --> F[Success State]
        D --> G[Error State]
        D --> H[Empty State]
        
        E --> I["isLoading: Boolean"]
        F --> J["data: List<T>"]
        G --> K["errorMessage: String?<br/>canRetry: Boolean"]
        H --> L["isEmpty: Boolean"]
    end
    
    subgraph StateFlowPattern ["🔄 StateFlow Pattern"]
        M["_uiState: MutableStateFlow<br/>・ViewModel内でPrivate<br/>・状態変更を管理"]
        N["uiState: StateFlow<br/>・UI向けPublic<br/>・読み取り専用"]
        O["combine for complex state<br/>・複数StateFlowの結合<br/>・derived state"]
        P["UI Observes StateFlow<br/>・collectAsStateでObserve<br/>・自動再描画"]
        
        M --> N
        N --> O
        O --> P
    end
    
    subgraph SideEffects ["⚡ Side Effects"]
        Q[LaunchedEffect] --> R[One-time events<br/>・API呼び出し<br/>・Navigation]
        S[DisposableEffect] --> T[Cleanup actions<br/>・リソース解放<br/>・リスナー削除]
        U[SideEffect] --> V[Non-compose calls<br/>・Analytics<br/>・Logging]
    end
    
    %% スタイリング - VSCode対応
    classDef principleStyle fill:#c2185b,stroke:#880e4f,stroke-width:3px,color:#ffffff
    classDef uiStateStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef stateTypeStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef stateFlowStyle fill:#7b1fa2,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef sideEffectStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    
    class A,B,C principleStyle
    class D uiStateStyle
    class E,F,G,H,I,J,K,L stateTypeStyle
    class M,N,O,P stateFlowStyle
    class Q,R,S,T,U,V sideEffectStyle
```

### 🔧 状態管理ガイドライン

1. **UiState設計**: 画面の全状態を1つのdata classで表現
2. **StateFlow活用**: リアクティブな状態変更をStateFlowで管理
3. **Side Effect分離**: 副作用は適切なCompose Effectで処理
4. **状態の最小化**: 必要最小限の状態のみを保持

## Architecture6: ナビゲーション設計パターン

### 🗺️ ナビゲーション設計の基本構造

```mermaid
flowchart TB
    subgraph NavigationCore ["🎯 ナビゲーション中核設計"]
        A["Routes定義<br/>・画面識別子<br/>・パラメータ定義<br/>・型安全な定義"]
        B["NavHost設定<br/>・ルート登録<br/>・画面遷移設定<br/>・アニメーション定義"]
        C["Navigation State<br/>・現在画面管理<br/>・履歴管理<br/>・状態保存"]
    end
    
    subgraph ScreenPattern ["📱 画面パターン設計"]
        D["List画面<br/>・一覧表示<br/>・検索・フィルタ<br/>・Pull to Refresh"]
        E["Detail画面<br/>・詳細表示<br/>・編集機能<br/>・CRUD操作"]
        F["Form画面<br/>・入力フォーム<br/>・バリデーション<br/>・保存処理"]
        G["Dialog画面<br/>・確認ダイアログ<br/>・設定画面<br/>・Modal表示"]
    end
    
    subgraph NavigationFlow ["🔄 画面遷移フロー"]
        H["Forward Navigation<br/>・リストから詳細<br/>・メイン機能遷移<br/>・Push遷移"]
        I["Backward Navigation<br/>・戻るボタン<br/>・システムBack<br/>・Pop遷移"]
        J["Replace Navigation<br/>・ログイン後<br/>・状態変更<br/>・履歴リセット"]
    end
    
    subgraph AnimationDesign ["🎨 アニメーション設計"]
        K["Horizontal Slide<br/>・左右スライド<br/>・Push/Pop<br/>・メイン遷移"]
        L["Vertical Slide<br/>・上下スライド<br/>・Modal/Sheet<br/>・付加機能"]
        M["Fade Transition<br/>・フェード<br/>・Replace<br/>・状態変更"]
    end
    
    %% データフロー定義
    A --> B
    B --> C
    
    D --> H
    E --> I
    F --> J
    G --> L
    
    H --> K
    I --> K
    J --> M
    
    %% スタイリング - VSCode対応
    classDef coreStyle fill:#1976d2,stroke:#0d47a1,stroke-width:3px,color:#ffffff
    classDef screenStyle fill:#388e3c,stroke:#1b5e20,stroke-width:3px,color:#ffffff
    classDef flowStyle fill:#7b1fa2,stroke:#4a148c,stroke-width:3px,color:#ffffff
    classDef animationStyle fill:#f57c00,stroke:#e65100,stroke-width:3px,color:#ffffff
    
    class A,B,C coreStyle
    class D,E,F,G screenStyle
    class H,I,J flowStyle
    class K,L,M animationStyle
```

### 🔧 ナビゲーション設計ガイドライン

1. **Route設計**: 画面識別子を明確に定義、パラメータは型安全に
2. **遷移パターン**: 画面の性質に応じた適切な遷移方法を選択
3. **状態管理**: ナビゲーション状態の適切な保存・復元
4. **アニメーション**: ユーザー体験を向上させる自然な遷移効果

### 📋 実装時の注意点

- **Deep Link対応**: 外部からの直接アクセスを考慮
- **State Restoration**: 画面回転・プロセス復帰時の状態保持
- **Performance**: 画面遷移時のメモリ使用量最適化
- **Testing**: ナビゲーションロジックの単体テスト実装

## Architecture7: Unit Test Guidelines

### 🧪 単体テスト設計ガイドライン

```mermaid
flowchart TB
    subgraph TestPyramid ["📐 テストピラミッド"]
        A["Unit Tests<br/>・高速実行<br/>・多数実装<br/>・単一機能テスト"]
        B["Integration Tests<br/>・中程度実行<br/>・適度な数<br/>・結合テスト"]
        C["UI Tests<br/>・低速実行<br/>・少数実装<br/>・E2Eテスト"]
        
        A --> B
        B --> C
    end
    
    subgraph UnitTestTargets ["🎯 Unit Test対象"]
        D["ViewModel Tests<br/>・StateFlow動作確認<br/>・ビジネスロジック検証<br/>・エラーハンドリング"]
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
    // Given: テストデータの準備
    val users = listOf(mockUser) // 期待する成功時のデータ
    // Repository.getUsers()が呼ばれた時にusersを返すようにMock設定
    coEvery { repository.getUsers() } returns flowOf(users)
    
    // When: テスト対象のメソッドを実行
    viewModel.loadUsers() // ユーザーリスト取得処理を実行
    
    // Then: 状態変化が正しい順序で発生することを検証
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
    // Given: キャッシュデータとキャッシュ有効性を設定
    val cachedUsers = listOf(mockUser) // キャッシュに保存されているデータ
    // ローカルデータソースがキャッシュデータを返すようにMock設定
    coEvery { localDataSource.getUsers() } returns cachedUsers
    // キャッシュが有効であることを示すMock設定
    coEvery { localDataSource.isCacheValid() } returns true
    
    // When: Repository経由でユーザーデータを取得
    val result = repository.getUsers().first() // 最初に出力される値を取得
    
    // Then: 結果検証
    // 返されたデータがキャッシュデータと一致することを確認
    assertEquals(cachedUsers, result)
    // リモートAPIが呼ばれていないことを確認（キャッシュ有効時は不要）
    coVerify(exactly = 0) { remoteDataSource.getUsers() }
    // このテストにより、オフライン時やパフォーマンス向上のためのキャッシュ機能が正しく動作することを確認
}
```

#### 3. エラーハンドリングテスト例
```kotlin
// このテストの目的: ネットワークエラー時に適切なエラー状態になることを検証
@Test
fun `loadUsers should emit error when network fails`() = runTest {
    // Given: ネットワークエラーをシミュレート
    val networkException = IOException("Network error")
    // Repository.getUsers()が呼ばれた時にエラーを発生させるMock設定
    coEvery { repository.getUsers() } throws networkException
    
    // When: エラーが発生する条件でloadUsers()を実行
    viewModel.loadUsers()
    
    // Then: エラー状態が正しく設定されることを確認
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
    // Given: APIエラーをシミュレート（try-catchを使わない）
    val networkError = IOException("Network error")
    coEvery { userApi.getUsers() } throws networkError
    
    // When: refreshUsersを実行（Result型が返される）
    val result = repository.refreshUsers()
    
    // Then: Result.failureが返されることを確認
    assertTrue(result.isFailure) // 失敗していることを確認
    
    // エラーメッセージがAppErrorに変換されていることを確認
    result.onFailure { throwable ->
        assertTrue(throwable.message?.contains("ネットワークエラー") == true)
    }
    
    // データベースが更新されていないことを確認
    coVerify(exactly = 0) { userDao.deleteAllUsers() }
    coVerify(exactly = 0) { userDao.insertUsers(any()) }
    
    // このテストにより、Result APIベースのエラーハンドリングが
    // 適切にエラーを伝播し、副作用（DB更新）を防ぐことを確認
}

// Result.successのテスト例
@Test
fun `refreshUsers should return success Result when API call succeeds`() = runTest {
    // Given: 正常なAPIレスポンス
    val userDtos = listOf(mockUserDto)
    coEvery { userApi.getUsers() } returns userDtos
    coEvery { userDao.deleteAllUsers() } just Runs
    coEvery { userDao.insertUsers(any()) } just Runs
    
    // When: refreshUsersを実行
    val result = repository.refreshUsers()
    
    // Then: Result.successが返されることを確認
    assertTrue(result.isSuccess)
    
    // データベースが正しく更新されたことを確認
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