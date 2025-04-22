package com.kouusei.restaurant

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kouusei.restaurant.data.api.HotPepperGourmetRepositoryImpl
import com.kouusei.restaurant.data.api.HotPepperGourmetService
import com.kouusei.restaurant.data.api.di.HotPepperGourmetModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    suspend fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.kouusei.restaurant", appContext.packageName)

        val service: HotPepperGourmetService =
            HotPepperGourmetModule.provideHotPepperGourmetService()
    }
}