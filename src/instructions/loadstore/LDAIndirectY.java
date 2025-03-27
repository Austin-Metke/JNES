package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Memory;

public class LDAIndirectY extends Load {
    @Override
    protected int resolveOperand(CPU6502 cpu, Memory memory) {
        int zpAddr = memory.read(cpu.PC++) & 0xFF;

        int baseLo = memory.read(zpAddr) & 0xFF;
        int baseHi = memory.read((zpAddr + 1) & 0xFF) & 0xFF;

        int baseAddress = (baseHi << 8) | baseLo;
        int finalAddress = (baseAddress + cpu.Y) & 0xFFFF;

        return memory.read(finalAddress) & 0xFF;
    }

    @Override
    protected void store(CPU6502 cpu, int value) {
        cpu.A = value;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 5; // Add +1 cycle if page crossed (optional)
    }
}
