package org.backend;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StyloUserManagementServiceApplicationTests {

    // Disabled temporarily because @SpringBootTest loads the full application context,
    // which requires a configured database. This test will be re-enabled once
    // test-specific datasource (H2/Testcontainers) is configured.
    //@Test
    void contextLoads() {
    }

}
