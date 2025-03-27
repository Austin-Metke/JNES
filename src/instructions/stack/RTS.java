package instructions.stack;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;
import cpu6502.Mode;

public class RTS implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int low = cpu.popStack(memory);
        int high = cpu.popStack(memory);
        int returnAddr = ((high << 8) | low);
        int newPC = (returnAddr + 1) & 0xFFFF;

        if (cpu.getMode() == Mode.DEBUG) {
            System.out.printf("🔁 RTS pulled return address: %04X → jumping to %04X\n", returnAddr, newPC);
        }

        cpu.PC = newPC;
    }

    @Override
    public int getSize() {
        return 1; // implied
    }

    @Override
    public int getCycles() {
        return 6; // RTS takes 6 cycles
    }
}
