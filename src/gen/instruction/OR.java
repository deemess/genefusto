package gen.instruction;

import gen.Gen68;
import gen.GenInstruction;
import gen.Size;

public class OR implements GenInstructionHandler {

	final Gen68 cpu;
	
	public OR(Gen68 cpu) {
		this.cpu = cpu;
	}
	
//	NAME
//	OR -- Logical OR
//
//SYNOPSIS
//	OR	<ea>,Dn
//	OR	Dn,<ea>
//
//	Size = (Byte, Word, Long)
//
//FUNCTION
//	Performs an OR operation on the destination operand
//	with the source operand.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|-----------|-----------|-----------|-----------|
//	| 1 | 0 | 0 | 0 | REGISTER  |  OP-MODE  |    MODE   | REGISTER  |
//	----------------------------------------=========================
//	                                                   <ea>
//
//OP-MODE
//	Byte	Word	Long	Operation
//	~~~~	~~~~	~~~~	~~~~~~~~~
//	000		001		010		<ea> OR <Dn> --> <Dn>
//	100		101		110		<Dn> OR <ea> --> <ea>
//
//REGISTER
//	If <ea> specifies destination operand, addressing modes allowed are:
//	--------------------------------- -------------------------------
//	|Addressing Mode|Mode| Register | |Addressing Mode|Mode|Register|
//	|-------------------------------| |-----------------------------|
//	|      Dn       | -  |    -     | |    Abs.W      |111 |  000   |
//	|-------------------------------| |-----------------------------|
//	|      An       | -  |    -     | |    Abs.L      |111 |  001   |
//	|-------------------------------| |-----------------------------|
//	|     (An)      |010 |N� reg. An| |   (d16,PC)    | -  |   -    |
//	|-------------------------------| |-----------------------------|
//	|     (An)+     |011 |N� reg. An| |   (d8,PC,Xi)  | -  |   -    |
//	|-------------------------------| |-----------------------------|
//	|    -(An)      |100 |N� reg. An| |   (bd,PC,Xi)  | -  |   -    |
//	|-------------------------------| |-----------------------------|
//	|    (d16,An)   |101 |N� reg. An| |([bd,PC,Xi],od)| -  |   -    |
//	|-------------------------------| |-----------------------------|
//	|   (d8,An,Xi)  |110 |N� reg. An| |([bd,PC],Xi,od)| -  |   -    |
//	|-------------------------------| |-----------------------------|
//	|   (bd,An,Xi)  |110 |N� reg. An| |    #data      | -  |   -    |
//	|-------------------------------| -------------------------------
//	|([bd,An,Xi]od) |110 |N� reg. An|
//	|-------------------------------|
//	|([bd,An],Xi,od)|110 |N� reg. An|
//	---------------------------------
//
//	If <ea> specifies source operand, addressing modes allowed are:
//	--------------------------------- -------------------------------
//	|Addressing Mode|Mode| Register | |Addressing Mode|Mode|Register|
//	|-------------------------------| |-----------------------------|
//	|      Dn       |000 |N� reg. Dn| |    Abs.W      |111 |  000   |
//	|-------------------------------| |-----------------------------|
//	|      An       | -  |    -     | |    Abs.L      |111 |  001   |
//	|-------------------------------| |-----------------------------|
//	|     (An)      |010 |N� reg. An| |   (d16,PC)    |111 |  010   |
//	|-------------------------------| |-----------------------------|
//	|     (An)+     |011 |N� reg. An| |   (d8,PC,Xi)  |111 |  011   |
//	|-------------------------------| |-----------------------------|
//	|    -(An)      |100 |N� reg. An| |   (bd,PC,Xi)  |111 |  011   |
//	|-------------------------------| |-----------------------------|
//	|    (d16,An)   |101 |N� reg. An| |([bd,PC,Xi],od)|111 |  011   |
//	|-------------------------------| |-----------------------------|
//	|   (d8,An,Xi)  |110 |N� reg. An| |([bd,PC],Xi,od)|111 |  011   |
//	|-------------------------------| |-----------------------------|
//	|   (bd,An,Xi)  |110 |N� reg. An| |    #data      |111 |  100   |
//	|-------------------------------| -------------------------------
//	|([bd,An,Xi]od) |110 |N� reg. An|
//	|-------------------------------|
//	|([bd,An],Xi,od)|110 |N� reg. An|
//	---------------------------------
//
//RESULT
//	X - Not Affected
//	N - Set to the value of the most significant bit.
//	Z - Set if the result is zero.
//	V - Always cleared
//	C - Always cleared
	
	@Override
	public void generate() {
		int base = 0x8000;
		GenInstruction ins;
		
		for (int s = 0; s < 3; s++) {
			if (s == 0b00) {
				ins = new GenInstruction() {
					
					@Override
					public void run(int opcode) {
						ORSourceEAByte(opcode);
					}

				};
			} else if (s == 0b01) {
				ins = new GenInstruction() {
					
					@Override
					public void run(int opcode) {
						ORSourceEAWord(opcode);
					}

				};
			} else {
				ins = new GenInstruction() {
					
					@Override
					public void run(int opcode) {
						ORSourceEALong(opcode);
					}

				};
			}
		
			for (int m = 0; m < 8; m++) {
				if (m == 1) {
					continue;
				}
				
				for (int r = 0; r < 8; r++) {
					if ((m == 7) && r > 0b100) {
						continue;
					}
					
					for (int register = 0; register < 8; register++) {
						int opcode = base + (register << 9) | (s << 6) | ((m << 3) | r);
						cpu.addInstruction(opcode, ins);
					}
				}
			}
		}
		
	}
	
	private void ORSourceEAByte(int opcode) {
		throw new RuntimeException("");
	}

	private void ORSourceEAWord(int opcode) {
		int register = (opcode & 0x7);
		int mode = (opcode >> 3) & 0x7;
		int destRegister = (opcode >> 9) & 0x7;
		
		long data = cpu.readAddressingMode(Size.word, mode, register);
		long res = (cpu.D[destRegister] & 0xFFFF) | data;
		cpu.D[destRegister] = (cpu.D[destRegister] & 0xFFFF_0000L) | res;
		
		calcFlags(res, Size.word.getMsb());
	}
	
	private void ORSourceEALong(int opcode) {
		throw new RuntimeException("");
	}
	
	void calcFlags(long data, long msb) {
		if (data == 0) {
			cpu.setZ();
		} else {
			cpu.clearZ();
		}
		if ((data & msb) > 0) {
			cpu.setN();
		} else {
			cpu.clearN();
		}
		
		cpu.clearV();
		cpu.clearC();
	}
	
}
