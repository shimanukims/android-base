package com.example.androidbaseapp.domain.repository

import com.example.androidbaseapp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * ユーザーデータへのアクセスを抽象化するRepository Interface
 * 
 * 設計方針:
 * - 全ての suspend 関数は Result型でエラーハンドリングを統一
 * - Flow型はエラーを内包しないため、そのまま使用
 * - AppError による統一的なエラー表現
 */
interface UserRepository {
    /**
     * ユーザー一覧をFlowで取得
     * ローカルDBからリアクティブにデータを取得
     * エラーは上位層（ViewModel）で refreshUsers() の結果として処理
     */
    fun getUsers(): Flow<List<User>>
    
    /**
     * リモートAPIからユーザー一覧を取得してローカルDBに保存
     * 
     * @return Result<Unit> 成功時はUnit、失敗時はAppErrorがラップされた例外
     */
    suspend fun refreshUsers(): Result<Unit>
    
    /**
     * 指定IDのユーザーを取得
     * 
     * @param id ユーザーID
     * @return Result<User?> 成功時はUser（存在しない場合はnull）、失敗時はAppErrorがラップされた例外
     */
    suspend fun getUserById(id: Int): Result<User?>
}