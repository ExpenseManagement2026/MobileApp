package com.example.mobileapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileapp.domain.repository.TransactionRepository
import com.example.mobileapp.presentation.search.SearchScreen
import com.example.mobileapp.presentation.search.SearchViewModel

@Composable
fun AppNavGraph(transactionRepository: TransactionRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = root.Search.route
    ) {
        composable(root.Search.route) {
            val vm: SearchViewModel = viewModel(
                factory = SearchViewModel.Factory(transactionRepository)
            )
            SearchScreen(
                navController = navController,
                viewModel = vm
            )
        }
    }
}