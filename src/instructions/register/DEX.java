package instructions.register;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class DEX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.X = (cpu.X-1) &0xFF;
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.X&0x80) != 0);
        cpu.setFlag(CPU6502.FLAG_ZERO, (cpu.X&0xFF) == 0);
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getCycles() {
        return 0;
    }
}
