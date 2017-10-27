package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class EXT implements GenInstructionHandler {

	final M68000 cpu;
	
	public EXT(M68000 cpu) {
		this.cpu = cpu;
	}
	
//	NAME
//	EXT, EXTB -- Sign extend
//
//SYNOPSIS
//	EXT.W	Dn	Extend byte to word
//	EXT.L	Dn	Extend word to long word
//	EXTB.L	Dn	Extend byte to long word	(68020+)
//
//	Size = (Word, Long)
//
//FUNCTION
//	Extends a byte to a word, or a word to a long word in a data
//	register by copying the sign bit through the upper bits. If the
//	operation is from byte to word, bit 7 is copied to bits 8 through
//	15. If the operation is from word to long word, bit 15 is copied
//	to bits 16 through 31. The EXTB copies bit 7 to bits 8 through 31.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|-----------|---|---|---|-----------|
//	| 0 | 1 | 0 | 0 | 1 | 0 | 0 |  OP-MODE  | 0 | 0 | 0 | REGISTER  |
//	-----------------------------------------------------------------
//
//	"REGISTER" specifies a data register.
//
//OP-MODE
//	010->Extending from 8 bits to 16 bits.
//	011->Extending from 16 bits to 32 bits.
//	111->Extending from 8 bits to 32 bits.
//
//RESULT
//	X - Not affected
//	N - Set if the result is negative. Cleared otherwise.
//	Z - Set if the result is zero. Cleared otherwise.
//	V - Always cleared
//	C - Always cleared
	
	@Override
	public void generate() {
		int base = 0x4800;
		Instruction ins = null;
		
		for (int opMode = 2; opMode < 4; opMode++) {
			if (opMode == 0b010) {
				ins = new Instruction() {
					
					@Override
					public void run(int opcode) {
						EXT8To16Bits(opcode);
					}
					
				};
			} else if (opMode == 0b011) {
				ins = new Instruction() {
					
					@Override
					public void run(int opcode) {
						EXT16To32Bits(opcode);
					}
					
				};
			}
			for (int register = 0; register < 8; register++) {
				int opcode = base | (opMode << 6) | register;
				cpu.addInstruction(opcode, ins);
			}
		}
	}
	
	private void EXT8To16Bits(int opcode) {
		int register = (opcode & 0x7);
		long data = cpu.getDByte(register);
		
		if ((data & 0x80) > 0) {
			data |= 0xFF00;
		}

		cpu.setDWord(register, data);
				
		calcFlags(data, OperationSize.WORD.getMsb());
	}
	
	private void EXT16To32Bits(int opcode) {
		int register = (opcode & 0x7);
		long data = cpu.getDWord(register);
		
		if ((data & 0x8000) > 0) {
			data |= 0xFFFF_0000;
		}

		cpu.setDLong(register, data);
				
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
