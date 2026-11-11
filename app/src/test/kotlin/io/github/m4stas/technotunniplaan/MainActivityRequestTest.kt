package io.github.m4stas.technotunniplaan

import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityRequestTest {
    private val intent = MainActivity.intentFor(ApplicationProvider.getApplicationContext(), Fixtures.CLASS_A)

    @Test fun freshStartOpensTheIntentEntity() {
        assertEquals(Fixtures.CLASS_A, MainActivity.initialRequest(null, intent))
    }

    @Test fun recreationAfterTheShellOpenedTheEntityDoesNotReplayIt() {
        val saved = Bundle().also { MainActivity.savePending(it, null) }
        assertNull(MainActivity.initialRequest(saved, intent))
    }

    @Test fun recreationBeforeTheShellOpenedTheEntityKeepsTheRequest() {
        val saved = Bundle().also { MainActivity.savePending(it, Fixtures.MARI) }
        assertEquals(Fixtures.MARI, MainActivity.initialRequest(saved, intent))
    }
}
