package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ASLAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        // Fetch the base address (2 bytes: low + high)
        int lo = memory.read(cpu.PC++) & 0xFF;
        int hi = memory.read(cpu.PC++) & 0xFF;
        int baseAddr = (hi << 8) | lo;

        // Add X and wrap around 16 bits
        int addr = (baseAddr + cpu.X) & 0xFFFF;

        // Read, shift, and write back
        int value = memory.read(addr) & 0xFF;
        boolean carryOut = (value & 0x80) != 0;

        value = (value << 1) & 0xFF; // Shift left and mask to 8 bits

        memory.write(addr, value);

        // Set flags
        cpu.setFlag(CPU6502.FLAG_CARRY, carryOut);
        cpu.setFlag(CPU6502.FLAG_ZERO, value == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (value & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 6; // +1 if page crossed, optional
    }
}
