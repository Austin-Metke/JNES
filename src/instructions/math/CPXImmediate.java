package instructions.math;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class CPXImmediate implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int immediate = memory.read(cpu.PC++)&0xFF;
        int result = (cpu.X&0xFF) - immediate;

        cpu.setFlag(CPU6502.FLAG_ZERO, result == 0);
        cpu.setFlag(CPU6502.FLAG_CARRY, (cpu.X >= immediate));
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (result&0x80) != 0);


    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 0;
    }
}
