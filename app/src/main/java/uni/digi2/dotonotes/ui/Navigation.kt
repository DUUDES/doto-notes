@file:OptIn(ExperimentalFoundationApi::class)

package uni.digi2.dotonotes.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import uni.digi2.dotonotes.R
import uni.digi2.dotonotes.data.categories.CategoriesDao
import uni.digi2.dotonotes.data.tasks.TodoTasksDao
import uni.digi2.dotonotes.ui.screens.authorization.AuthScreen
import uni.digi2.dotonotes.ui.screens.categories.CategoriesListScreen
import uni.digi2.dotonotes.ui.screens.categories.TaskCategoriesViewModel
import uni.digi2.dotonotes.ui.screens.groupedTasks.GroupedTasks
import uni.digi2.dotonotes.ui.screens.profile.ProfileScreen
import uni.digi2.dotonotes.ui.screens.tasks.CompletedTasksScreen
import uni.digi2.dotonotes.ui.screens.tasks.TaskDetailsScreen
import uni.digi2.dotonotes.ui.screens.tasks.TodoListScreen
import uni.digi2.dotonotes.ui.screens.tasks.TodoViewModel
import uni.digi2.dotonotes.ui.theme.DoToTheme


@Composable
fun AppNavHost(
    navController: NavController
) {

    val tasksDao = TodoTasksDao()
    val categoriesDao = CategoriesDao()

    val tasksViewModel = TodoViewModel(tasksDao, categoriesDao)
    val categoriesViewModel = TaskCategoriesViewModel(categoriesDao)

    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.Auth.route,
    ) {
        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    tasksViewModel.stopObservation()
                    categoriesViewModel.stopObservation()

                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Auth.route)

                },
                onDeleteAccount = {
                    FirebaseAuth.getInstance().currentUser?.let { it1 ->
                        tasksViewModel.deleteAllTasks(it1.uid)
                    }

                    tasksViewModel.stopObservation()
                    categoriesViewModel.stopObservation()

                    FirebaseAuth.getInstance().currentUser?.delete()
                    navController.navigate(Screen.Auth.route)
                })
        }
        composable(Screen.GroupedTasks.route) {
            GroupedTasks(navController, tasksViewModel)
        }
        composable(Screen.Tasks.route) {
            TodoListScreen(navController, tasksViewModel)
        }
        composable(Screen.Categories.route) {
            CategoriesListScreen(categoriesViewModel)
        }
        composable(Screen.CompletedTasks.route) {
            CompletedTasksScreen(navController, tasksViewModel)
        }
        composable(Screen.Auth.route) {
            navController.popBackStack(route = Screen.Auth.route, inclusive = true)
            AuthScreen(navController = navController)
        }
        composable(route = "task_details") {
            TaskDetailsScreen(viewModel = tasksViewModel)
        }
    }
}

val LocalNavController = compositionLocalOf<NavController> { error("No NavController found") }

val items = listOf(
    Screen.Tasks,
    Screen.GroupedTasks,
    Screen.CompletedTasks,
    Screen.Profile,
)

@Composable
fun DoToApplication(navController: NavController) {

    CompositionLocalProvider(LocalNavController provides navController) {
            Scaffold(
                bottomBar = {
                    DoToTheme {
                        BottomNavigation {
                            val navBackStackEntry by LocalNavController.current.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            items.forEach { screen ->
                                val selected =
                                    currentDestination?.hierarchy?.any { it.route == screen.route }
                                        ?: false
                                val onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                BottomNavigationItem(
                                    icon = {
                                        Icon(
                                            screen.icon,
                                            contentDescription = stringResource(id = screen.title),
                                            tint = if (selected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(if (selected) 28.dp else 36.dp)
                                        )
                                    },
                                    alwaysShowLabel = false,
                                    label = {
                                        AnimatedVisibility(visible = selected) {
                                            Text(
                                                text = stringResource(id = screen.title),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    },
                                    selected = selected,
                                    onClick = onClick,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                innerPadding.calculateTopPadding()
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    AppNavHost(navController = navController)
                }
            }
    }

}

sealed class Screen(val route: String, val title: Int, val icon: ImageVector) {
    object Tasks : Screen("tasks", R.string.tasks_page, Icons.Filled.Home)
    object GroupedTasks : Screen("grouped-tasks", R.string.grouped_tasks_page, Icons.Filled.List)
    object CompletedTasks : Screen("completedTasks", R.string.completed_tasks_page, Icons.Filled.Done)
    object Profile : Screen("profile", R.string.profile_page, Icons.Default.Person)
    object Auth : Screen("auth", R.string.auth_page, Icons.Default.Home)
    object Categories : Screen("categories", R.string.categories_page, Icons.Default.List)
}
