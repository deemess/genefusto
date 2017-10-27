package gen;

public abstract class Instruction {

	public Instruction() {
	}

	OperationSize mapSize(int siz) {
		if (siz == 0b01) {
			return OperationSize.BYTE;
		} else if (siz == 0b11) {
			return OperationSize.WORD;
		} else if (siz == 0b10) {
			return OperationSize.LONG;
		}
		return null;
	}
	
//	SIZE
//	00->one Byte operation
//	01->one Word operation
//	10->one Long operation
OperationSize mapAlternateSize(int size) {
		if (size == 0b00) {
			return OperationSize.BYTE;
		} else if (size == 0b01) {
			return OperationSize.WORD;
		} else if (size == 0b10) {
			return OperationSize.LONG;
		}
		return null;
	}
	
//	OP-MODE
//	Byte	Word	Long
//	~~~~	~~~~	~~~~
//	000		001		010	(Dn) - (<ea>) -> Dn
//	100		101		110	(<ea>) - (Dn) -> <ea>
OperationSize mapFromOpMode(int size) {
		if (size == 0b000 || size == 0b100) {
			return OperationSize.BYTE;
		} else if (size == 0b001 || size == 0b101) {
			return OperationSize.WORD;
		} else if (size == 0b010 || size == 0b110) {
			return OperationSize.LONG;
		}
		return null;
	}
	
	public abstract void run(int opcode);
}
