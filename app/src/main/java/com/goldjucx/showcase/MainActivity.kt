package com.goldjucx.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.goldjucx.showcase.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ShowcaseApp() }
    }
}

@androidx.compose.runtime.Composable
private fun ShowcaseApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "ui_showcase",
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }
    ) {
        composable("ui_showcase") {
            UIShowcaseScreen(
                onBack = null,
                onNavigateToDemo = { navController.navigate(it) }
            )
        }
        composable("demo_settings_page_config") {
            DemoSettingsPageConfig(onBack = { navController.popBackStack() })
        }
        composable("demo_settings_layout") {
            DemoSettingsLayout(onBack = { navController.popBackStack() })
        }
        composable("demo_bottom_sheet_drawer") {
            DemoBottomSheetDrawer(onBack = { navController.popBackStack() })
        }
        composable("demo_dialog") {
            DemoGoldjucXDialog(onBack = { navController.popBackStack() })
        }
        composable("demo_common_components") {
            DemoCommonComponents(onBack = { navController.popBackStack() })
        }
        composable("demo_material_preview") {
            DemoMaterialPreview(onBack = { navController.popBackStack() })
        }
    }
}
