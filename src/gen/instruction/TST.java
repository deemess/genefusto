package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class TST implements GenInstructionHandler {

	final M68000 cpu;
	
	public TST(M68000 cpu) {
		this.cpu = cpu;
	}
	
//	TeST operand for zero
//
//	NAME
//		TST -- Test operand for zero
//
//	SYNOPSIS
//		TST	<ea>
//
//		Size = (Byte, Word, Long)
//
//	FUNCTION
//		Operand is compared with zero. Flags are set according to the result.
//
//	FORMAT
//		-----------------------------------------------------------------
//		|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//		|---|---|---|---|---|---|---|---|-------|-----------|-----------|
//		| 0 | 1 | 0 | 0 | 1 | 0 | 1 | 0 | SIZE  |   MODE    |  REGISTER |
//		----------------------------------------=========================
//	                                                          <ea>
//
//	SIZE
//		00->one Byte operation
//		01->one Word operation
//		10->one Long operation
//
//	REGISTER
//		<ea> is destination, if size is 16 or 32 bits then all addressing
//		modes are allowed. If size is 8 bits, allowed addressing modes are:
//		--------------------------------- -------------------------------
//		|Addressing Mode|Mode| Register | |Addressing Mode|Mode|Register|
//		|-------------------------------| |-----------------------------|
//		|      Dn       |000 |N� reg. Dn| |    Abs.W      |111 |  000   |
//		|-------------------------------| |-----------------------------|
//		|      An       | -  |    -     | |    Abs.L      |111 |  001   |
//		|-------------------------------| |-----------------------------|
//		|     (An)      |010 |N� reg. An| |   (d16,PC)    |111 |  010   |
//		|-------------------------------| |-----------------------------|
//		|     (An)+     |011 |N� reg. An| |   (d8,PC,Xi)  |111 |  011   |
//		|-------------------------------| |-----------------------------|
//		|    -(An)      |100 |N� reg. An| |   (bd,PC,Xi)  |111 |  011   |
//		|-------------------------------| |-----------------------------|
//		|    (d16,An)   |101 |N� reg. An| |([bd,PC,Xi],od)|111 |  011   |
//		|-------------------------------| |-----------------------------|
//		|   (d8,An,Xi)  |110 |N� reg. An| |([bd,PC],Xi,od)|111 |  011   |
//		|-------------------------------| |-----------------------------|
//		|   (bd,An,Xi)  |110 |N� reg. An| |    #data      | -  |   -    |
//		|-------------------------------| -------------------------------
//		|([bd,An,Xi]od) |110 |N� reg. An|
//		|-------------------------------|
//		|([bd,An],Xi,od)|110 |N� reg. An|
//		---------------------------------
//
//	RESULT
//		X - Not affected.
//		N - Set if the result is negative. Cleared otherwise.
//		Z - Set if the result is zero. Cleared otherwise.
//		V - Always cleared.
//		C - Always cleared.

	@Override
	public void generate() {
		Instruction ins = null;
		int base = 0x4A00;
		
		for (int s = 0; s < 3; s++) {
			if (s == 0b00) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						TSTByte(opcode);
					}
				};
				
			} else if (s == 0b01) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						TSTWord(opcode);
					}
				};
				
			} else if (s == 0b10) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						TSTLong(opcode);
					}
				};
			}
			
			for (int m = 0; m < 8; m++) {
				for (int r = 0; r < 8; r++) {
					if (s == 0b00 && m == 1) {		//	byte no tiene este modo
						continue;
					}
					if (m == 0b111 && r > 0b011) {
						continue;
					}

					int opcode = base | (s << 6) | (m << 3) | r;
					cpu.addInstruction(opcode, ins);
				}
			}
		}
		
	}
	
	private void TSTByte(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.BYTE, mode, register);
		long data = o.getAddressingMode().getByte(o);
		
		calcFlags(data, OperationSize.BYTE.getMsb());
	}
	
	private void TSTWord(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.WORD, mode, register);
		long data = o.getAddressingMode().getWord(o);
		
		calcFlags(data, OperationSize.WORD.getMsb());
	}
	
	private void TSTLong(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.LONG, mode, register);
		long data = o.getAddressingMode().getLong(o);
		
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
