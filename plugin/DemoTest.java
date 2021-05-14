package plugin;

import static org.junit.Assert.assertEquals;

import org.junit.*;

public class DemoTest {

    @Test
    public void testHello() {
        Demo demo = new Demo();
        assertEquals("hello you", demo.hello());
    }
    
}
