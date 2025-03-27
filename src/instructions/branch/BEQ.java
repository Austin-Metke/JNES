package instructions.branch;

import cpu6502.CPU6502;

public class BEQ extends Branch {
    @Override
    protected boolean shouldBranch(CPU6502 cpu) {
        return cpu.getFlag(CPU6502.FLAG_ZERO);
    }
}
