/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.configuration.properties;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class LocalPropertiesTest {

	private static final String TEST = "test";

	@Test
	public void testAsIntsNormal() {
		LocalProperties.set(TEST, "1 2 3");
		assertEquals(Arrays.asList(new Integer[] { 1, 2, 3 }), LocalProperties.getAsIntList(TEST));
		LocalProperties.set(TEST, "1 2 3");
		assertEquals(Arrays.asList(new Integer[] { 1, 2, 3 }), LocalProperties.getAsIntList(TEST));
	}

	@Test
	public void testAsIntsEmpty() {
		LocalProperties.set(TEST, "");
		assertEquals(Arrays.asList(new Integer[] {}), LocalProperties.getAsIntList(TEST));
	}

	@Test
	public void testStringToIntListNull() {
		assertEquals(null, LocalProperties.stringToIntList(null));
	}

	@Test
	public void testAsIntsDefault() {
		assertEquals(Arrays.asList(new Integer[] { 1, 2, 3 }), LocalProperties.getAsIntList("not a property", new Integer[] { 1, 2, 3 }));
	}

	@Test
	public void testStringToIntListSingleA() {
		assertEquals(Arrays.asList(new Integer[] { 1 }), LocalProperties.stringToIntList("1,"));
	}

	@Test
	public void testStringToIntListSingleB() {
		assertEquals(Arrays.asList(new Integer[] { 1 }), LocalProperties.stringToIntList("1"));
	}

	@Test
	public void testGetAsInt() {
		LocalProperties.set(LocalPropertiesTest.TEST, "1");
		assertEquals(1, LocalProperties.getAsInt(LocalPropertiesTest.TEST));
		assertEquals(2, LocalProperties.getAsInt("not a property", 2));
	}

	@Test(expected = NullPointerException.class)
	public void testGetAsIntThrowsNPEWhenPropertyIsUndefined() {
		LocalProperties.getAsInt("not a property");
	}

	@Test(expected = NumberFormatException.class)
	public void testGetAsIntThrowsNumberFormatExceptionWhenPropertyIsNotANumber() {
		LocalProperties.set(LocalPropertiesTest.TEST, "not a number");
		LocalProperties.getAsInt(LocalPropertiesTest.TEST);
	}
}
