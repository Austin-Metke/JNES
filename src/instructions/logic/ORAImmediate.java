package instructions.logic;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class ORAImmediate implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int value = memory.read(cpu.PC++) & 0xFF;
        cpu.A |=  value;

        cpu.setFlag(CPU6502.FLAG_ZERO, cpu.A == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.A & 0x80) != 0);

    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 2;
    }
}
