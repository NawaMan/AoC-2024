package common;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class BaseTest implements AocCommon, Testable {
    
    @Rule
    public TestName testName = new TestName();
    
    
    @Before
    public void before() {
        println("==| " + testName.getMethodName() + " |==");
        println();
    }
    
    @After
    public void after() {
        println();
        println("--| " + testName.getMethodName() + " |--");
        println();
        println();
        println();
    }

}
