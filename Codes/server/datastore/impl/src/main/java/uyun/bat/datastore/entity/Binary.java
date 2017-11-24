package uyun.bat.datastore.entity;

import java.util.Arrays;

public class Binary {
	private byte[] value;

	public Binary(byte[] value) {
		this.value = value;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Binary binary = (Binary) o;

		return Arrays.equals(value, binary.value);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

	@Override
	public String toString() {
		if (value == null)
			return null;
		StringBuilder sb = new StringBuilder(value.length * 2);
		for (byte b : value) {
			String t = Integer.toHexString(0xff & b);
			if (t.length() == 1)
				sb.append("0");
			sb.append(t);
		}
		return sb.toString();
	}
}
