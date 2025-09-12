package com.phuonghai.inspection

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.donut.assignment2.presentation.supervisor.profile.SupervisorProfileScreen
import org.junit.Rule
import org.junit.Test



import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.phuonghai.inspection.presentation.supervisor.profile.FakeSupervisorProfileViewModel
class SupervisorProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileScreen_displaysProfileInfo() {
        val fakeViewModel = FakeSupervisorProfileViewModel()

        composeRule.setContent {
            SupervisorProfileScreen(
                navController = rememberNavController(),
                viewModel = fakeViewModel
            )
        }

        composeRule.onNodeWithText("Test User").assertIsDisplayed()
        composeRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }
}