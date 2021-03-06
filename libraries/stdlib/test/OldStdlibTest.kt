package test.collections

import kotlin.*
import kotlin.io.*
import kotlin.test.*
import org.junit.Test as test

class OldStdlibTest() {
    @test fun testCollectionEmpty() {
        assertFalse {
            listOf(0, 1, 2).isEmpty()
        }
    }

    @test fun testCollectionSize() {
        assertTrue {
            listOf(0, 1, 2).size == 3
        }
    }

    @test fun testInputStreamIterator() {
        val x = ByteArray (10)

        for(index in 0..9) {
            x [index] = index.toByte()
        }

        x.inputStream().buffered().use { stream ->
            for(b in stream) {
                println(b)
            }
        }
    }
}
