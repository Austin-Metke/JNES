package instructions.math;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ADCAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lower = memory.read(cpu.PC++)&0xFF;
        int upper = memory.read(cpu.PC++)&0xFF;

        int address = (upper<<8) | lower;
        int result = memory.read((address+cpu.X)&0xFFFF);
        result = cpu.A + result + (cpu.getFlag(CPU6502.FLAG_CARRY) ? 1 : 0);
        cpu.setFlag(CPU6502.FLAG_CARRY, result > 0xFF);
        cpu.A = result&0xFF;
        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, cpu.A != 0);


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
