package cpu6502;

public interface Instruction {
    void execute(CPU6502 cpu, Memory memory);
    int getSize(); // bytes to advance program counter (PC)

    int getCycles();

    public static void applyBranchIf(CPU6502 cpu, Memory memory, boolean condition) {
        int offset = memory.read(cpu.PC++) & 0xFF;

        if (condition) {
            int signedOffset = (offset < 0x80) ? offset : offset - 0x100;
            int oldPC = cpu.PC;
            cpu.PC = (cpu.PC + signedOffset) & 0xFFFF;

            // Optional: count page crossing
            // if ((oldPC & 0xFF00) != (cpu.PC & 0xFF00)) {
            //     cpu.incrementCycles(1);
            // }
        }
    }

}
