package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class NBCD implements GenInstructionHandler {

	final M68000 cpu;
	
	public NBCD(M68000 cpu) {
		this.cpu = cpu;
	}

//	NAME
//	NBCD -- Negate binary coded decimal with extend
//
//SYNOPSIS
//	NBCD	<ea>
//
//	Size = (Byte)
//
//FUNCTION
//	The specified BCD number and the extend bit are subtracted
//	from zero. Therefore, if the extend bit is set a nines
//	complement is performed, else a tens complement is performed.
//	The result is placed back in the specified <ea>.
//
//	It can be useful to set the zero flag before performing
//	this operation so that multi precision operations can
//	be correctly tested for zero.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|---|---|-----------|-----------|
//	| 0 | 1 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |    MODE   | REGISTER  |
//	----------------------------------------=========================
//	                                                   <ea>
//
//RESULT
//	X - Set the same as the carry bit.
//	N - Undefined.
//	Z - Cleared it the result is non-zero, unchanged otherwise.
//	V - Undefined.
//	C - Set if a borrow was generated, cleared otherwise.

	@Override
	public void generate() {
		int base = 0x4800;
		Instruction ins = null;
		
		ins = new Instruction() {
			@Override
			public void run(int opcode) {
				NBCDDataByte(opcode);
			}
			
		};
				
		for (int m = 0; m < 8; m++) {
			for (int r = 0; r < 8; r++) {
				int opcode = base | (m << 3) | r;
				cpu.addInstruction(opcode, ins);
			}
		}
	}
	
	private void NBCDDataByte(int opcode) {
		int mode = (opcode >> 3) & 0x07;
		int register = (opcode & 0x07);
		
		Operation o = cpu.resolveAddressingMode(OperationSize.BYTE, mode, register);
		long data = o.getData();

		int x = (cpu.isX() ? 1 : 0);
		int c;

		int lo = (int) (10 - (data & 0x0f) - x);
		if (lo < 10) {
			c = 1;
		} else {
			lo = 0;
			c = 0;
		}

		int hi = (int) (10 - ((data >> 4) & 0x0f) - c);
		if (hi < 10) {
			c = 1;
		} else {
			c = 0;
			hi = 0;
		}

		int result = (hi << 4) + lo;

		if (c != 0) {
			cpu.setC();
			cpu.setX();
		} else {
			cpu.setC();
			cpu.setX();
		}

		if (result != 0) {
			cpu.clearZ();
		}

		o.setData(result);
		
		cpu.writeKnownAddressingMode(o, result, OperationSize.BYTE);
	}
	
}
