package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ANDAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        // Fetch low and high bytes of absolute address
        int lowByte = memory.read(cpu.PC++) & 0xFF;
        int highByte = memory.read(cpu.PC++) & 0xFF;

        // Form absolute address and add X register
        int address = ((highByte << 8) | lowByte) + cpu.X;
        address &= 0xFFFF; // Ensure 16-bit wraparound

        // Read value from computed address
        int value = memory.read(address) & 0xFF;

        // Perform AND operation with accumulator
        cpu.A &= value;

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
        return 4; // +1 cycle if page boundary crossed, optional here
    }
}
