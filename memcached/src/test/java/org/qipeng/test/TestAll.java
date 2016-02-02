package org.qipeng.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestSpringSpyMemcached.class,
	TestSpringXMemcached.class,
	TestSpyMemcached.class,
	TestXMemcached.class
})
public class TestAll {
	//执行所有的测试类
}
