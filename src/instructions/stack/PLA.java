package instructions.stack;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class PLA implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.SP = (cpu.SP+1) & 0xFF;
        cpu.A = memory.read(0x0100 + cpu.SP);

        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A&0x80) != 0);
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getCycles() {
        return 0;
    }
}
