package gen;

public class Memory {

	int[] rom;
	int[] ram = new int[0x10000];
    int[] sram = new int[0x10000];

	int readROM(int address, OperationSize opsize) {
		int data = 0;

		switch (opsize) {
            case BYTE:
                if(address < rom.length) {
                    data = rom[address];
                }
                break;
            case WORD:
                if(address+1 < rom.length) {
                    data  = rom[address] << 8;
                    data |= rom[address + 1];
                }
                break;
            case LONG:
                if(address+3 < rom.length) {
                    data = rom[address] << 24;
                    data |= rom[address + 1] << 16;
                    data |= rom[address + 2] << 8;
                    data |= rom[address + 3];
                }
                break;
        }

		return data;
	}

    int readRAM(int address, OperationSize opsize) {
        int data = 0;

        switch (opsize) {
            case BYTE:
                if(address < ram.length) {
                    data = ram[address];
                }
                break;
            case WORD:
                if(address+1 < ram.length) {
                    data  = ram[address] << 8;
                    data |= ram[address + 1];
                }
                break;
            case LONG:
                if(address+3 < ram.length) {
                    data = ram[address] << 24;
                    data |= ram[address + 1] << 16;
                    data |= ram[address + 2] << 8;
                    data |= ram[address + 3];
                }
                break;
        }

        return data;
    }


    int readSRAM(int address, OperationSize opsize) {
        int data = 0;

        switch (opsize) {
            case BYTE:
                if(address < sram.length) {
                    data = sram[address];
                }
                break;
            case WORD:
                if(address+1 < sram.length) {
                    data  = sram[address] << 8;
                    data |= sram[address + 1];
                }
                break;
            case LONG:
                if(address+3 < sram.length) {
                    data = sram[address] << 24;
                    data |= sram[address + 1] << 16;
                    data |= sram[address + 2] << 8;
                    data |= sram[address + 3];
                }
                break;
        }

        return data;
    }

    public void writeSRAM(int address, int data, OperationSize opsize) {

        switch (opsize) {
            case BYTE:
                if(address < sram.length) {
                    sram[address] = data & 0xFF;
                }
                break;
            case WORD:
                if(address+1 < sram.length) {
                    sram[address + 1] = data & 0xFF;
					sram[address] = (data >>> 8) & 0xFF;
                }
                break;
            case LONG:
                if(address+3 < sram.length) {
                    sram[address] = (data >>> 24) & 0xFF;
                    sram[address + 1] = (data >>> 16) & 0xFF;
                    sram[address + 2] = (data >>> 8) & 0xFF;
                    sram[address + 3] = data & 0xFF;
                }
                break;
        }
    }

	int readCartridgeByte(int address) {
		int data = 0;
//		if (address <= 0x3FFFFF) {
//			while (address >= rom.length) {	//	wrapping ? TODO confirmar
//				address -= rom.length;
//			}
//			data = rom[address];
//		}

		if(address < rom.length)
			data = rom[address];

		return data;
	}
	
	long readCartridgeWord(long address) {
		long data = 0;
//		if (address <= 0x3FFFFF) {
			if (address >= rom.length) {	//	wrapping ? TODO confirmar
				address -= rom.length;
			}
			data  = rom[(int) address] << 8;
			data |= rom[(int) address + 1];
//		}
		return data;
	}
	
	long readRam(long address) {
		long data = 0;
		if (address >= 0xFF0000) {
			data = ram[(int) (address - 0xFF0000)];
		}
		return data;
	}
	
	void writeRam(long address, long data) {
		if (address <= 0xFFFF) {
			ram[(int) address] = (int) data;
		} else {
			throw new RuntimeException("READ NOT MAPPED: " + Integer.toHexString((int) address));
		}
	}
	
}
