package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class AddressRegisterIndirectPostIncrement implements AddressingMode {

	private M68000 cpu;
	
	public AddressRegisterIndirectPostIncrement(M68000 cpu) {
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
		int register = o.getRegister();
		long addr = cpu.getALong(register);
		o.setAddress(addr);
		
		if (size == OperationSize.BYTE) {	//	byte
			if (register == 7) {	// stack pointer siempre alineado de a 2
				addr += 2;
			} else {
				addr += 1;
			}
			cpu.setALong(register, addr);
			
		} else if (size == OperationSize.WORD) {	//	word
			addr += 2;
			cpu.setALong(register, addr);
			
		} else if (size == OperationSize.LONG) {	//	long
			addr += 4;
			cpu.setALong(register, addr);
			
		}
	}

}
