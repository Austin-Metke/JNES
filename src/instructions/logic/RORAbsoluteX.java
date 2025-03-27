package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class RORAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int low = memory.read(cpu.PC++) & 0xFF;
        int high = memory.read(cpu.PC++) & 0xFF;

        int baseAddr = (high << 8) | low;
        int address = (baseAddr + cpu.X) & 0xFFFF;

        int value = memory.read(address) & 0xFF;

        boolean oldCarry = cpu.getFlag(CPU6502.FLAG_CARRY);
        boolean newCarry = (value & 0x01) != 0;

        value = (value >> 1) & 0x7F;

        if (oldCarry) {
            value |= 0x80;
        }

        memory.write(address, value);

        cpu.setFlag(CPU6502.FLAG_CARRY, newCarry);
        cpu.setFlag(CPU6502.FLAG_ZERO, value == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (value & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 6; // 6 cycles for ROR Absolute,X
    }
}
