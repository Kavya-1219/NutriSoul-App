package com.simats.nutrisoul

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.simats.nutrisoul.data.UserViewModel
import com.simats.nutrisoul.ui.steps.StepsViewModel

@Composable
fun NavGraph(
    navController: NavHostController
) {
    val userViewModel: UserViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = { navController.navigate(Screen.Onboarding1.route) })
        }
        composable(Screen.Onboarding1.route) {
            OnboardingScreen(
                onNextClicked = { navController.navigate(Screen.Onboarding2.route) },
                onSkipClicked = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Onboarding2.route) {
            OnboardingScreen2(
                onNextClicked = { navController.navigate(Screen.Onboarding3.route) },
                onSkipClicked = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Onboarding3.route) {
            OnboardingScreen3(
                onGetStartedClicked = { navController.navigate(Screen.Register.route) },
                onSkipClicked = { navController.navigate(Screen.Login.route) },
                onLoginClicked = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(navController = navController)
        }
        navigation(startDestination = Screen.PersonalDetails.route, route = "main_graph") {
            composable(Screen.PersonalDetails.route) {
                PersonalDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onContinueClicked = { navController.navigate(Screen.BodyDetails.route) },
                    userViewModel = userViewModel
                )
            }
            composable(Screen.BodyDetails.route) {
                BodyDetailsScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.FoodPreferences.route) {
                FoodPreferencesScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.LifestyleAndActivity.route) {
                LifestyleAndActivityScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.Goals.route) {
                GoalsScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.GoalWeight.route) {
                GoalWeightScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.HealthConditions.route) {
                HealthConditionsScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.HealthDetails.route) {
                HealthDetailsScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.MealsPerDay.route) {
                MealPerDayScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.Home.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry("main_graph")
                }
                val stepsViewModel: StepsViewModel = hiltViewModel(parentEntry)
                HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = hiltViewModel(), stepsViewModel = stepsViewModel)
            }
            composable(Screen.WaterTracking.route) {
                WaterTrackingScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.StepsTracking.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry("main_graph")
                }
                val stepsViewModel: StepsViewModel = hiltViewModel(parentEntry)
                StepsTrackingScreen(navController = navController, viewModel = stepsViewModel)
            }

            composable(
                route = Screen.LogFood.route + "?autoScan={autoScan}",
                arguments = listOf(
                    androidx.navigation.navArgument("autoScan") {
                        type = androidx.navigation.NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val autoScan = backStackEntry.arguments?.getBoolean("autoScan") ?: false
                LogFoodScreen(
                    navController = navController, 
                    viewModel = hiltViewModel(),
                    autoScan = autoScan
                )
            }
            
            composable(Screen.MealPlan.route) {
                TodaysMealPlanScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable(Screen.AiTips.route) {
                AiTipsScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable(Screen.MindCare.route) {
                MindCareScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable(Screen.Recipes.route) {
                val userState by userViewModel.user.collectAsStateWithLifecycle()
                val email = userState?.email ?: ""
                RecipesScreen(navController = navController, userEmail = email)
            }
            composable(Screen.Insights.route) {
                NutritionInsightsScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController, userViewModel = userViewModel)
            }
            composable("profile") {
                ProfileScreen(navController = navController, userViewModel = userViewModel)
            }
            composable(
                route = "help?initialMessage={initialMessage}",
                arguments = listOf(
                    androidx.navigation.navArgument("initialMessage") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { _backStackEntry ->
                HelpSupportScreen(
                    navController = navController
                )
            }
            composable("about") {
                AboutScreen(navController = navController)
            }
        }
    }
}
