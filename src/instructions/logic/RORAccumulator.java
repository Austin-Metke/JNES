package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class RORAccumulator implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        boolean oldCarry = cpu.getFlag(CPU6502.FLAG_CARRY);
        boolean newCarry = (cpu.A & 0x01) != 0;

        cpu.A = (cpu.A >> 1) & 0x7F;
        if (oldCarry) {
            cpu.A |= 0x80;
        }

        cpu.setFlag(CPU6502.FLAG_CARRY, newCarry);
        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A & 0x80) != 0);
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
