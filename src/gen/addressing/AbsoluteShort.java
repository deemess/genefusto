package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class AbsoluteShort implements AddressingMode {

	private M68000 cpu;
	
	public AbsoluteShort(M68000 cpu) {
		this.cpu = cpu;
	}
	
	@Override
	public void setByte(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();
		cpu.bus.write(addr, data, OperationSize.BYTE);
	}

	@Override
	public void setWord(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();
		cpu.bus.write(addr, data, OperationSize.WORD);
	}

	@Override
	public void setLong(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();
		cpu.bus.write(addr, data, OperationSize.LONG);
	}
	
	@Override
	public long getByte(Operation o) {
		long addr = o.getAddress();
		long data = cpu.bus.read(addr, OperationSize.BYTE);
		
		return data;
	}

	@Override
	public long getWord(Operation o) {
		long addr = o.getAddress();
		long data = cpu.bus.read(addr, OperationSize.WORD);
		
		return data;
	}

	@Override
	public long getLong(Operation o) {
		long addr = o.getAddress();
		long data = cpu.bus.read(addr, OperationSize.LONG);
		
		return data;
	}
	
	@Override
	public void calculateAddress(Operation o, OperationSize size) {
		long addr = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		if ((addr & 0x8000) > 0) {
			addr |= 0xFFFF_0000L;
		}
		o.setAddress(addr);
		
		cpu.PC += 2;
	}

}
