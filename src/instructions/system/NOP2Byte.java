package instructions.system;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class NOP2Byte implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.PC++; // skip operand
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 2;
    }
}
