package uyun.bat.datastore.api.util;


import org.junit.Test;

public class PreConditionsTest {

	@Test
	public void testCheckArgumentBoolean() {
		PreConditions.checkArgument(true);;
	}

	@Test
	public void testCheckArgumentBooleanObject() {
		PreConditions.checkArgument(true, "");
	}

	@Test
	public void testCheckArgumentBooleanStringObjectArray() {
		PreConditions.checkArgument(true, "", new String[]{});
	}

	@Test
	public void testCheckStateBoolean() {
		PreConditions.checkState(true);
	}

	@Test
	public void testCheckStateBooleanObject() {
		PreConditions.checkState(true, "test");
	}

	@Test
	public void testCheckStateBooleanStringObjectArray() {
		PreConditions.checkState(true, "test", new String[]{});
	}

	@Test
	public void testCheckNotNullT() {
		PreConditions.checkNotNull(new Object());
	}

	@Test
	public void testCheckNotNullTObject() {
		PreConditions.checkNotNull(new Object(), new Object());
	}

	@Test
	public void testCheckNotNullTStringObjectArray() {
		PreConditions.checkNotNull(new Object(), "test", new String[]{});
	}

	@Test
	public void testCheckElementIndexIntInt() {
		PreConditions.checkElementIndex(0, 10);
	}

	@Test
	public void testCheckElementIndexIntIntString() {
		PreConditions.checkElementIndex(0, 10, "");
	}

	@Test
	public void testCheckPositionIndexIntInt() {
		PreConditions.checkPositionIndex(0, 10);
	}

	@Test
	public void testCheckPositionIndexIntIntString() {
		PreConditions.checkPositionIndex(0, 10, "");
	}

	@Test
	public void testCheckPositionIndexes() {
		PreConditions.checkPositionIndexes(0, 10, 40);
	}

	@Test
	public void testFormat() {
		PreConditions.format("", new Object[]{});
	}

}
