package id.pina.bacakomik.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.pina.bacakomik.ui.theme.PinaNavy
import id.pina.bacakomik.ui.theme.PinaRed
import id.pina.bacakomik.ui.theme.PinaTextSecondary
import id.pina.bacakomik.ui.detail.DetailScreen
import id.pina.bacakomik.ui.home.HomeScreen
import id.pina.bacakomik.ui.home.ExploreScreen
import id.pina.bacakomik.ui.library.LibraryScreen
import id.pina.bacakomik.ui.reader.ReaderScreen
import id.pina.bacakomik.ui.settings.ImportScreen
import id.pina.bacakomik.ui.settings.SettingsScreen

sealed class Tab(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlineIcon: ImageVector,
) {
    object Home : Tab("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Explore : Tab("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    object Favorit : Tab("favorit", "Favorit", Icons.Filled.Bookmark, Icons.Outlined.Bookmark)
    object Me : Tab("me", "Me", Icons.Filled.Person, Icons.Outlined.Person)
}

private val Tabs = listOf(Tab.Home, Tab.Explore, Tab.Favorit, Tab.Me)

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    // Show bottom bar only on main tabs (not detail/reader)
    val mainRoutes = Tabs.map { it.route }
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        containerColor = PinaNavy,
        bottomBar = {
            if (showBottomBar) PinaBottomNav(nav, currentRoute)
        },
    ) { padding ->
        AppNavGraph(nav, padding)
    }
}

@Composable
private fun PinaBottomNav(nav: NavHostController, currentRoute: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PinaNavy)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            val iconColor by animateColorAsState(
                targetValue = if (selected) Color.White else PinaTextSecondary,
                animationSpec = spring(),
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (selected) PinaRed else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) tab.filledIcon else tab.outlineIcon,
                    contentDescription = tab.label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun AppNavGraph(nav: NavHostController, padding: PaddingValues) {
    NavHost(
        navController = nav,
        startDestination = Tab.Home.route,
        modifier = Modifier.padding(padding),
    ) {
        composable(Tab.Home.route) {
            HomeScreen(onOpen = { nav.navigate("manga/$it") })
        }
        composable(Tab.Explore.route) {
            ExploreScreen(onOpen = { nav.navigate("manga/$it") })
        }
        composable(Tab.Favorit.route) {
            LibraryScreen(onOpen = { nav.navigate("manga/$it") })
        }
        composable(Tab.Me.route) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onImport = { nav.navigate("import") },
            )
        }
        composable("import") {
            ImportScreen(onBack = { nav.popBackStack() })
        }
        composable("manga/{slug}") {
            val slug = it.arguments?.getString("slug") ?: return@composable
            DetailScreen(
                slug = slug,
                onBack = { nav.popBackStack() },
                onReadChapter = { chSlug -> nav.navigate("read/$chSlug") },
            )
        }
        composable("read/{slug}") {
            val slug = it.arguments?.getString("slug") ?: return@composable
            ReaderScreen(
                slug = slug,
                onBack = { nav.popBackStack() },
                onNavigateChapter = { newSlug ->
                    nav.navigate("read/$newSlug") {
                        popUpTo("read/{slug}") { inclusive = true }
                    }
                },
            )
        }
    }
}
