package ast.projects.passwordmanager;

//import static org.junit.Assert.assertTrue;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import org.apache.logging.log4j.LogManager;

public class AppTest {

    @Test
    public void testAddition() {
    	LogManager.getLogger().info("This is an info message from test code");
        int result = 1 + 2;
        assertThat(result).isEqualTo(3);
    }
}