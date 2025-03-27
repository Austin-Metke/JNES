package cpu6502;

public class CPU6502 {
    // Registers
    public int A = 0x00;  // Accumulator
    public int X = 0x00;  // X index
    public int Y = 0x00;  // Y index
    public int SP = 0xFD; // Stack Pointer
    public int PC = 0x0000; // Program Counter
    public int status = 0x00; // Processor Status Register



    // Status flag bitmasks
    public static final int FLAG_CARRY     = 0x01; // C
    public static final int FLAG_ZERO      = 0x02; // Z
    public static final int FLAG_INTERRUPT = 0x04; // I
    public static final int FLAG_DECIMAL   = 0x08; // D
    public static final int FLAG_BREAK     = 0x10; // B
    public static final int FLAG_UNUSED    = 0x20; // Unused (always set)
    public static final int FLAG_OVERFLOW  = 0x40; // V
    public static final int FLAG_NEGATIVE  = 0x80; // N
    //Halt execution
    public boolean halted = false;
    public boolean nmiRequested = false;
    public boolean irqRequested = false;

    private int lastPC = 0;
    private int lastOpcode = 0;

    public void requestNMI() {
        this.nmiRequested = true;
    }

    public void requestIRQ() {
        this.irqRequested = true;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private Memory memory;
    private Mode mode;
    private final InstructionSet instructionSet = new InstructionSet();
    public CPU6502(Memory memory) {
        this.memory = memory;
    }

    public CPU6502(Memory memory, Mode mode) {
        this(memory);
        this.mode = mode;
    }

    public void reset() {
        PC = memory.readWord(0xFFFC); // Reset vector
        SP = 0xFD;
        A = X = Y = 0;
        status = 0x24;
        System.out.printf("🔁 CPU Reset — Reset vector loaded: %04X\n", PC);
    }

    public void pushStack(int value, Memory memory) {
        //System.out.printf("📥 PUSH $%02X → [$%04X]\n", value, 0x0100 + SP);
        memory.write(0x0100 + SP, value & 0xFF);
        SP = (SP - 1) & 0xFF;
    }

    public int popStack(Memory memory) {
        SP = (SP + 1) & 0xFF;
        int val = memory.read(0x0100 + SP);
        //System.out.printf("📤 POP  $%02X ← [$%04X]\n", val, 0x0100 + SP);
        return val;
    }


    public int clock() {
        if (halted) return 0;

        // Check NMI (non-maskable, always runs if requested)
        if (nmiRequested) {
            nmiRequested = false;
            handleInterrupt(0xFFFA, false); // false = not BRK
            return 7;
        }

        // Check IRQ
        if (irqRequested && !getFlag(FLAG_INTERRUPT)) {
            irqRequested = false;
            handleInterrupt(0xFFFE, false);
            return 7;
        }
        lastPC = PC;
        if (PC == 0x0000) {
            System.err.println("🚨 PC jumped to $0000 — likely invalid return or vector.");
        }

        int opcode = memory.read(PC++) & 0xFF;
        lastOpcode = opcode;




        return executeInstruction(opcode);
    }



    private int executeInstruction(int opcode) {
        Instruction instr = instructionSet.get(opcode);

        if (instr != null) {
            instr.execute(this, memory);
            return instr.getCycles();
        } else {
            System.err.printf("❌ Illegal opcode: %02X at PC: %04X\n", opcode, PC - 1);
            System.err.printf("🔙 Previous opcode: %02X at PC: %04X\n", lastOpcode, lastPC);
            memory.dumpToBinaryFile("crash_dump.bin");
            halted = true;
            return 0;
        }
    }


    public void handleInterrupt(int vectorAddr, boolean isBRK) {
        int returnPC = isBRK ? PC + 1 : PC;

        // Push PC
        pushStack((returnPC >> 8) & 0xFF, memory);
        pushStack(returnPC & 0xFF, memory);

        // Push status register
        int statusToPush = status | CPU6502.FLAG_UNUSED;
        if (isBRK) {
            statusToPush |= CPU6502.FLAG_BREAK;
        } else {
            statusToPush &= ~CPU6502.FLAG_BREAK;
        }
        pushStack(statusToPush, memory);

        // Set Interrupt Disable
        setFlag(FLAG_INTERRUPT, true);

        // Load new PC from vector
        int newPC = memory.readWord(vectorAddr);
        PC = newPC;

        // Print the interrupt type and destination
        String type = isBRK ? "BRK" : (vectorAddr == 0xFFFA ? "NMI" : "IRQ");
       // System.out.printf("🚨 Interrupt triggered: %s → jumping to $%04X\n", type, newPC);
    }


    public boolean getFlag(int flag) {
        return (status & flag) != 0;
    }

    public void setFlag(int flag, boolean value) {
        if (value) {
            status |= flag;
        } else {
            status &= ~flag;
        }
    }

    private String decodeOpcode(int opcode) {
        Instruction instr = instructionSet.get(opcode);
        if (instr == null) {
            return "???";
        }
        return instr.getClass().getSimpleName();
    }


    public void printState() {

        System.out.println("=== CPU STATE ===");
        System.out.printf("PC:  $%04X\n", PC);
        System.out.printf("A:   $%02X  X: $%02X  Y: $%02X\n", A, X, Y);
        System.out.printf("SP:  $%02X (stack top: $%02X)\n", SP, memory.read(0x0100 + ((SP + 1) & 0xFF)));
        System.out.printf("STATUS: %s%s%s%s%s%s%s%s\n",
                getFlag(FLAG_NEGATIVE)  ? "N" : ".",
                getFlag(FLAG_OVERFLOW)  ? "V" : ".",
                getFlag(FLAG_UNUSED)    ? "U" : ".",
                getFlag(FLAG_BREAK)     ? "B" : ".",
                getFlag(FLAG_DECIMAL)   ? "D" : ".",
                getFlag(FLAG_INTERRUPT) ? "I" : ".",
                getFlag(FLAG_ZERO)      ? "Z" : ".",
                getFlag(FLAG_CARRY)     ? "C" : ".");
        System.out.println("==================");
    }

    public void debugState(Memory memory) {
        System.out.println("=== CPU STATE ===");
        System.out.printf("PC:  $%04X\n", PC);
        System.out.printf("A:   $%02X  X: $%02X  Y: $%02X\n", A, X, Y);
        System.out.printf("SP:  $%02X (stack top: $%02X)\n", SP, memory.read(0x0100 + ((SP + 1) & 0xFF)));
        System.out.printf("STATUS: %s\n", formatFlags());
        System.out.println("==================");

        System.out.println("--- Memory Dump ($0200–$0210) ---");
        for (int i = 0x0200; i <= 0x0210; i++) {
            System.out.printf("$%04X: %02X\n", i, memory.read(i));
        }

        System.out.println("--- Memory Dump ($0300–$0310) ---");
        for (int i = 0x0300; i <= 0x0310; i++) {
            System.out.printf("$%04X: %02X\n", i, memory.read(i));
        }

        System.out.println("--- Stack Page ($01FA–$01FF) ---");
        for (int i = 0x01FA; i <= 0x01FF; i++) {
            System.out.printf("$%04X: %02X\n", i, memory.read(i));
        }
    }
    private String formatFlags() {
        return String.format("%c%c%c%c%c%c%c%c",
                getFlag(FLAG_NEGATIVE) ? 'N' : '.',
                getFlag(FLAG_OVERFLOW) ? 'V' : '.',
                true ? 'U' : '.', // Unused flag (always set)
                getFlag(FLAG_BREAK) ? 'B' : '.',
                getFlag(FLAG_DECIMAL) ? 'D' : '.',
                getFlag(FLAG_INTERRUPT) ? 'I' : '.',
                getFlag(FLAG_ZERO) ? 'Z' : '.',
                getFlag(FLAG_CARRY) ? 'C' : '.');
    }


}
