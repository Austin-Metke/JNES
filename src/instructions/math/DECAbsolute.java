package instructions.math;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class DECAbsolute implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lower = memory.read(cpu.PC++) & 0xFF;
        int upper = memory.read(cpu.PC++) & 0xFF;

        int address = ((upper << 8) | lower) & 0xFFFF;

        // Read the value, decrement, and mask to 8 bits
        int result = (memory.read(address) - 1) & 0xFF;

        // Write the masked result back to memory
        memory.write(address, result);

        // Update flags
        cpu.setFlag(CPU6502.FLAG_ZERO, result == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (result & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 6;
    }
}
