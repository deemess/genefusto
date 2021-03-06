package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class PCWithDisplacement implements AddressingMode {

	private M68000 cpu;
	
	public PCWithDisplacement(M68000 cpu) {
		this.cpu = cpu;
	}
	
	@Override
	public void setByte(Operation o) {
		throw new RuntimeException();
	}

	@Override
	public void setWord(Operation o) {
		throw new RuntimeException();
	}

	@Override
	public void setLong(Operation o) {
		throw new RuntimeException();
	}

	@Override
	public long getByte(Operation o) {
		long address = o.getAddress();
		long data = cpu.bus.read(address, OperationSize.BYTE);
		
		return data;
	}

	@Override
	public long getWord(Operation o) {
		long address = o.getAddress();
		long data = cpu.bus.read(address, OperationSize.WORD);
		
		return data;
	}

	@Override
	public long getLong(Operation o) {
		long address = o.getAddress();
		long data = cpu.bus.read(address, OperationSize.LONG);
		
		return data;
	}

	@Override
	public void calculateAddress(Operation o, OperationSize size) {
		long displacement = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		long addr;
		if ((displacement & 0x8000) > 0) {
			displacement = -displacement;
			displacement &= 0xFFFF;
			addr = cpu.PC + 2 - displacement;
		} else {
			addr = cpu.PC + 2 + displacement;
		}
		o.setAddress(addr);
		
		cpu.PC += 2;
	}

}
