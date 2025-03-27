package instructions.register;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class INY implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        cpu.Y = (cpu.Y-1) &0xFF;
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (cpu.Y&0x80) != 0);
        cpu.setFlag(CPU6502.FLAG_ZERO, (cpu.Y&0xFF) == 0);
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
