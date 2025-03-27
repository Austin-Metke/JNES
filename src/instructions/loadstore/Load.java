package instructions.loadstore;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public abstract class Load implements Instruction {

    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int value = resolveOperand(cpu, memory);
        store(cpu, value);

        cpu.setFlag(CPU6502.FLAG_ZERO, value == 0);
        cpu.setFlag(CPU6502.FLAG_NEGATIVE, (value & 0x80) != 0);
    }

    // Each subclass defines how to resolve its operand (immediate, absolute, etc.)
    protected abstract int resolveOperand(CPU6502 cpu, Memory memory);

    // Each subclass defines which register to store the result in
    protected abstract void store(CPU6502 cpu, int value);
}
