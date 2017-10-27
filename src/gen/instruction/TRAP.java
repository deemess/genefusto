package gen.instruction;

import gen.M68000;
import gen.Instruction;
import gen.OperationSize;

public class TRAP implements GenInstructionHandler {

	final M68000 cpu;
	
	public TRAP(M68000 cpu) {
		this.cpu = cpu;
	}

//	NAME
//	TRAP -- Initiate processor trap
//
//SYNOPSIS
//	TRAP	#<number>
//
//FUNCTION
//	Processor starts an exception process. TRAP number is pointed
//	out by 4 bits into the instruction. 16 vectors are free to
//	be used for TRAP (vectors from 32 to 47).
//	So the <number> can go from 0 to 15.
//	PC and SR are stored to SSP, and Vector is written to PC.
//
//FORMAT
//	-----------------------------------------------------------------
//	|15 |14 |13 |12 |11 |10 | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
//	|---|---|---|---|---|---|---|---|---|---|---|---|---------------|
//	| 0 | 1 | 0 | 0 | 1 | 1 | 1 | 0 | 0 | 1 | 0 | 0 |  N� of TRAP   |
//	-----------------------------------------------------------------
	
	@Override
	public void generate() {
		int base = 0x4E40;
		Instruction ins;
		
		ins = new Instruction() {
			@Override
			public void run(int opcode) {
				TRAP_OP(opcode);
			}
			
		};
		
		for (int trap = 0; trap < 8; trap++) {
			int opcode = base | trap;
			cpu.addInstruction(opcode, ins);
		}
		
	}
	
	private void TRAP_OP(int opcode) {
int trap = opcode & 0x7;
		
		long oldPC = cpu.PC + 2;
		int oldSR = cpu.SR;
		
		if ((cpu.SR & 0x2000) == 0) {
			cpu.SR = cpu.SR | 0x2000;
		}
		
		cpu.SSP--;
		cpu.bus.write(cpu.SSP, oldPC & 0xFF, OperationSize.BYTE);
		cpu.SSP--;
		cpu.bus.write(cpu.SSP, (oldPC >> 8) & 0xFF, OperationSize.BYTE);
		cpu.SSP--;
		cpu.bus.write(cpu.SSP, (oldPC >> 16) & 0xFF, OperationSize.BYTE);
		cpu.SSP--;
		cpu.bus.write(cpu.SSP, (oldPC >> 24), OperationSize.BYTE);
		
		cpu.SSP--;
		cpu.bus.write(cpu.SSP, (oldSR & 0xFF), OperationSize.BYTE);
		cpu.SSP--;
		cpu.bus.write(cpu.SSP, (oldSR >> 8), OperationSize.BYTE);
		
		cpu.setALong(7, cpu.SSP);
		
		long vector = 0x80 + (trap * 4);
		long newPC = cpu.bus.readInterruptVector(vector);
		
		cpu.PC = newPC - 2;
	}

}
