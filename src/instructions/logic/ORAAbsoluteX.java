package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ORAAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lower = memory.read(cpu.PC++)&0xFF;
        int upper = memory.read(cpu.PC++)&0xFF;

        int address = (((upper<<8) | lower)+cpu.X)&0xFFFF;
        int operand = memory.read(address)&0xFF;

        cpu.A |= operand;
        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 4;
    }
}
