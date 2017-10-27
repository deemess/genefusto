package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class AddressRegisterWithIndex implements AddressingMode {

	private M68000 cpu;
	
	public AddressRegisterWithIndex(M68000 cpu) {
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
		int register = o.getRegister();
		long exten = cpu.bus.read(cpu.PC + 2, OperationSize.WORD);
		int displacement = (int) (exten & 0xFF);		// es 8 bits, siempre el ultimo byte ?
		
		cpu.PC += 2;
		
		if ((displacement & 0x80) > 0) { 	// sign extend
			displacement = 0xFFFF_FF00 | displacement;
		}
		int idxRegNumber = (int) ((exten >> 12) & 0x07);
		OperationSize idxSize = ((exten & 0x0800) == 0x0800 ? OperationSize.LONG : OperationSize.WORD);
		boolean idxIsAddressReg = ((exten & 0x8000) == 0x8000);
		
		long data;
		if (idxIsAddressReg) {
			if (idxSize == OperationSize.WORD) {
				data = cpu.getAWord(idxRegNumber);
				if ((data & 0x8000) > 0) {
					data = 0xFFFF_0000 | data;
				}
			} else {
				data = cpu.getALong(idxRegNumber);
			}
		} else {
			if (idxSize == OperationSize.WORD) {
				data = cpu.getDWord(idxRegNumber);
				if ((data & 0x8000) > 0) {
					data = 0xFFFF_0000 | data;
				}
			} else {
				data = cpu.getDLong(idxRegNumber);
			}
		}
		
		long result = cpu.getALong(register) + displacement + data;
		o.setAddress(result);		
	}

}
