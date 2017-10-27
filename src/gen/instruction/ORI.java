package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class ORI implements GenInstructionHandler {

	final M68000 cpu;
	
	public ORI(M68000 cpu) {
		this.cpu = cpu;
	}
	
	//	ORI		Logical OR Immediate
//	Performs an inclusive OR operation on the destination operand
//	with the source operand.
//Size = (Byte, Word, Long)
//													 <ea>
//----------------------------------------=========================
//|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//|---|---|---|---|---|---|---|---|-------|-----------|-----------|
//| 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | SIZE  |    MODE   | REGISTER  |
//|-------------------------------|-------------------------------|
//|    16 BITS IMMEDIATE DATA     |     8 BITS IMMEDIATE DATA     |
//|---------------------------------------------------------------|
//|                    32 BITS IMMEDIATE DATA                     |
//-----------------------------------------------------------------
//
//	REGISTER
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
	
//SIZE
//00->Byte.
//01->Word.
//10->Long.

//X - Not Affected
//N - Set to the value of the most significant bit.
//Z - Set if the result is zero.
//V - Always cleared
//C - Always cleared
	@Override
	public void generate() {
		int base = 0;
		Instruction ins;
		
		for (int s = 0; s < 3; s++) {
			if (s == 0b00) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						ORIByte(opcode);
					}

				};
			} else if (s == 0b01) {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						ORIWord(opcode);
					}

				};
			} else {
				ins = new Instruction() {
					@Override
					public void run(int opcode) {
						ORILong(opcode);
					}

				};
			}
		
			for (int m = 0; m < 8; m++) {
				if (m == 1) {
					continue;
				}
				
				for (int r = 0; r < 8; r++) {
					if ((m == 7) && r > 0b001) {
						continue;
					}
					
					int opcode = base | (s << 6) | (m << 3) | r;
					cpu.addInstruction(opcode, ins);
				}
			}
		}
		
	}
	
	private void ORIByte(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		long toOr = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		toOr = toOr & 0xFF;	//	ocupa 2 bytes, pero solo se toma el ultimo
		
	 	cpu.PC += 2;
	 	 
		Operation o = cpu.resolveAddressingMode(OperationSize.BYTE, mode, register);
		long data = o.getAddressingMode().getByte(o);
		
		long res = data | toOr;
		cpu.writeKnownAddressingMode(o, res, OperationSize.BYTE);
		 	 
		calcFlags(res, OperationSize.BYTE.getMsb());
	}

	private void ORIWord(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);
		
		long toOr = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		
	 	cpu.PC += 2;
	 	 
		Operation o = cpu.resolveAddressingMode(OperationSize.WORD, mode, register);
		long data = o.getAddressingMode().getWord(o);
		
		long res = data | toOr;
		cpu.writeKnownAddressingMode(o, res, OperationSize.WORD);
		 	 
		calcFlags(res, OperationSize.WORD.getMsb());
	}
	
	private void ORILong(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = (opcode & 0x7);

		long toOr = cpu.bus.read(cpu.PC + 2, OperationSize.LONG);
		
		cpu.PC += 4;
		
		Operation o = cpu.resolveAddressingMode(OperationSize.LONG, mode, register);
		long data = o.getAddressingMode().getLong(o);
		
		long res = data | toOr;
		cpu.writeKnownAddressingMode(o, res, OperationSize.LONG);
		 	 
		calcFlags(res, OperationSize.LONG.getMsb());
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
