package instructions.stack;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class PHA implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.pushStack(cpu.A, memory);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getCycles() {
        return 3;
    }
}
