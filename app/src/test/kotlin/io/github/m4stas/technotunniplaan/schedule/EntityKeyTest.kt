package io.github.m4stas.technotunniplaan.schedule
import org.junit.Test
import kotlin.test.assertEquals
class EntityKeyTest {
    @Test fun normalizesNames() {
        assertEquals("mari näidis", entityKey("Mari Näidis"))
        assertEquals("mari näidis", entityKey("  Näidis   Mari "))
        assertEquals("k-xx-24a", entityKey("K-XX-24A"))
    }
}
