package com.example.lockedin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            "Dashboard",
            "dashboard",
            defaultIcon = painterResource(id = R.drawable.homeicon),
            selectedIcon = painterResource(id = R.drawable.homeiconblack)
        ),
        BottomNavItem(
            "Progress",
            "my_progress_screen",
            defaultIcon = painterResource(id = R.drawable.progressicon),
            selectedIcon = painterResource(id = R.drawable.progressiconblack)
        ),
        BottomNavItem(
            "Profile",
            "profile_screen",
            defaultIcon = painterResource(id = R.drawable.personicon),
            selectedIcon = painterResource(id = R.drawable.personiconblack)
        )
    )

    BottomNavigation(
        backgroundColor = Color.Black,
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp) // Size of the circle
                            .clip(CircleShape)
                            .background(if (currentRoute == item.route) Color.White else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = if (currentRoute == item.route) item.selectedIcon else item.defaultIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(19.dp), // Icon size
                            tint = Color.Unspecified // Ensure the icon color is based on the drawable, not overridden
                        )
                    }
                },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = false,
                selectedContentColor = Color.Transparent,
                unselectedContentColor = Color.White,
            )
        }
    }
}


data class BottomNavItem(
    val label: String,
    val route: String,
    val defaultIcon: Painter,
    val selectedIcon: Painter
)