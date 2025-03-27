package instructions.math;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class SBCAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        // Fetch absolute address (16-bit)
        int lower = memory.read(cpu.PC++) & 0xFF;
        int upper = memory.read(cpu.PC++) & 0xFF;
        int address = ((upper << 8) | lower) + cpu.X;
        address &= 0xFFFF; // Ensure 16-bit wraparound

        // Fetch operand
        int operand = memory.read(address) & 0xFF;

        // Perform subtraction: A = A - operand - (1 - Carry)
        int carry = cpu.getFlag(CPU6502.FLAG_CARRY) ? 1 : 0;
        int result = cpu.A + (operand ^ 0xFF) + carry;

        // Update Carry (set if result >= 0)
        cpu.setFlag(CPU6502.FLAG_CARRY, result > 0xFF);

        // Update Overflow flag:
        // Overflow if signed overflow: ((A ^ result) & (operand ^ result) & 0x80) != 0
        cpu.setFlag(CPU6502.FLAG_OVERFLOW, ((cpu.A ^ result) & ((operand ^ 0xFF) ^ result) & 0x80) != 0);

        // Mask result to 8 bits and store back to A
        cpu.A = result & 0xFF;

        // Update Zero and Negative flags
        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 4; // +1 if page boundary crossed (optional to implement)
    }
}
