package cpu6502;

import instructions.compare.*;
import instructions.loadstore.*;
import instructions.branch.*;
import instructions.logic.*;
import instructions.math.*;
import instructions.register.*;
import instructions.status.*;
import instructions.system.*;
import instructions.stack.*;
import instructions.jump.*;

import java.util.HashMap;
import java.util.Map;
public class InstructionSet {
    private final Map<Integer, Instruction> instructions = new HashMap<>();

    public InstructionSet() {
        loadInstructions();
    }

    private void loadInstructions() {

        //Jump Instructions
        instructions.put(0x4C, new JMPDirect());
        instructions.put(0x6C, new JMPIndirect());

        //Load Instructions
        instructions.put(0xA9, new LDAImmediate());
        instructions.put(0xBD, new LDAAbsoluteX());
        instructions.put(0xAD, new LDAAbsolute());
        instructions.put(0xB9, new LDAAbsoluteY());
        instructions.put(0xA2, new LDXImmediate());
        instructions.put(0xA6, new LDXZeroPage());
        instructions.put(0xBE, new LDXAbsoluteY());
        instructions.put(0xAE, new LDXAbsolute());
        instructions.put(0xB6, new LDXZeroPageY());
        instructions.put(0xA0, new LDYImmediate());
        instructions.put(0xAC, new LDYAbsolute());
        instructions.put(0xB4, new LDYZeroPageX());
        instructions.put(0xA4, new LDYZeroPage());
        instructions.put(0xBC, new LDYAbsoluteX());
        instructions.put(0x9D, new STAAbsoluteX());
        instructions.put(0x8D, new STAAbsolute());
        instructions.put(0x85, new STAZeroPage());
        instructions.put(0x86, new STXZeroPage());
        instructions.put(0x91, new STAIndirectY());
        instructions.put(0x99, new STAAbsoluteY());
        instructions.put(0xB1, new LDAIndirectY());
        //System instructions
        instructions.put(0xEA, new NOP());
        instructions.put(0x00, new BRK());

        //Status instructions
        instructions.put(0x18, new CLC());
        instructions.put(0x78, new SEI());
        instructions.put(0x58, new CLI());
        instructions.put(0xD8, new CLD());
        instructions.put(0xF8, new SED());
        instructions.put(0x2C, new BITAbsolute());
        instructions.put(0x38, new SEC());

        //Math instructions
        instructions.put(0xE0, new CPXImmediate());
        instructions.put(0x7D, new ADCAbsoluteX());
        instructions.put(0xF9, new SBCAbsoluteY());
        instructions.put(0xFD, new SBCAbsoluteX());
        instructions.put(0xE6, new INCZeroPage());

        //Register instructions
        instructions.put(0xE8, new INX());
        instructions.put(0xCA, new DEX());
        instructions.put(0x88, new DEY());
        instructions.put(0xC8, new INY());
        instructions.put(0xCE, new DECAbsolute());
        //Stack instructions
        instructions.put(0x20, new JSR());
        instructions.put(0x60, new RTS());
        instructions.put(0x40, new RTI());
        instructions.put(0x9A, new TXS());
        instructions.put(0xBA, new TSX());
        instructions.put(0x8A, new TXA());
        instructions.put(0xAA, new TAX());
        instructions.put(0x48, new PHA());
        instructions.put(0x68, new PLA());
        instructions.put(0xA8, new TAY());

        //Compare Instructions
        instructions.put(0xC9, new CMPImmediate());
        instructions.put(0xC0, new CPYImmediate());

        //Logic Instructions
        instructions.put(0x09, new ORAImmediate());
        instructions.put(0x29, new ANDIndirectX());
        instructions.put(0x4A, new LSRAccumulator());
        instructions.put(0x05, new ORAZeroPage());
        instructions.put(0x2A, new ROLAccumulator());
        instructions.put(0x3D, new ANDAbsoluteX());
        instructions.put(0x45, new EORZeroPage());
        instructions.put(0x7E, new RORAbsoluteX());
        instructions.put(0x6A, new RORAccumulator());
        instructions.put(0x0A, new ASLAccumulator());
        instructions.put(0x19, new ORAAbsoluteY());
        instructions.put(0x1D, new ORAAbsoluteX());
        instructions.put(0x1E, new ASLAbsoluteX());

        //Branch Instructions
        instructions.put(0x10, new BPL());
        instructions.put(0x30, new BMI());
        instructions.put(0x50, new BVC());
        instructions.put(0x70, new BVS());
        instructions.put(0x90, new BCC());
        instructions.put(0xB0, new BCS());
        instructions.put(0xD0, new BNE());
        instructions.put(0xF0, new BEQ());


        //NOP instructions (0xEA defined above)
        NOP nop = new NOP();
        instructions.put(0x1A, nop);
        instructions.put(0x3A, nop);
        instructions.put(0x5A, nop);
        instructions.put(0x7A, nop);
        instructions.put(0xDA, nop);
        instructions.put(0xFA, nop);

        //2 Byte NOPs
        Instruction nop2 = new NOP2Byte();

        instructions.put(0x82, nop2);
        instructions.put(0x89, nop2);
        instructions.put(0xC2, nop2);
        instructions.put(0xE2, nop2);


        //Illegal instructions
        instructions.put(0x1C, new NOPAbsoluteX());


    }

    public Instruction get(int opcode) {
        return instructions.get(opcode);
    }
}
