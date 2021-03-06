package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class ADDQ implements GenInstructionHandler {

	final M68000 cpu;
	
	public ADDQ(M68000 cpu) {
		this.cpu = cpu;
	}

//	NAME
//	ADDQ -- Add 3-bit immediate quick
//
//SYNOPSIS
//	ADDQ	#<data>,<ea>
//
//	Size = (Byte, Word, Long)
//
//FUNCTION
//	Adds the immediate value of 1 to 8 to the operand at the
//	destination location. The size of the operation may be specified as
//	byte, word, or long. When adding to address registers, the condition
//	codes are not altered, and the entire destination address register is
//	used regardless of the operation size.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|-----------|---|-------|-----------|-----------|
//	| 0 | 1 | 0 | 1 |    DATA   | 0 | SIZE  |    MODE   |  REGISTER |
//	----------------------------------------=========================
//                                                          <ea>
//
//DATA
//	000        ->represent value 8
//	001 to 111 ->immediate data from 1 to 7
//
//SIZE
//	00->one Byte operation
//	01->one Word operation
//	10->one Long operation
//
//REGISTER
//	<ea> is always destination, addressing modes are the followings:
//
//	--------------------------------- -------------------------------
//	|Addressing Mode|Mode| Register | |Addressing Mode|Mode|Register|
//	|-------------------------------| |-----------------------------|
//	|      Dn       |000 |N� reg. Dn| |    Abs.W      |111 |  000   |
//	|-------------------------------| |-----------------------------|
//	|      An *     |001 |N� reg. An| |    Abs.L      |111 |  001   |
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
//	 * Word or Long only.
//
//RESULT
//	X - Set the same as the carry bit.
//	N - Set if the result is negative. Cleared otherwise.
//	Z - Set if the result is zero. Cleared otherwise.
//	V - Set if an overflow is generated. Cleared otherwise.
//	C - Set if a carry is generated. Cleared otherwise.
	
	@Override
	public void generate() {
		int base = 0x5000;
		Instruction ins = null;
		
		for (int s = 0; s < 3; s++) {
			if (s == 0b00) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						ADDQByte(opcode);
					}
				};
			} else if (s == 0b01) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						ADDQWord(opcode);
					}
				};
			} else if (s == 0b10) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						ADDQLong(opcode);
					}
				};
			}
			for (int m = 0; m < 8; m++) {
				if (m == 1 && s == 0b00) {	// byte no tiene este modo
					continue;
				}
				for (int r = 0; r < 8; r++) {
					if (m == 0b111 & r > 0b001) {
						continue;
					}
					for (int d = 0; d < 8; d++) {
						int opcode = base + ((d << 9) | (s << 6) | (m << 3) | r);
						cpu.addInstruction(opcode, ins);
					}
				}
			}
		}
		
	}
	
	private void ADDQByte(int opcode) {
		int dataToAdd = (opcode >> 9) & 0x7;
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		if (dataToAdd == 0) {
			dataToAdd = 8;
		}
		
		Operation o = cpu.resolveAddressingMode(OperationSize.BYTE, mode, register);
		long data = o.getAddressingMode().getByte(o);
		
		long tot = (data + dataToAdd);
		long total = tot & 0xFFFF_FFFFL;
		
		cpu.writeKnownAddressingMode(o, total, OperationSize.BYTE);
		
		calcFlags(tot, data, dataToAdd, OperationSize.BYTE.getMsb(), 0xFF);
	}
	
	private void ADDQWord(int opcode) {
		int dataToAdd = (opcode >> 9) & 0x7;
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		if (dataToAdd == 0) {
			dataToAdd = 8;
		}
		
		//	direct address lo maneja distinto
		if (mode == 1) {
			long data = cpu.getALong(register);
			long tot = (data + dataToAdd);
			cpu.setALong(register, tot);
			
		} else {
			Operation o = cpu.resolveAddressingMode(OperationSize.WORD, mode, register);
			long data = o.getAddressingMode().getWord(o);
			
			long tot = (data + dataToAdd);
			long total = tot & 0xFFFF_FFFFL;
			
			cpu.writeKnownAddressingMode(o, total, OperationSize.WORD);
			
			calcFlags(tot, data, dataToAdd, OperationSize.WORD.getMsb(), 0xFFFF);
		}
		
	}
	
	private void ADDQLong(int opcode) {
		int dataToAdd = (opcode >> 9) & 0x7;
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		if (dataToAdd == 0) {
			dataToAdd = 8;
		}
		
		Operation o = cpu.resolveAddressingMode(OperationSize.LONG, mode, register);
		long data = o.getAddressingMode().getLong(o);
		
		long tot = (data + dataToAdd);
		long total = tot & 0xFFFF_FFFFL;

		cpu.writeKnownAddressingMode(o, total, OperationSize.LONG);
		
		// if destination is An no cambian los flags
		if (mode != 1) {
			calcFlags(tot, data, dataToAdd, OperationSize.LONG.getMsb(), 0xFFFF_FFFFL);
		}
	}
	
	void calcFlags(long tot, long data, long toAdd, long msb, long maxSize) {
		if ((tot & maxSize) == 0) {
			cpu.setZ();
		} else {
			cpu.clearZ();
		}
		if (((tot & maxSize) & msb) > 0) {
			cpu.setN();
		} else {
			cpu.clearN();
		}
		
		boolean Dm = (data & msb) > 0;
		boolean Sm = (toAdd & msb) > 0;
		boolean Rm = (tot & msb) > 0;
		if((Sm && Dm && !Rm) || (!Sm && !Dm && Rm)) {
			cpu.setV();
		} else {
			cpu.clearV();
		}
		
		if (tot > maxSize) {
			cpu.setC();
			cpu.setX();
		} else {
			cpu.clearC();
			cpu.clearX();
		}
	}
	
}
