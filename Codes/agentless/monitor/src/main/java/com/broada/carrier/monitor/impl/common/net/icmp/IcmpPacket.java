package com.broada.carrier.monitor.impl.common.net.icmp;

import java.security.InvalidParameterException;

public class IcmpPacket {
	public static final byte TYPE_ECHO_REPLY = 0;
	public static final byte TYPE_ECHO_REQUEST = 8;
	public static final byte TYPE_IPV6_ECHO_REPLY = (byte) 129;
	public static final byte TYPE_IPV6_ECHO_REQUEST = (byte) 128;	

	private byte type;
	private byte code;
	private short id;
	private short seq;
	private byte[] data;

	public byte[] decode() {
		byte[] buf = new byte[data.length + 8]; // 8是指ICMP包头占用的字节数

		buf[0] = type;
		buf[1] = code;
		buf[2] = 0;
		buf[3] = 0;
		decodeShort(id, buf, 4);
		decodeShort(seq, buf, 6);
		copyByteArray(data, 0, buf, 8, data.length);

		short cksum = makeCksum(buf, buf.length);
		decodeShort(cksum, buf, 2);

		return buf;
	}

	static short makeCksum(byte[] buf, int length) {
		int sum = 0;

		int i = 0;
		for (; (i + 1) < length; i += 2) {
			sum += encodeShort(buf, i) & 0xffff;
			if ((sum & 0xffffffff) > 0xfffffff)
				sum = (sum >> 16) + (sum & 0xffff);
		}

		if (i < length)
			sum += (buf[i] & 0x00ff) << 8;

		while (sum > 0xffff)
			sum = (sum >> 16) + (sum & 0xffff);

		return (short) ~(sum & 0xffff);
	}

	static void copyByteArray(byte[] src, int srcOffset, byte[] dest, int destOffset, int length) {
		int size = src.length - srcOffset;
		size = size < length ? size : length;

		if (dest.length - destOffset < size)
			throw new InvalidParameterException("复制字节数组，目标数组长度不足");

		for (int i = 0; i < size; i++)
			dest[destOffset + i] = src[srcOffset + i];
	}

	static void decodeShort(short value, byte[] buf, int offset) {
		buf[offset] = (byte) ((value & 0xff00) >> 8);
		buf[offset + 1] = (byte) (value & 0xff);
	}

	static short encodeShort(byte[] buf, int offset) {
		short value;
		value = (short) (buf[offset] << 8);
		value += (short) (buf[offset + 1] & 0xff);
		return value;
	}

	public boolean encode(byte[] buf, int offset, int length) {
		type = buf[offset];
		code = buf[offset + 1];
		id = encodeShort(buf, offset + 4);
		seq = encodeShort(buf, offset + 6);

		data = new byte[length - 8];
		copyByteArray(buf, offset + 8, data, 0, data.length);
		return true;
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
		this.code = code;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}
}