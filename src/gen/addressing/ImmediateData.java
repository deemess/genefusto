package gen.addressing;

import gen.M68000;
import gen.OperationSize;
import gen.instruction.Operation;

public class ImmediateData implements AddressingMode {

	private M68000 cpu;
	
	public ImmediateData(M68000 cpu) {
		this.cpu = cpu;
	}
	
	@Override
	public void setByte(Operation o) {
		throw new RuntimeException("NOO");
	}

	@Override
	public void setWord(Operation o) {
		throw new RuntimeException("NOO");
	}

	@Override
	public void setLong(Operation o) {
		throw new RuntimeException("NOO");
	}
	
	@Override
	public long getByte(Operation o) {
		long addr = o.getAddress();
		long data = cpu.bus.read(addr, OperationSize.WORD);	//	lee 2 bytes
		data = data & 0xFF;
		
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
		o.setAddress(cpu.PC + 2);
		
		if (size == OperationSize.BYTE) {		//	aunque sea byte, siempre ocupa 2 bytes y cuenta el de la derecha
			cpu.PC += 2;
			
		} else if (size == OperationSize.WORD) {
			cpu.PC += 2;
			
		} else if (size == OperationSize.LONG) {	// long
			cpu.PC += 4;
			
		}
	}

}
