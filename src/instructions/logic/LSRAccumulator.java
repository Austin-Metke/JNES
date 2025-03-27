package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class LSRAccumulator implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.setFlag(CPU6502.FLAG_CARRY, (cpu.A&0x01) != 0);
        cpu.A >>= 1;
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A&0x80) != 0);

        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, false); // Bit 7 will always be 0 after LSR
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getCycles() {
        return 2;
    }
}
