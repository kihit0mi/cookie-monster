package cz.kihitomi.cookiemonster

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cz.kihitomi.cookiemonster.ui.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("cz.kihitomi.cookiemonster", appContext.packageName)
    }

    @Test
    fun mainActivity_displaysCoreUiElements() {
        composeTestRule.onNodeWithText("Cookie Monster").assertIsDisplayed()
        composeTestRule.onNodeWithText("MCP Server Host").assertIsDisplayed()
        composeTestRule.onNodeWithText("VIEW AGENT LOGS").assertIsDisplayed()
    }
}
