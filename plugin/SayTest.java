package plugin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SayTest {
    
    @Test
    public void testSayHello() {
        String result = Say.sayHello();
        assertEquals("hello you", result);
    }
}
