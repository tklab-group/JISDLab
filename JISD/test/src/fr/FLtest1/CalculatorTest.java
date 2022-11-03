package fr.FLtest1;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalculatorTest {

	Calculator c = new Calculator();

	@Test
	public void testSum() {

		assertEquals(4, c.calculate("+", 3, 1));

	}

	@Test
	public void testSubs() {

		assertEquals(2, c.calculate("-", 3, 1));

	}

	@Test
	public void testMul() {

		assertEquals(8, c.calculate("*", 4, 2));

	}

	@Test
	public void testDiv() {

		assertEquals(2, c.calculate("/", 12, 6));

	}

	@Ignore
	@Test
	public void testIgnore() {

		assertEquals(2, c.calculate("/", 12, 6));

	}

}
