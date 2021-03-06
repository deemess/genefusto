package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class CMPI implements GenInstructionHandler {

	final M68000 cpu;
	
	public CMPI(M68000 cpu) {
		this.cpu = cpu;
	}

//	NAME
//	CMPI -- Compare immediate
//
//SYNOPSIS
//	CMP	#<data>,<ea>
//
//	Size = (Byte, Word, Long)
//
//FUNCTION
//	Subtracts the source operand from the destination operand and sets
//	the condition codes according to the result. The destination is
//	NOT changed. The size of the immediate data matches the operation
//	size.
//
//FORMAT
//	                                                  <ea>
//	----------------------------------------=========================
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|-------|-----------|-----------|
//	| 0 | 0 | 0 | 0 | 1 | 1 | 0 | 0 | SIZE  |    MODE   | REGISTER  |
//	|-------------------------------|-------------------------------|
//	|         16 BITS DATA          |         8 BITS DATA           |
//	|---------------------------------------------------------------|
//	|                          32 BITS DATA                         |
//	-----------------------------------------------------------------
//
//SIZE
//	00->one Byte operation
//	01->one Word operation
//	10->one Long operation
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
//	|   (bd,An,Xi)  |110 |N� reg. An| |    #data      | -  |   -    |
//	|-------------------------------| -------------------------------
//	|([bd,An,Xi]od) |110 |N� reg. An|
//	|-------------------------------|
//	|([bd,An],Xi,od)|110 |N� reg. An|
//	---------------------------------
//
//RESULT
//	X - Not affected
//	N - Set if the result is negative. Cleared otherwise.
//	Z - Set if the result is zero. Cleared otherwise.
//	V - Set if an overflow occours. Cleared otherwise.
//	C - Set if a borrow occours. Cleared otherwise.
	
	@Override
	public void generate() {
		int base = 0x0C00;
		Instruction ins = null;
		for (int s = 0; s < 3; s++) {
			if (s == 0b00) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						CMPIByte(opcode);
					}
				};
			} else if (s == 0b01) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						CMPIWord(opcode);
					}
				};
			} else if (s == 0b10) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						CMPILong(opcode);
					}
				};
			}
			for (int m = 0; m < 8; m++) {
				if (m == 1) {
					continue;
				}
				for (int r = 0; r < 8; r++) {
					if ((m == 7) && r > 0b011) {
						continue;
					}
					int opcode = base | (s << 6) | (m << 3) | r;
					cpu.addInstruction(opcode, ins);
				}
			}
		}
	}

	private void CMPIByte(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);

		long data = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		data = data & 0xFF;	//	ultimo byte
		
		cpu.PC += 2;
		
		Operation o = cpu.resolveAddressingMode(OperationSize.BYTE, mode, register);
		long toSub = o.getAddressingMode().getByte(o);
		
		long res = toSub - data;
		
		calcFlags(data, toSub, res, OperationSize.BYTE.getMsb(), OperationSize.BYTE.getMax());
	}
	
	private void CMPIWord(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);

		long data = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		
		cpu.PC += 2;
		
		Operation o = cpu.resolveAddressingMode(OperationSize.WORD, mode, register);
		long toSub = o.getAddressingMode().getWord(o);

		long res = toSub - data;
		
		calcFlags(data, toSub, res, OperationSize.WORD.getMsb(), OperationSize.WORD.getMax());
	}
	
	private void CMPILong(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);

		long data = cpu.bus.read(cpu.PC + 2, OperationSize.LONG);
		
		cpu.PC += 4;
		
		Operation o = cpu.resolveAddressingMode(OperationSize.LONG, mode, register);
		long toSub = o.getAddressingMode().getLong(o);
		
		long res = toSub - data;
		
		calcFlags(data, toSub, res, OperationSize.LONG.getMsb(), OperationSize.LONG.getMax());
	}
	
	void calcFlags(long data, long toSub, long res, long msb, long maxSize) {
		if ((res & maxSize) == 0) {
			cpu.setZ();
		} else {
			cpu.clearZ();
		}
		if (((res & maxSize) & msb) > 0) {
			cpu.setN();
		} else {
			cpu.clearN();
		}
		
		boolean Sm = (data & msb) != 0;
		boolean Dm = (toSub & msb) != 0;
		boolean Rm = (res & msb) != 0;
		if ((!Sm && Dm && !Rm) || (Sm && !Dm && Rm)) {
			cpu.setV();
		} else {
			cpu.clearV();
		}

		if ((Sm && !Dm) || (Rm && !Dm) || (Sm && Rm)) {
			cpu.setC();
		} else {
			cpu.clearC();
		}
	}
	
}
