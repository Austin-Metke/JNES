package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Memory;

public class LDYImmediate extends Load {

    @Override
    protected int resolveOperand(CPU6502 cpu, Memory memory) {
        return memory.read(cpu.PC++) & 0xFF;
    }

    @Override
    protected void store(CPU6502 cpu, int value) {
        cpu.Y = value;
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
