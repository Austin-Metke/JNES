package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Memory;

public class LDAAbsoluteX extends Load {

    @Override
    protected int resolveOperand(CPU6502 cpu, Memory memory) {
        int lo = memory.read(cpu.PC++) & 0xFF;
        int hi = memory.read(cpu.PC++) & 0xFF;
        int base = (hi << 8) | lo;
        int address = (base + cpu.X) & 0xFFFF;
        return memory.read(address) & 0xFF;
    }

    @Override
    protected void store(CPU6502 cpu, int value) {
        cpu.A = value;
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 4; // Add +1 cycle if page crossed (optional)
    }
}
