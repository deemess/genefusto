package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class NOT implements GenInstructionHandler {

	final M68000 cpu;
	
	public NOT(M68000 cpu) {
		this.cpu = cpu;
	}
	
//	NAME
//	NOT -- Logical complement
//
//SYNOPSIS
//	NOT	<ea>
//
//	Size = (Byte, Word, Long)
//
//FUNCTION
//	All bits of the specified operand are inverted and placed
//	back in the operand.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|-------|-----------|-----------|
//	| 0 | 1 | 0 | 0 | 0 | 1 | 1 | 0 | SIZE  |    MODE   | REGISTER  |
//	----------------------------------------=========================
//	                                                   <ea>
//
//SIZE
//	00->Byte.
//	01->Word.
//	10->Long.
//
//REGISTER
//	<ea> specifies destination operand, addressing modes allowed are:
//	--------------------------------- -------------------------------
//	|Addressing Mode|Mode| Register | |Addressing Mode|Mode|Register|
//	|-------------------------------| |-----------------------------|
//	|      Dn       |000 |N� reg. Dn| |    Abs.W      |111 |  000   |
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
//RESULT
//	X - Not affected.
//	N - Set if the result is negative, otherwise cleared.
//	Z - Set if the result is zero, otherwise cleared.
//	V - Always cleared.
//	C - Always cleared.
	
	@Override
	public void generate() {
		int base = 0x4600;
		Instruction ins = null;
		
		for (int s = 0; s < 3; s++) {
			if (s == 0b00) {
				ins = new Instruction() {
					
					@Override
					public void run(int opcode) {
						NOTByte(opcode);
					}
					
				};
			} else if (s == 0b01) {
				ins = new Instruction() {
					
					@Override
					public void run(int opcode) {
						NOTWord(opcode);
					}
					
				};
			} else if (s == 0b10) {
				ins = new Instruction() {
					
					@Override
					public void run(int opcode) {
						NOTLong(opcode);
					}
					
				};
			}
			
			for (int m = 0; m < 8; m++) {
				if (m == 1) {
					continue;
				}
				for (int r = 0; r < 8; r++) {
					if (m == 0b111 && r > 0b001) {
						continue;
					}
					int opcode = base | (s << 6) | (m << 3) | r;
					cpu.addInstruction(opcode, ins);
				}
			}	
		}
	}
	
	private void NOTByte(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.BYTE, mode, register);
		long data = o.getAddressingMode().getByte(o);
		data = (~data) & 0xFF;

		cpu.writeKnownAddressingMode(o, data, OperationSize.BYTE);
				
		calcFlags(data, OperationSize.BYTE.getMsb());
	}
	
	private void NOTWord(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.WORD, mode, register);
		long data = o.getAddressingMode().getWord(o);
		data = (~data) & 0xFFFF;

		cpu.writeKnownAddressingMode(o, data, OperationSize.WORD);
				
		calcFlags(data, OperationSize.WORD.getMsb());
	}

	private void NOTLong(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.LONG, mode, register);
		long data = o.getAddressingMode().getLong(o);
		data = (~data) & 0xFFFF_FFFFL;

		cpu.writeKnownAddressingMode(o, data, OperationSize.LONG);
				
		calcFlags(data, OperationSize.LONG.getMsb());
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
