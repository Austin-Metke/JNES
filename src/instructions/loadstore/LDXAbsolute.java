package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Memory;

public class LDXAbsolute extends Load {

    @Override
    protected int resolveOperand(CPU6502 cpu, Memory memory) {
        int lo = memory.read(cpu.PC++) & 0xFF;
        int hi = memory.read(cpu.PC++) & 0xFF;
        int addr = (hi << 8) | lo;
        return memory.read(addr) & 0xFF;
    }

    @Override
    protected void store(CPU6502 cpu, int value) {
        cpu.X = value;
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 4;
    }
}
