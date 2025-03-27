package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ANDIndirectX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int zpAddr = (memory.read(cpu.PC++) + cpu.X) & 0xFF;

        int pointerLo = memory.read(zpAddr) & 0xFF;
        int pointerHi = memory.read((zpAddr + 1) & 0xFF) & 0xFF;

        int finalAddr = (pointerHi << 8) | pointerLo;
        int value = memory.read(finalAddr) & 0xFF;

        cpu.A &= value;

        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 6; // May take 1 extra if page crossed, but 6 is fine for now
    }
}
