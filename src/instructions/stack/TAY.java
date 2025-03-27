package instructions.stack;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class TAY implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.Y = cpu.A;
        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.Y == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.Y&0x80) != 0);
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
