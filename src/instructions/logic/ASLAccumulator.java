package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ASLAccumulator implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {

        boolean newCarry = (cpu.A & 0x80) != 0;

        cpu.A = (cpu.A << 1)&0xFF;



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
