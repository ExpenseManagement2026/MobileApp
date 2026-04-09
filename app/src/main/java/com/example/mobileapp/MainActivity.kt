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
import com.example.mobileapp.presentation.dashboard.DashboardScreen
import com.example.mobileapp.ui.theme.MobileAppTheme

// Model cho mỗi tab trong bottom nav
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
        NavItem("Home",      Icons.Filled.Home),
        NavItem("Search",    Icons.Filled.Search),
        NavItem("Add",       Icons.Filled.Add),
        NavItem("Budget",    Icons.Filled.Wallet),
        NavItem("Dashboard", Icons.Filled.BarChart)
    )

    var selectedIndex by remember { mutableIntStateOf(4) } // Dashboard mặc định
    val greenColor = Color(0xFF26A480)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    if (index == 2) {
                        // Nút "Add" ở giữa - thiết kế nổi bật
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
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
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
        // Hiển thị màn hình tương ứng với tab đang chọn
        when (selectedIndex) {
            4 -> DashboardScreen(modifier = Modifier.padding(innerPadding))
            else -> PlaceholderScreen(
                label = navItems[selectedIndex].label,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// Màn hình tạm cho các tab chưa implement
@Composable
fun PlaceholderScreen(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}
