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
    %% スタイリング定義
    %% =================================================================
    classDef presentationStyle fill:#e1f5fe,stroke:#0277bd,stroke-width:2px
    classDef domainStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef dataStyle fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef diStyle fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef interfaceStyle fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
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

## Architecture2: 新規画面作成時の標準パターン

```mermaid
flowchart TD
    A[新機能要件] --> B{データソースは？}
    
    B -->|API + DB| C[標準パターン]
    B -->|APIのみ| D[軽量パターン]
    B -->|DBのみ| E[ローカルパターン]
    
    subgraph StandardPattern ["標準パターン 推奨"]
        C --> C1["Step1: Domain Model作成"]
        C1 --> C2["Step2: Repository Interface定義"]
        C2 --> C3["Step3: API + DAO作成"]
        C3 --> C4["Step4: Repository Implementation"]
        C4 --> C5["Step5: ViewModel作成"]
        C5 --> C6["Step6: Screen作成"]
        C6 --> C7["Step7: DI設定"]
        C7 --> C8["Step8: Navigation設定"]
    end
    
    subgraph LightPattern ["軽量パターン"]
        D --> D1["Domain Model + API"]
        D1 --> D2["Repository Simple"]
        D2 --> D3["ViewModel + Screen"]
    end
    
    subgraph LocalPattern ["ローカルパターン"]
        E --> E1["Domain Model + DAO"]
        E1 --> E2["Repository Local"]
        E2 --> E3["ViewModel + Screen"]
    end
    
    %% スタイリング
    classDef startStyle fill:#ffebee,stroke:#c62828,stroke-width:2px
    classDef decisionStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef standardStyle fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef lightStyle fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef localStyle fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    
    class A startStyle
    class B decisionStyle
    class C,C1,C2,C3,C4,C5,C6,C7,C8 standardStyle
    class D,D1,D2,D3 lightStyle
    class E,E1,E2,E3 localStyle
```

## Architecture3: データフロー設計原則

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

## Architecture4: エラーハンドリング統一設計

```mermaid
graph TD
    A[Exception発生] --> B{Exception Type}
    
    B -->|Network| C[NetworkError]
    B -->|Timeout| D[TimeoutError]  
    B -->|Offline| E[OfflineError]
    B -->|Business Logic| F[BusinessError]
    B -->|Unknown| G[UnknownError]
    
    C --> H[AppError Sealed Class]
    D --> H
    E --> H
    F --> H
    G --> H
    
    H --> I[エラーメッセージ生成]
    H --> J[リトライ可否判定]
    
    I --> K[ViewModel Error State]
    J --> K
    
    K --> L{UI Error Handling}
    L -->|Retryable| M[AlertDialog with Retry]
    L -->|Non-Retryable| N[AlertDialog with OK]
    L -->|Silent| O[Snackbar/Toast]
    
    %% スタイリング
    classDef exceptionStyle fill:#ffebee,stroke:#d32f2f,stroke-width:2px
    classDef errorTypeStyle fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef errorClassStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef processStyle fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef viewModelStyle fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef uiStyle fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class A exceptionStyle
    class B,L errorTypeStyle
    class C,D,E,F,G,H errorClassStyle
    class I,J processStyle
    class K viewModelStyle
    class M,N,O uiStyle
```

## Architecture5: DI設計方針（Hilt）

```mermaid
graph TB
    subgraph ModuleDesignPrinciples ["Module設計原則"]
        A[機能別Module分割]
        B[Scope適切設定]
        C[Interface活用]
        D[Test可能性確保]
    end
    
    subgraph ModulePatterns ["Moduleパターン"]
        direction TB
        
        subgraph CoreModules ["Core Modules"]
            NetMod["NetworkModule|Singleton"]
            DBMod["DatabaseModule|Singleton"]
        end
        
        subgraph FeatureModules ["Feature Modules"]
            UserMod["UserModule|Repository + UseCase"]
            AuthMod["AuthModule|将来追加想定"]
            SettingMod["SettingsModule|将来追加想定"]
        end
        
        subgraph TestModules ["Test Modules"]
            TestNetMod["TestNetworkModule"]
            TestDBMod["TestDatabaseModule"]
        end
    end
    
    subgraph ScopeStrategy ["Scope戦略"]
        SingletonScope["Singleton|DB, API, Repository"]
        ViewModelScope["ViewModelScoped|ViewModel"]
        ActivityScope["ActivityScoped|Navigation"]
    end
    
    %% スタイリング
    classDef principleStyle fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef coreStyle fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef featureStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef testStyle fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef scopeStyle fill:#ffebee,stroke:#c62828,stroke-width:2px
    
    class A,B,C,D principleStyle
    class NetMod,DBMod coreStyle
    class UserMod,AuthMod,SettingMod featureStyle
    class TestNetMod,TestDBMod testStyle
    class SingletonScope,ViewModelScope,ActivityScope scopeStyle
```

## Architecture6: UI状態管理パターン

```mermaid
graph LR
    subgraph UiStateDesignPattern ["UiState Design Pattern"]
        A[UiState Data Class] --> B[Loading State]
        A --> C[Success State]
        A --> D[Error State]
        A --> E[Empty State]
        
        B --> F["isLoading: Boolean"]
        C --> G["data: List T"]
        D --> H["errorMessage: String?|canRetry: Boolean"]
        E --> I["isEmpty: Boolean"]
    end
    
    subgraph StateFlowPattern ["StateFlow Pattern"]
        J["_uiState: MutableStateFlow"] --> K["uiState: StateFlow"]
        K --> L["combine for complex state"]
        L --> M["UI Observes StateFlow"]
    end
    
    subgraph SideEffects ["Side Effects"]
        N[LaunchedEffect] --> O[One-time events]
        P[DisposableEffect] --> Q[Cleanup actions]
        R[SideEffect] --> S[Non-compose calls]
    end
    
    %% スタイリング
    classDef uiStateStyle fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef stateTypeStyle fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef stateFlowStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef sideEffectStyle fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    
    class A uiStateStyle
    class B,C,D,E,F,G,H,I stateTypeStyle
    class J,K,L,M stateFlowStyle
    class N,O,P,Q,R,S sideEffectStyle
```

## Architecture7: ナビゲーション設計パターン

```mermaid
graph TD
    subgraph NavigationArchitecture ["Navigation Architecture"]
        A[Routes Object] --> B[Type-safe Navigation]
        B --> C[NavHost Configuration]
        C --> D[Screen Transitions]
        
        subgraph ScreenTypes ["Screen Types"]
            E["List Screens|Pull to Refresh|Search/Filter"]
            F["Detail Screens|CRUD Operations|Form Validation"]
            G["Dialog Screens|Confirmation|Settings"]
        end
        
        subgraph AnimationPatterns ["Animation Patterns"]
            H["Horizontal Slide|Push/Pop"]
            I["Vertical Slide|Modal/Sheet"]
            J["Fade|Replace/Update"]
        end
    end
```

## Architecture8: データベース設計原則

```mermaid
erDiagram
    ENTITIES ||--o{ RELATIONSHIPS : has
    
    ENTITIES {
        string naming_convention "snake_case"
        primary_key id "Always Int or Long"
        timestamps created_at "Long timestamp"
        timestamps updated_at "Long timestamp"
        foreign_keys user_id "Reference pattern"
    }
    
    RELATIONSHIPS {
        string type "OneToMany or ManyToMany"
        boolean cascade "Delete strategy"
        string indexing "Performance optimization"
    }
    
    MIGRATION_STRATEGY ||--|| ENTITIES : applies_to
    
    MIGRATION_STRATEGY {
        boolean export_schema "true for production"
        version_control incremental "Version 1 2 3"
        fallback_strategy destructive "Development only"
        test_strategy comprehensive "All scenarios"
    }
```

## Architecture9: テスト戦略

```mermaid
graph TB
    subgraph TestArchitecture ["Test Architecture"]
        
        subgraph UnitTests ["Unit Tests"]
            A["ViewModel Tests|StateFlow testing|Business logic"]
            B["Repository Tests|Mock API/DB|Data transformation"]
            C["UseCase Tests|Business rules|Error scenarios"]
        end
        
        subgraph IntegrationTests ["Integration Tests"]
            D["API Tests|Network scenarios|Error responses"]
            E["Database Tests|Migration testing|Query validation"]
        end
        
        subgraph UITests ["UI Tests"]
            F["Screen Tests|User interactions|Navigation flows"]
            G["Component Tests|Reusable UI|State changes"]
        end
    end
    
    subgraph TestPrinciples ["Test Principles"]
        H["Given-When-Then|Pattern"]
        I["Arrange-Act-Assert|Pattern"]
        J["Mock External|Dependencies"]
        K["Test State|Isolation"]
    end
    
    %% スタイリング
    classDef unitTestStyle fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef integrationTestStyle fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef uiTestStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef principleStyle fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    
    class A,B,C unitTestStyle
    class D,E integrationTestStyle
    class F,G uiTestStyle
    class H,I,J,K principleStyle
```

## Architecture10: 機能拡張時のチェックリスト

```mermaid
flowchart TD
    Start([新機能開発開始]) --> A{既存パターンで対応可能？}
    
    A -->|Yes| B[既存パターン適用]
    A -->|No| C[新パターン設計検討]
    
    B --> D[Domain Model定義]
    C --> C1[Architecture Review]
    C1 --> C2[新パターン文書化]
    C2 --> D
    
    D --> E[Repository Interface作成]
    E --> F[DI設定追加]
    F --> G[ViewModel作成]
    G --> H[Screen作成]
    H --> I[Navigation追加]
    I --> J[Unit Test作成]
    J --> K[Integration Test]
    K --> L[UI Test]
    L --> M[Code Review]
    M --> End([機能完成])
    
    subgraph Considerations ["注意点"]
        N[既存コードへの影響確認]
        O[Performance測定]
        P[Security考慮]
        Q[Accessibility対応]
    end
    
    %% スタイリング
    classDef startEndStyle fill:#ffebee,stroke:#c62828,stroke-width:3px
    classDef decisionStyle fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef designStyle fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef implementationStyle fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef testStyle fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef considerationStyle fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class Start,End startEndStyle
    class A decisionStyle
    class B,C,C1,C2,D designStyle
    class E,F,G,H,I implementationStyle
    class J,K,L,M testStyle
    class N,O,P,Q considerationStyle
```

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