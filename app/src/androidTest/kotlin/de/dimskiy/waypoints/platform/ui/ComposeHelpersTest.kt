package de.dimskiy.waypoints.platform.ui

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComposeHelpersTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun getMiles_2_WHEN_isNotMetric() {
        composeTestRule.setContent {
            assertEquals(
                "2\nmi",
                GetDistanceFormatted(3.219, isMiles = true)
            )
        }
    }

    @Test
    fun getMiles_2_3_WHEN_isNotMetric() {
        composeTestRule.setContent {
            assertEquals(
                "2.3\nmi",
                GetDistanceFormatted(3.701, isMiles = true)
            )
        }
    }

    @Test
    fun getFeet_1581_WHEN_isNotMetric() {
        composeTestRule.setContent {
            assertEquals(
                "1,581\nft",
                GetDistanceFormatted(0.482, isMiles = true)
            )
        }
    }

    @Test
    fun getKm_2_WHEN_isMetric() {
        composeTestRule.setContent {
            assertEquals(
                "2\nkm",
                GetDistanceFormatted(2.0, isMiles = false)
            )
        }
    }

    @Test
    fun getKm_2_3_WHEN_isMetric() {
        composeTestRule.setContent {
            assertEquals(
                "2.3\nkm",
                GetDistanceFormatted(2.3, isMiles = false)
            )
        }
    }

    @Test
    fun getM_300_WHEN_isMetric() {
        composeTestRule.setContent {
            assertEquals(
                "300\nm",
                GetDistanceFormatted(0.3, isMiles = false)
            )
        }
    }
}