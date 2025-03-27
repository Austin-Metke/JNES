package instructions.stack;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class RTI implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        // Pull status from stack (including BREAK and UNUSED bits)
        cpu.status = cpu.popStack(memory) & 0xEF | CPU6502.FLAG_UNUSED; // clear BREAK bit (bit 4), set UNUSED bit (bit 5)

        // Pull PC from stack (low then high)
        int lo = cpu.popStack(memory);
        int hi = cpu.popStack(memory);
        cpu.PC = (hi << 8) | lo;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getCycles() {
        return 6;
    }
}
