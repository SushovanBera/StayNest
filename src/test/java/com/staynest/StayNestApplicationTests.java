package com.staynest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class StayNestApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring application context initializes cleanly with all entities and beans
    }
}
