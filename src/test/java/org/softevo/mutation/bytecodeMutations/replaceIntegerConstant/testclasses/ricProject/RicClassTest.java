package org.softevo.mutation.bytecodeMutations.replaceIntegerConstant.testForOwnClass.ricProject;

import junit.framework.TestCase;

public class RicClassTest extends TestCase {
	private RicClass ric;

	public void setUp() {
		ric = new RicClass();
	}

	public void testMethod1() {
		assertEquals(5 * 50, ric.method1(50));
	}

	public void testMethod2() {
		assertEquals(500l, ric.method2());
	}

	public void testMethod3() {
		assertTrue(ric.method3(5));
	}

}