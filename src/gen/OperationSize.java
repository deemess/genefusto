package gen;

public enum OperationSize {

	BYTE(0x80, 0xFF), WORD(0x8000, 0xFFFF), LONG(0x8000_0000L, 0xFFFF_FFFFL);
	
	long msb;
	long max;

	OperationSize(long msb, long maxSize) {
		this.msb = msb;
		this.max = maxSize;
	}

	public long getMsb() {
		return this.msb;
	}
	
	public long getMax() {
		return this.max;
	}
	
}
