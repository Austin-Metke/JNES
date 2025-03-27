package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ROLAccumulator implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        // Save original bit 7 for carry
        boolean newCarry = (cpu.A & 0x80) != 0;

        // Shift accumulator left by 1
        cpu.A = (cpu.A << 1) & 0xFF; // Masking to keep within 8 bits

        // Add previous carry flag to bit 0
        if (cpu.getFlag(CPU6502.FLAG_CARRY)) {
            cpu.A |= 0x01;
        }

        // Set the new carry flag
        cpu.setFlag(CPU6502.FLAG_CARRY, newCarry);

        // Update Zero and Negative flags (typically done)
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
