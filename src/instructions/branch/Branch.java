package instructions.branch;

import cpu6502.*;

public abstract class Branch implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int offset = memory.read(cpu.PC++) & 0xFF;
        if (shouldBranch(cpu)) {
            int signedOffset = (offset < 0x80) ? offset : offset - 0x100;
            cpu.PC = (cpu.PC + signedOffset) & 0xFFFF;
        }
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 2;
    }

    // Subclasses implement this to define the condition
    protected abstract boolean shouldBranch(CPU6502 cpu);
}

