package instructions.status;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class CLI implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.setFlag(CPU6502.FLAG_INTERRUPT, false); // Clear interrupt disable flag
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
