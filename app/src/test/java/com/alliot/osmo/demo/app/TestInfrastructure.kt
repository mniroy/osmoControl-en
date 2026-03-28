package com.alliot.osmo.demo.app

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TestInfrastructure {
    @Test
    fun `app test infrastructure wiring`() = runTest {
        assertEquals(4, 2 + 2)
    }
}
