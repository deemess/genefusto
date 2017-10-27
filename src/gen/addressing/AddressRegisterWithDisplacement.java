package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class AddressRegisterWithDisplacement implements AddressingMode {

	private M68000 cpu;
	
	public AddressRegisterWithDisplacement(M68000 cpu) {
		this.cpu = cpu;
	}
	
	@Override
	public void setByte(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();

		cpu.bus.write(addr, data & 0xFF, OperationSize.BYTE);
	}

	@Override
	public void setWord(Operation o) {
		long addr = o.getAddress();
		long data = o.getData();

		cpu.bus.write(addr, data & 0xFFFF, OperationSize.WORD);
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
		long base = cpu.getALong(o.getRegister());
		long displac = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		
		cpu.PC += 2;
		
		long displacement = (long) displac;
		if ((displacement & 0x8000) > 0) {
			displacement |= 0xFFFF_0000L;	// sign extend 32 bits
		}
		long addr = (int) (base + displacement);	// TODO verificar esto, al pasarlo a int hace el wrap bien parece
		
		o.setAddress(addr);	
	}

}
