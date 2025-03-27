package instructions.stack;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class JSR implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int low = memory.read(cpu.PC++) & 0xFF;
        int high = memory.read(cpu.PC++) & 0xFF;
        int target = (high << 8) | low;

        // Push return address (PC - 1) to the stack
        int returnAddr = (cpu.PC - 1) & 0xFFFF;
        cpu.pushStack((returnAddr >> 8) & 0xFF, memory); // High byte
        cpu.pushStack(returnAddr & 0xFF, memory);        // Low byte

        cpu.PC = target;
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 6; // instructions.stack.JSR takes 6 cycles
    }
}
