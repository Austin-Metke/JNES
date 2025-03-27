package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class STAZeroPage implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int address = memory.read(cpu.PC++) & 0xFF;
        memory.write(address, cpu.A);
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
