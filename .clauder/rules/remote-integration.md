# リモートリポジトリ連携

このドキュメントは、Git操作とリモートリポジトリとの連携に関するベストプラクティスを記録します。

## 目次
- [Git設定](#git設定)
- [ブランチ戦略](#ブランチ戦略)
- [コミットガイドライン](#コミットガイドライン)
- [PR/MRテンプレート](#prmrテンプレート)
- [CI/CD設定](#cicd設定)
- [トラブルシューティング](#トラブルシューティング)

## Git設定

### 推奨設定
```bash
# ユーザー情報
git config user.name "Your Name"
git config user.email "your.email@example.com"

# 改行コード
git config core.autocrlf input  # Mac/Linux
git config core.autocrlf true   # Windows
```

### .gitignore
*（今後、必要に応じて追記）*

## ブランチ戦略

### ブランチ命名規則
- `main` - メインブランチ
- `develop` - 開発ブランチ
- `feature/xxx` - 機能追加
- `bugfix/xxx` - バグ修正
- `hotfix/xxx` - 緊急修正
- `release/xxx` - リリース準備

### ブランチ運用ルール
*（今後、チーム決定時に追記）*

## コミットガイドライン

### コミットメッセージ形式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type一覧
- `feat`: 新機能
- `fix`: バグ修正
- `docs`: ドキュメント
- `style`: フォーマット修正
- `refactor`: リファクタリング
- `test`: テスト追加・修正
- `chore`: ビルドプロセスやツールの変更

## PR/MRテンプレート

### Pull Requestテンプレート
```markdown
## 概要
変更の概要を記載

## 変更内容
- [ ] 変更点1
- [ ] 変更点2

## テスト
- [ ] Unit Test
- [ ] UI Test
- [ ] 手動テスト

## スクリーンショット
必要に応じて添付

## 関連Issue
#xxx
```

## CI/CD設定

### GitHub Actions
*（今後、設定時に追記）*

### その他のCIツール
*（今後、使用時に追記）*

## トラブルシューティング

### マージコンフリクト
*（今後、遭遇時に追記）*

### プッシュエラー
*（今後、遭遇時に追記）*

### その他の問題
*（今後、遭遇時に追記）*

---
最終更新: 2025-07-01