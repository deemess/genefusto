package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class MOVE_TO_SR implements GenInstructionHandler {

	final M68000 cpu;
	
	public MOVE_TO_SR(M68000 cpu) {
		this.cpu = cpu;
	}

//	NAME
//	MOVE to SR -- Move to status register (PRIVILEGED)
//
//SYNOPSIS
//	MOVE	<ea>,SR
//
//	Size = (Word)
//
//FUNCTION
//	The content of the source operand is moved to the
//	status register. The source operand size is a word
//	and all bits of the status register are affected.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|---|---|-----------|-----------|
//	| 0 | 1 | 0 | 0 | 0 | 1 | 1 | 0 | 1 | 1 |    MODE   | REGISTER  |
//	----------------------------------------=========================
//	                                                  <ea>
//
//REGISTER
//	<ea> specifies source operand, addressing modes allowed are:
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
//
//	X - Set the same as bit 4 of the source operand.
//	N - Set the same as bit 3 of the source operand.
//	Z - Set the same as bit 2 of the source operand.
//	V - Set the same as bit 1 of the source operand.
//	C - Set the same as bit 0 of the source operand.
	
	@Override
	public void generate() {
		int base = 0x46C0;
		Instruction ins = null;
		
		ins = new Instruction() {
			
			@Override
			public void run(int opcode) {
				MOVEToSR(opcode);
			}
		};

		for (int m = 0; m < 8; m++) {
			if (m == 1) {
				continue;
			}
			for (int r = 0; r < 8; r++) {
				if (m == 0b111 && r > 0b100) {
					continue;
				}
				int opcode = base + (m << 3) | (r);
				cpu.addInstruction(opcode, ins);
			}
		}
	}
	
	private void MOVEToSR(int opcode) {
		int mode = (opcode >> 3) & 0x7;
		int register = opcode & 0x7;

		int oldSR = cpu.SR;
		
		Operation o = cpu.resolveAddressingMode(OperationSize.WORD, mode, register);
		long data = o.getAddressingMode().getWord(o);
		cpu.SR = (int) data;
		
		if (((oldSR & 0x2000) ^ (data & 0x2000)) != 0) {	//	si cambio el supervisor bit
			if ((data & 0x2000) == 0x2000) {
				cpu.setALong(7, cpu.SSP);
			} else {
				cpu.setALong(7, cpu.USP);
			}	
		}
	}
	
}
