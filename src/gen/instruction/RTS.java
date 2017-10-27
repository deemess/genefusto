package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.Size;

public class RTS implements GenInstructionHandler {

	final M68000 cpu;
	
	public RTS(M68000 cpu) {
		this.cpu = cpu;
	}
	
//	NAME
//	RTS -- Return from subroutine
//
//SYNOPSIS
//	RTS
//
//FUNCTION
//	PC is restored by SP.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
//	| 0 | 1 | 0 | 0 | 1 | 1 | 1 | 0 | 0 | 1 | 1 | 1 | 0 | 1 | 0 | 1 |
//	-----------------------------------------------------------------
//
//RESULT
//	None.
	
	@Override
	public void generate() {
		int base = 0x4E75;
		Instruction ins;
		
		ins = new Instruction() {
			
			@Override
			public void run(int opcode) {
				RSTpc(opcode);
			}
		};
		
		cpu.addInstruction(base, ins);
	}
	
	private void RSTpc(int opcode) {
		long newPC;
		
		if ((cpu.SR & 0x2000) == 0x2000) {
			newPC = cpu.bus.read(cpu.SSP, Size.LONG);
			cpu.SSP += 4;
			
			cpu.setALong(7, cpu.SSP);
			
			cpu.PC = newPC - 2;
		} else {
			newPC = cpu.bus.read(cpu.USP, Size.LONG);
			cpu.USP += 4;
			
			cpu.setALong(7, cpu.USP);
			
			cpu.PC = newPC - 2;
		}
		
	}

}
