package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class LDXZeroPage implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int addr = memory.read(cpu.PC++) & 0xFF;
        cpu.X = memory.read(addr) & 0xFF;

        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.X == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.X & 0x80) != 0);
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 3;
    }
}
