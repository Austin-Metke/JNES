package instructions.system;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class BRK implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        // Advance past the BRK opcode
        cpu.PC++;

        // Trigger BRK-style interrupt (true = BRK)
        cpu.handleInterrupt(0xFFFE, true);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getCycles() {
        return 7; // BRK takes 7 cycles
    }
}
