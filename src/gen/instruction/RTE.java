package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.Size;

public class RTE implements GenInstructionHandler {

	final M68000 cpu;
	
	public RTE(M68000 cpu) {
		this.cpu = cpu;
	}
	
//	NAME
//	RTE -- Return from exception (PRIVILEGED)
//
//SYNOPSIS
//	RTE
//
//FUNCTION
//	SR and PC are restored by SP. All SR bits are affected.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
//	| 0 | 1 | 0 | 0 | 1 | 1 | 1 | 0 | 0 | 1 | 1 | 1 | 0 | 0 | 1 | 1 |
//	-----------------------------------------------------------------
//
//RESULT
//	SR is set following to the restored word taken from SP.
	
	@Override
	public void generate() {
		int base = 0x4E73;
		Instruction ins;
		
		ins = new Instruction() {
			
			@Override
			public void run(int opcode) {
				RTEpc(opcode);
			}
		};
		
		cpu.addInstruction(base, ins);
	}
	
	private void RTEpc(int opcode) {
		long SR = cpu.bus.read(cpu.SSP, Size.WORD);
		cpu.SSP += 2;
		
		cpu.SR = (int) SR;
		
		long newPC;
		newPC = cpu.bus.read(cpu.SSP, Size.LONG);
		cpu.SSP += 4;
		
		cpu.PC = newPC - 2;
		
		if ((cpu.SR & 0x2000) == 0x2000) {
			cpu.setALong(7, cpu.SSP);
		} else {
			cpu.setALong(7, cpu.USP);
		}
		
	}

}
