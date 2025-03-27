package instructions.status;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class BITAbsolute implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lower = memory.read(cpu.PC++)&0xFF;
        int upper = memory.read(cpu.PC++)&0xFF;

        int address = (upper<<8) | lower;
        int value = memory.read(address);
        cpu.setFlag(CPU6502.FLAG_ZERO,(cpu.A&value) == 0);

        cpu.setFlag(CPU6502.FLAG_OVERFLOW, (value&0x40) != 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (value&0x80) != 0);

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
