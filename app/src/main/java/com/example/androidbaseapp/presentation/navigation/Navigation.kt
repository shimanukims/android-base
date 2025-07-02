package com.example.androidbaseapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidbaseapp.presentation.userdetail.UserDetailScreen
import com.example.androidbaseapp.presentation.userlist.UserListScreen

object Routes {
    const val USER_LIST = "user_list"
    const val USER_DETAIL = "user_detail/{userId}"
    
    fun userDetail(userId: Int) = "user_detail/$userId"
}

@Composable
fun AndroidBaseNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.USER_LIST,
        modifier = modifier
    ) {
        composable(Routes.USER_LIST) {
            UserListScreen(
                onNavigateToDetail = { userId ->
                    navController.navigate(Routes.userDetail(userId))
                }
            )
        }
        
        composable(
            route = Routes.USER_DETAIL,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserDetailScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}