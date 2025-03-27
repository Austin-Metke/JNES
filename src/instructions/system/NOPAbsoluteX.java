package instructions.system;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class NOPAbsoluteX implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lo = memory.read(cpu.PC++) & 0xFF;
        int hi = memory.read(cpu.PC++) & 0xFF;
        int addr = ((hi << 8) | lo) + cpu.X;
        memory.read(addr & 0xFFFF); // discard value
    }


    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 4; // Optional: +1 if page crossed
    }
}
