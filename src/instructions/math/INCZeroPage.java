package instructions.math;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class INCZeroPage implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int addr = memory.read(cpu.PC++) & 0xFF;
        int value = (memory.read(addr)+1)&0xFF;
        memory.write(addr, value);

        // Update CPU flags
        cpu.setFlag(CPU6502.FLAG_ZERO, value == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (value & 0x80) != 0);

    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 5;
    }
}
