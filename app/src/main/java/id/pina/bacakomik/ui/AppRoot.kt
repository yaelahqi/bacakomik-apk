package id.pina.bacakomik.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.pina.bacakomik.ui.detail.DetailScreen
import id.pina.bacakomik.ui.home.HomeScreen
import id.pina.bacakomik.ui.library.LibraryScreen
import id.pina.bacakomik.ui.reader.ReaderScreen
import id.pina.bacakomik.ui.search.SearchScreen
import id.pina.bacakomik.ui.settings.ImportScreen
import id.pina.bacakomik.ui.settings.SettingsScreen

sealed class Tab(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Home : Tab("home", "Home", { Icon(Icons.Outlined.Home, null) })
    object Search : Tab("search", "Search", { Icon(Icons.Outlined.Search, null) })
    object Library : Tab("library", "Library", { Icon(Icons.Outlined.Bookmark, null) })
    object Settings : Tab("settings", "Settings", { Icon(Icons.Outlined.Settings, null) })
}

private val Tabs = listOf(Tab.Home, Tab.Search, Tab.Library, Tab.Settings)

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in Tabs.map { it.route }

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(nav, currentRoute) }
    ) { padding ->
        AppNavGraph(nav, padding)
    }
}

@Composable
private fun BottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar {
        Tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = tab.icon,
                label = { Text(tab.label) },
            )
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
        composable(Tab.Home.route) { HomeScreen(onOpen = { nav.navigate("manga/$it") }) }
        composable(Tab.Search.route) { SearchScreen(onOpen = { nav.navigate("manga/$it") }) }
        composable(Tab.Library.route) {
            LibraryScreen(onOpen = { nav.navigate("manga/$it") })
        }
        composable(Tab.Settings.route) {
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
