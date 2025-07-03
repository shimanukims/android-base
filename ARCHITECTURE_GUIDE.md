# Android Base App - アーキテクチャガイド

> 新規参画者向け基本構成説明および機能拡張時の設計方針

## 1. 全体アーキテクチャ（Clean Architecture + MVVM）

```mermaid
graph TB
    subgraph "📱 Presentation Layer"
        direction TB
        Screen[Screen<br/>Jetpack Compose]
        ViewModel[ViewModel<br/>StateFlow + UiState]
        
        Screen --> ViewModel
    end
    
    subgraph "🎯 Domain Layer"
        direction TB
        Repository[Repository Interface]
        Models[Domain Models]
        UseCases[Use Cases<br/>※必要に応じて追加]
        
        Repository --> Models
    end
    
    subgraph "🔄 Data Layer"
        direction TB
        RepoImpl[Repository Implementation]
        
        subgraph "Remote"
            API[Retrofit API]
            DTOs[Data Transfer Objects]
        end
        
        subgraph "Local"
            DAO[Room DAO]
            Entities[Database Entities]
        end
        
        RepoImpl --> API
        RepoImpl --> DAO
        API --> DTOs
        DAO --> Entities
    end
    
    subgraph "⚙️ DI Layer"
        direction LR
        NetworkMod[NetworkModule]
        DatabaseMod[DatabaseModule]
        RepoMod[RepositoryModule]
    end
    
    ViewModel --> Repository
    Repository --> RepoImpl
    
    NetworkMod -.provides.-> API
    DatabaseMod -.provides.-> DAO
    RepoMod -.binds.-> RepoImpl
```

## 2. 新規画面作成時の標準パターン

```mermaid
flowchart TD
    A[新機能要件] --> B{データソースは？}
    
    B -->|API + DB| C[標準パターン]
    B -->|APIのみ| D[軽量パターン]
    B -->|DBのみ| E[ローカルパターン]
    
    subgraph "📋 標準パターン（推奨）"
        C --> C1[1. Domain Model作成]
        C1 --> C2[2. Repository Interface定義]
        C2 --> C3[3. API + DAO作成]
        C3 --> C4[4. Repository Implementation]
        C4 --> C5[5. ViewModel作成]
        C5 --> C6[6. Screen作成]
        C6 --> C7[7. DI設定]
        C7 --> C8[8. Navigation設定]
    end
    
    subgraph "🚀 軽量パターン"
        D --> D1[Domain Model + API]
        D1 --> D2[Repository Simple]
        D2 --> D3[ViewModel + Screen]
    end
    
    subgraph "💾 ローカルパターン"
        E --> E1[Domain Model + DAO]
        E1 --> E2[Repository Local]
        E2 --> E3[ViewModel + Screen]
    end
```

## 3. データフロー設計原則

```mermaid
sequenceDiagram
    participant UI as 📱 UI (Screen)
    participant VM as 🧠 ViewModel
    participant Repo as 📦 Repository
    participant API as 🌐 Remote API
    participant DB as 💾 Local DB
    
    Note over UI,DB: 🔄 標準的なデータフロー
    
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
    Repo-->>VM: Flow<DomainModel>
    VM->>VM: Transform to UiState
    VM-->>UI: StateFlow<UiState>
    UI->>UI: Recomposition
```

## 4. エラーハンドリング統一設計

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
```

## 5. DI設計方針（Hilt）

```mermaid
graph TB
    subgraph "🏗️ Module設計原則"
        A[機能別Module分割]
        B[Scope適切設定]
        C[Interface活用]
        D[Test可能性確保]
    end
    
    subgraph "📁 Moduleパターン"
        direction TB
        
        subgraph "Core Modules"
            NetMod[NetworkModule<br/>@Singleton]
            DBMod[DatabaseModule<br/>@Singleton]
        end
        
        subgraph "Feature Modules"
            UserMod[UserModule<br/>Repository + UseCase]
            AuthMod[AuthModule<br/>※将来追加想定]
            SettingMod[SettingsModule<br/>※将来追加想定]
        end
        
        subgraph "Test Modules"
            TestNetMod[TestNetworkModule]
            TestDBMod[TestDatabaseModule]
        end
    end
    
    subgraph "🎯 Scope戦略"
        SingletonScope["@Singleton<br/>DB, API, Repository"]
        ViewModelScope["@ViewModelScoped<br/>ViewModel"]
        ActivityScope["@ActivityScoped<br/>Navigation"]
    end
```

## 6. UI状態管理パターン

```mermaid
graph LR
    subgraph "🎨 UiState Design Pattern"
        A[UiState Data Class] --> B[Loading State]
        A --> C[Success State]
        A --> D[Error State]
        A --> E[Empty State]
        
        B --> F[isLoading: Boolean]
        C --> G[data: List&lt;T&gt;]
        D --> H[errorMessage: String?<br/>canRetry: Boolean]
        E --> I[isEmpty: Boolean]
    end
    
    subgraph "🔄 StateFlow Pattern"
        J[_uiState: MutableStateFlow] --> K[uiState: StateFlow]
        K --> L[combine() for complex state]
        L --> M[UI Observes StateFlow]
    end
    
    subgraph "⚡ Side Effects"
        N[LaunchedEffect] --> O[One-time events]
        P[DisposableEffect] --> Q[Cleanup actions]
        R[SideEffect] --> S[Non-compose calls]
    end
```

## 7. ナビゲーション設計パターン

```mermaid
graph TD
    subgraph "🧭 Navigation Architecture"
        A[Routes Object] --> B[Type-safe Navigation]
        B --> C[NavHost Configuration]
        C --> D[Screen Transitions]
        
        subgraph "📱 Screen Types"
            E[List Screens<br/>- Pull to Refresh<br/>- Search/Filter]
            F[Detail Screens<br/>- CRUD Operations<br/>- Form Validation]
            G[Dialog Screens<br/>- Confirmation<br/>- Settings]
        end
        
        subgraph "🎬 Animation Patterns"
            H[Horizontal Slide<br/>Push/Pop]
            I[Vertical Slide<br/>Modal/Sheet]
            J[Fade<br/>Replace/Update]
        end
    end
```

## 8. データベース設計原則

```mermaid
erDiagram
    ENTITIES ||--o{ RELATIONSHIPS : has
    
    ENTITIES {
        string naming_convention "snake_case"
        primary_key id "Always Int/Long"
        timestamps created_at "Long timestamp"
        timestamps updated_at "Long timestamp"
        foreign_keys user_id "Reference pattern"
    }
    
    RELATIONSHIPS {
        string type "OneToMany/ManyToMany"
        boolean cascade "Delete strategy"
        string indexing "Performance optimization"
    }
    
    MIGRATION_STRATEGY ||--|| ENTITIES : applies_to
    
    MIGRATION_STRATEGY {
        boolean export_schema "true for production"
        version_control incremental "Version 1,2,3..."
        fallback_strategy destructive "Development only"
        test_strategy comprehensive "All scenarios"
    }
```

## 9. テスト戦略

```mermaid
graph TB
    subgraph "🧪 Test Architecture"
        
        subgraph "Unit Tests"
            A[ViewModel Tests<br/>- StateFlow testing<br/>- Business logic]
            B[Repository Tests<br/>- Mock API/DB<br/>- Data transformation]
            C[UseCase Tests<br/>- Business rules<br/>- Error scenarios]
        end
        
        subgraph "Integration Tests"
            D[API Tests<br/>- Network scenarios<br/>- Error responses]
            E[Database Tests<br/>- Migration testing<br/>- Query validation]
        end
        
        subgraph "UI Tests"
            F[Screen Tests<br/>- User interactions<br/>- Navigation flows]
            G[Component Tests<br/>- Reusable UI<br/>- State changes]
        end
    end
    
    subgraph "🎯 Test Principles"
        H[Given-When-Then<br/>Pattern]
        I[Arrange-Act-Assert<br/>Pattern]
        J[Mock External<br/>Dependencies]
        K[Test State<br/>Isolation]
    end
```

## 10. 機能拡張時のチェックリスト

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
    
    subgraph "⚠️ 注意点"
        N[既存コードへの影響確認]
        O[Performance測定]
        P[Security考慮]
        Q[Accessibility対応]
    end
```

## 📝 開発時の重要原則

### 🎯 設計原則
- **Single Responsibility**: 各クラスは単一の責任を持つ
- **Dependency Inversion**: 抽象に依存し、具象に依存しない
- **Testability**: テストしやすい設計を心がける
- **Consistency**: 既存パターンとの一貫性を保つ

### 🔄 データフロー原則
- **Unidirectional**: データは一方向に流れる
- **Immutable State**: 状態は不変オブジェクトで管理
- **Reactive**: Flow/StateFlowによるリアクティブプログラミング
- **Error Handling**: 統一されたエラーハンドリング

### 🚀 パフォーマンス原則
- **Lazy Loading**: 必要な時に必要なデータを読み込む
- **Caching Strategy**: 適切なキャッシュ戦略
- **Background Processing**: UI スレッドをブロックしない
- **Memory Management**: メモリリークの防止