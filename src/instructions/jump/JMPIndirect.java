package instructions.jump;

import cpu6502.CPU6502;
import cpu6502.Instruction;
import cpu6502.Memory;

public class JMPIndirect implements Instruction {
    @Override
    public void execute(CPU6502 cpu, Memory memory) {
        int lower = memory.read(cpu.PC++)&0xFF;
        int upper = memory.read(cpu.PC++)&0xFF;

        int jmpAddress = (upper<<8) | lower;
        int jumpLocationLower = memory.read(jmpAddress);
        int jumpLocationUpper = memory.read(jmpAddress+1);

        cpu.PC = (jumpLocationUpper<<8) | jumpLocationLower;

    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public int getCycles() {
        return 0;
    }
}
