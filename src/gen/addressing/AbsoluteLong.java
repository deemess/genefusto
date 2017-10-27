package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class AbsoluteLong implements AddressingMode {

	private M68000 cpu;
	
	public AbsoluteLong(M68000 cpu) {
		this.cpu = cpu;
	}
	
	@Override
	public void setByte(Operation o) {
		long address = o.getAddress();
		long data = o.getData();
		
		cpu.bus.write(address, data, OperationSize.BYTE);
	}

	@Override
	public void setWord(Operation o) {
		long address = o.getAddress();
		long data = o.getData();
		
		cpu.bus.write(address, data, OperationSize.WORD);
	}

	@Override
	public void setLong(Operation o) {
		long address = o.getAddress();
		long data = o.getData();
		
		cpu.bus.write(address, data, OperationSize.LONG);
	}
	
	@Override
	public long getByte(Operation o) {
		long addr = o.getAddress();
		long data = cpu.bus.read(addr, OperationSize.BYTE) & 0xFF;
		
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
		long addr = cpu.bus.read(cpu.PC + 2, OperationSize.LONG);
		o.setAddress(addr);
		
		cpu.PC += 4;
	}

}
