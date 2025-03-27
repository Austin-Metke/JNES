package instructions.compare;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class CPYImmediate implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int value = memory.read(cpu.PC++) & 0xFF;
        int result = (cpu.Y - value) & 0xFF;

        cpu.setFlag(CPU6502.FLAG_CARRY, cpu.Y >= value);
        cpu.setFlag(CPU6502.FLAG_ZERO, result == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (result & 0x80) != 0);
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
