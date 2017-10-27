package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class AddressRegisterIndirect implements AddressingMode {

	private M68000 cpu;
	
	public AddressRegisterIndirect(M68000 cpu) {
		this.cpu = cpu;
	}
	
	@Override
	public void setByte(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();

		cpu.bus.write(addr, (data & 0xFF), OperationSize.BYTE);
	}

	@Override
	public void setWord(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();

		cpu.bus.write(addr, (data & 0xFFFF), OperationSize.WORD);
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
		long addr = cpu.getALong(o.getRegister());
		
		o.setAddress(addr);
	}

}
