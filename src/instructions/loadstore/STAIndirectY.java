package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class STAIndirectY implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int zpAddr = memory.read(cpu.PC++) & 0xFF;

        int pointerLo = memory.read(zpAddr) & 0xFF;
        int pointerHi = memory.read((zpAddr + 1) & 0xFF) & 0xFF;

        int baseAddr = (pointerHi << 8) | pointerLo;
        int finalAddr = (baseAddr + cpu.Y) & 0xFFFF;

        memory.write(finalAddr, cpu.A);
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int getCycles() {
        return 6; // add 1 cycle if page boundary crossed (not required unless accurate timing)
    }
}
