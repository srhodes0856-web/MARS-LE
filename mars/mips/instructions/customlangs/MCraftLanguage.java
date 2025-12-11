package mars.mips.instructions.customlangs;

import mars.simulator.*;
import mars.mips.hardware.*;
import mars.*;
import mars.util.*;
import mars.mips.instructions.*;

/**
 * M-CRAFT custom language implementation.
 *
 * Implements:
 *  - 14 basic MIPS-style instructions (ADD, SUB, AND, OR, XOR, SLT,
 *    ADDI, ANDI, ORI, LW, SW, BEQ, BNE, J)
 *  - Unique Minecraft-themed instructions (MINE, PLACE, CRAFT, BREW,
 *    SMELT, HOTBAR, INVLD, INVST, CHAT, SPRINT, IFMOB)
 */
public class MCraftLanguage extends CustomAssembly {

    // WORLD + INVENTORY memory regions placed inside valid MARS data space
    private static final int WORLD_BASE     = 0x10010000;
    private static final int INVENTORY_BASE = 0x10020000;

    @Override
    public String getName() {
        return "M-CRAFT Language";
    }

    @Override
    public String getDescription() {
        return "Minecraft-themed 32-bit ISA with WORLD, INVENTORY, and syscall-like actions.";
    }

    @Override
    protected void populate() {

        /* ============================================================
         *  BASIC MIPS-STYLE INSTRUCTIONS (14)
         * ============================================================
         */

        instructionList.add(new BasicInstruction(
            "ADD $t1,$t2,$t3",
            "ADD: $t1 = $t2 + $t3",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 100000",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) + RegisterFile.getValue(op[2]));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "SUB $t1,$t2,$t3",
            "SUB: $t1 = $t2 - $t3",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 100010",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) - RegisterFile.getValue(op[2]));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "AND $t1,$t2,$t3",
            "AND: $t1 = $t2 & $t3",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 100100",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) & RegisterFile.getValue(op[2]));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "OR $t1,$t2,$t3",
            "OR: $t1 = $t2 | $t3",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 100101",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) | RegisterFile.getValue(op[2]));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "XOR $t1,$t2,$t3",
            "XOR: $t1 = $t2 ^ $t3",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 100110",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) ^ RegisterFile.getValue(op[2]));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "SLT $t1,$t2,$t3",
            "SLT: $t1 = 1 if $t2 < $t3",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 101010",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        (RegisterFile.getValue(op[1]) < RegisterFile.getValue(op[2])) ? 1 : 0);
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "ADDI $t1,$t2,-100",
            "ADDI: $t1 = $t2 + imm",
            BasicInstructionFormat.I_FORMAT,
            "001000 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) + op[2]);
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "ANDI $t1,$t2,0xFFFF",
            "ANDI: $t1 = $t2 & zeroext(imm)",
            BasicInstructionFormat.I_FORMAT,
            "001100 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) & (op[2] & 0xFFFF));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "ORI $t1,$t2,0xFFFF",
            "ORI: $t1 = $t2 | zeroext(imm)",
            BasicInstructionFormat.I_FORMAT,
            "001101 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    RegisterFile.updateRegister(op[0],
                        RegisterFile.getValue(op[1]) | (op[2] & 0xFFFF));
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "LW $t1,0($t2)",
            "LW: load MEM[$t2 + offset]",
            BasicInstructionFormat.I_FORMAT,
            "100011 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int addr = RegisterFile.getValue(op[1]) + op[2];
                    try {
                        RegisterFile.updateRegister(op[0],
                            Memory.getInstance().getWord(addr));
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "SW $t1,0($t2)",
            "SW: store $t1 into MEM[$t2 + offset]",
            BasicInstructionFormat.I_FORMAT,
            "101011 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int addr = RegisterFile.getValue(op[1]) + op[2];
                    try {
                        Memory.getInstance().setWord(addr,
                            RegisterFile.getValue(op[0]));
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "BEQ $t1,$t2,label",
            "BEQ: branch if equal",
            BasicInstructionFormat.I_BRANCH_FORMAT,
            "000100 fffff sssss tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    if (RegisterFile.getValue(op[0]) == RegisterFile.getValue(op[1])) {
                        Globals.instructionSet.processBranch(op[2]);
                    }
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "BNE $t1,$t2,label",
            "BNE: branch if not equal",
            BasicInstructionFormat.I_BRANCH_FORMAT,
            "000101 fffff sssss tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    if (RegisterFile.getValue(op[0]) != RegisterFile.getValue(op[1])) {
                        Globals.instructionSet.processBranch(op[2]);
                    }
                }
            }
        ));

        instructionList.add(new BasicInstruction(
            "J target",
            "J: unconditional jump",
            BasicInstructionFormat.J_FORMAT,
            "000010 ffffffffffffffffffffffffff",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int target = op[0];
                    int newPC = (RegisterFile.getProgramCounter() & 0xF0000000)
                              | (target << 2);
                    Globals.instructionSet.processJump(newPC);
                }
            }
        ));

        /* ============================================================
         *  UNIQUE M-CRAFT INSTRUCTIONS
         * ============================================================
         */

        // MINE
        instructionList.add(new BasicInstruction(
            "MINE $t1,0($t2)",
            "MINE: load WORLD[$t2 + offset] into $t1",
            BasicInstructionFormat.I_FORMAT,
            "110000 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int index = RegisterFile.getValue(op[1]) + op[2];
                    int addr  = WORLD_BASE + index * 4;
                    try {
                        RegisterFile.updateRegister(op[0],
                            Memory.getInstance().getWord(addr));
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));

        // PLACE
        instructionList.add(new BasicInstruction(
            "PLACE $t1,0($t2)",
            "PLACE: store $t1 into WORLD[$t2 + offset]",
            BasicInstructionFormat.I_FORMAT,
            "110001 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int index = RegisterFile.getValue(op[1]) + op[2];
                    int addr  = WORLD_BASE + index * 4;
                    try {
                        Memory.getInstance().setWord(addr,
                            RegisterFile.getValue(op[0]));
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));

        // CRAFT
        instructionList.add(new BasicInstruction(
            "CRAFT $t1,$t2,$t3",
            "CRAFT: combine lower 16 bits",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 110000",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int a = RegisterFile.getValue(op[1]) & 0xFFFF;
                    int b = RegisterFile.getValue(op[2]) & 0xFFFF;
                    RegisterFile.updateRegister(op[0], (a << 16) | b);
                }
            }
        ));

        // BREW
        instructionList.add(new BasicInstruction(
            "BREW $t1,$t2,$t3",
            "BREW: hashing-like mix",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss ttttt fffff 00000 110001",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int a = RegisterFile.getValue(op[1]);
                    int b = RegisterFile.getValue(op[2]);
                    int brewed = (a ^ (b << 3)) + (b >>> 2);
                    RegisterFile.updateRegister(op[0], brewed);
                }
            }
        ));

        // SMELT
        instructionList.add(new BasicInstruction(
            "SMELT $t1,$t2",
            "SMELT: clamp $t2 into [0,255]",
            BasicInstructionFormat.R_FORMAT,
            "000000 sssss 00000 fffff 00000 110010",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int v = RegisterFile.getValue(op[1]);
                    if (v < 0) v = 0;
                    else if (v > 255) v = 255;
                    RegisterFile.updateRegister(op[0], v);
                }
            }
        ));

        // HOTBAR
        instructionList.add(new BasicInstruction(
            "HOTBAR -100",
            "HOTBAR: select hotbar bank",
            BasicInstructionFormat.I_FORMAT,
            "110101 00000 00000 ffffffffffffffff",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int imm = s.getOperands()[0];
                    RegisterFile.updateRegister(26, imm & 0x7);
                }
            }
        ));

        // INVLD
        instructionList.add(new BasicInstruction(
            "INVLD $t1,0($t2)",
            "INVLD: load from INVENTORY",
            BasicInstructionFormat.I_FORMAT,
            "110110 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int index = RegisterFile.getValue(op[1]) + op[2];
                    int addr  = INVENTORY_BASE + index * 4;
                    try {
                        RegisterFile.updateRegister(op[0],
                            Memory.getInstance().getWord(addr));
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));

        // INVST
        instructionList.add(new BasicInstruction(
            "INVST $t1,0($t2)",
            "INVST: store into INVENTORY",
            BasicInstructionFormat.I_FORMAT,
            "110111 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int index = RegisterFile.getValue(op[1]) + op[2];
                    int addr  = INVENTORY_BASE + index * 4;
                    try {
                        Memory.getInstance().setWord(addr,
                            RegisterFile.getValue(op[0]));
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));

        // CHAT — direct print
        instructionList.add(new BasicInstruction(
            "CHAT $t1",
            "CHAT: print integer in $t1 to Run I/O",
            BasicInstructionFormat.I_FORMAT,
            "111000 fffff 00000 0000000000000000",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int rs = s.getOperands()[0];
                    int val = RegisterFile.getValue(rs);

                    // Direct printing to MARS Run I/O
                    SystemIO.printString(Integer.toString(val));
                    SystemIO.printString("\n");
                }
            }
        ));

        // SPRINT
        instructionList.add(new BasicInstruction(
            "SPRINT $t1,label",
            "SPRINT: decrement and branch while not zero",
            BasicInstructionFormat.I_BRANCH_FORMAT,
            "111010 fffff 00000 ssssssssssssssss",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int rs = op[0];
                    int offset = op[2];

                    int newVal = RegisterFile.getValue(rs) - 1;
                    RegisterFile.updateRegister(rs, newVal);
                    if (newVal != 0) {
                        Globals.instructionSet.processBranch(offset);
                    }
                }
            }
        ));

        // IFMOB
        instructionList.add(new BasicInstruction(
            "IFMOB $t1,0($t2)",
            "IFMOB: $t1 = 1 if WORLD[...] < 0 else 0",
            BasicInstructionFormat.I_FORMAT,
            "111011 sssss fffff tttttttttttttttt",
            new SimulationCode() {
                public void simulate(ProgramStatement s) throws ProcessingException {
                    int[] op = s.getOperands();
                    int rt = op[0];
                    int rs = op[1];
                    int imm = op[2];

                    int index = RegisterFile.getValue(rs) + imm;
                    int addr  = WORLD_BASE + index * 4;

                    try {
                        int v = Memory.getInstance().getWord(addr);
                        RegisterFile.updateRegister(rt, (v < 0) ? 1 : 0);
                    } catch (AddressErrorException e) {
                        throw new ProcessingException(s, e);
                    }
                }
            }
        ));
    }
}
