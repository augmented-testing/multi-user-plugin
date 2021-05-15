package plugin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    JSONStateParserTest.class,
    DemoTest.class,
    SayTest.class
})

public class AllTests {

}
