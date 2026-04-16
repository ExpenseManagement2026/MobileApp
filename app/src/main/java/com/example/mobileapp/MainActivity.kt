package com.example.mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.presentation.budget.BudgetScreen
import com.example.mobileapp.presentation.dashboard.DashboardScreen
import com.example.mobileapp.presentation.home.HomeScreen
import com.example.mobileapp.presentation.theme.MobileAppTheme

data class NavItem(val label: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Search", Icons.Filled.Search),
        NavItem("Add", Icons.Filled.Add),
        NavItem("Budget", Icons.Filled.AccountBalance),
        NavItem("Dashboard", Icons.Filled.Poll)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    val greenColor = Color(0xFF00BFA5)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                navItems.forEachIndexed { index, item ->
                    if (index == 2) {
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(greenColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = greenColor,
                                selectedTextColor = greenColor,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color(0xFF9E9E9E),
                                unselectedTextColor = Color(0xFF9E9E9E)
                            )
                        )
                    } else {
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = greenColor,
                                selectedTextColor = greenColor,
                                indicatorColor = greenColor.copy(alpha = 0.12f),
                                unselectedIconColor = Color(0xFF9E9E9E),
                                unselectedTextColor = Color(0xFF9E9E9E)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedIndex) {
                0 -> HomeScreen()
                3 -> BudgetScreen()
                4 -> DashboardScreen()
                else -> PlaceholderScreen(label = navItems[selectedIndex].label)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 22.sp, color = Color.Gray)
            Text(text = "Đang kết nối dữ liệu từ đồng đội...", fontSize = 14.sp, color = Color.LightGray)
        }
    }
}
