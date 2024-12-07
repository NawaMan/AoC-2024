package common;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import functionalj.function.Func;
import functionalj.lens.Access;

public abstract class BaseTest implements AocCommon, Testable, Func, Access {
    
    @Rule
    public TestName testName = new TestName();
    
    
    @Before
    public void before() {
        println("==| " + testName.getMethodName() + " |==");
    }
    
    @After
    public void after() {
        println("--| " + testName.getMethodName() + " |--");
        println();
    }

}
