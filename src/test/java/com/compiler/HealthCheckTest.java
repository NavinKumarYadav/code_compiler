package com.compiler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HealthCheckTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    void basicArithmetic() {
        assertEquals(4, 2 + 2);
        assertNotEquals(5, 2 + 2);
    }
}