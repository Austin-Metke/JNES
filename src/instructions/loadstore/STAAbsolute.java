package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class STAAbsolute implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lower = memory.read(cpu.PC++)&0xFF;
        int upper = memory.read(cpu.PC++)&0xFF;

        int address = (upper<<8) | lower;
        memory.write(address, cpu.A);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 0;
    }
}
