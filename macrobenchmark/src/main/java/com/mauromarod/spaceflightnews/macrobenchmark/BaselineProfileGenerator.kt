package com.mauromarod.spaceflightnews.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE = "com.mauromarod.spaceflightnews"

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generate() = baselineRule.collect(
        packageName = PACKAGE,
        profileBlock = {
            startActivityAndWait()

            // If login screen appears, tap "Continue as Guest" button
            val guestButton = device.wait(
                Until.findObject(By.text("CONTINUE AS GUEST")),
                5_000
            )
            guestButton?.click()
            device.waitForIdle()

            // Wait for article list
            device.wait(Until.hasObject(By.desc("news_article_list")), 8_000)

            // Scroll feed
            val list = device.findObject(By.desc("news_article_list"))
            list?.fling(Direction.DOWN)
            device.waitForIdle()
            list?.fling(Direction.UP)
            device.waitForIdle()

            // Search → open first result
            val searchField = device.findObject(By.desc("search_bar_field"))
            searchField?.click()
            searchField?.text = "Space"
            device.waitForIdle()
            device.wait(Until.findObject(By.desc("article_card")), 5_000)?.click()
            device.wait(Until.hasObject(By.desc("detail_content")), 3_000)

            // Back
            device.pressBack()
            device.wait(Until.hasObject(By.desc("news_article_list")), 3_000)

            // Open first article from feed
            device.wait(Until.findObject(By.desc("article_card")), 3_000)?.click()
            device.wait(Until.hasObject(By.desc("detail_content")), 3_000)
            device.waitForIdle()

            // Back
            device.pressBack()
            device.wait(Until.hasObject(By.desc("news_article_list")), 3_000)
        },
    )
}
