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

    // Conceptual base addresses for WORLD and INVENTORY spaces
    // Use standard data-segment-ish addresses so MARS won't complain.
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

        // ADD rd, rs, rt   (opcode=0x00, funct=0x20)
        instructionList.add(
            new BasicInstruction(
                "ADD $t1,$t2,$t3",
                "ADD: $t1 = $t2 + $t3 (signed)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100000",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);
                        int sum = a + b;
                        RegisterFile.updateRegister(rd, sum);
                    }
                }
        ));

        // SUB rd, rs, rt   (funct=0x22)
        instructionList.add(
            new BasicInstruction(
                "SUB $t1,$t2,$t3",
                "SUB: $t1 = $t2 - $t3 (signed)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100010",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);
                        int diff = a - b;
                        RegisterFile.updateRegister(rd, diff);
                    }
                }
        ));

        // AND rd, rs, rt   (funct=0x24)
        instructionList.add(
            new BasicInstruction(
                "AND $t1,$t2,$t3",
                "AND: $t1 = $t2 & $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100100",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int r = RegisterFile.getValue(rs) & RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // OR rd, rs, rt    (funct=0x25)
        instructionList.add(
            new BasicInstruction(
                "OR $t1,$t2,$t3",
                "OR: $t1 = $t2 | $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100101",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int r = RegisterFile.getValue(rs) | RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // XOR rd, rs, rt   (funct=0x26)
        instructionList.add(
            new BasicInstruction(
                "XOR $t1,$t2,$t3",
                "XOR: $t1 = $t2 ^ $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100110",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int r = RegisterFile.getValue(rs) ^ RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // SLT rd, rs, rt   (funct=0x2A)
        instructionList.add(
            new BasicInstruction(
                "SLT $t1,$t2,$t3",
                "SLT: $t1 = 1 if $t2 < $t3 else 0",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, (a < b) ? 1 : 0);
                    }
                }
        ));

        // ADDI rt, rs, imm   (opcode=0x08)
        instructionList.add(
            new BasicInstruction(
                "ADDI $t1,$t2,-100",    // literal imm placeholder
                "ADDI: $t1 = $t2 + signext(imm)",
                BasicInstructionFormat.I_FORMAT,
                "001000 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2]; // already sign-extended by MARS
                        int r = RegisterFile.getValue(rs) + imm;
                        RegisterFile.updateRegister(rt, r);
                    }
                }
        ));

        // ANDI rt, rs, imm   (opcode=0x0C)
        instructionList.add(
            new BasicInstruction(
                "ANDI $t1,$t2,0xFFFF",
                "ANDI: $t1 = $t2 & zeroext(imm)",
                BasicInstructionFormat.I_FORMAT,
                "001100 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2] & 0xFFFF;
                        int r = RegisterFile.getValue(rs) & imm;
                        RegisterFile.updateRegister(rt, r);
                    }
                }
        ));

        // ORI rt, rs, imm    (opcode=0x0D)
        instructionList.add(
            new BasicInstruction(
                "ORI $t1,$t2,0xFFFF",
                "ORI: $t1 = $t2 | zeroext(imm)",
                BasicInstructionFormat.I_FORMAT,
                "001101 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2] & 0xFFFF;
                        int r = RegisterFile.getValue(rs) | imm;
                        RegisterFile.updateRegister(rt, r);
                    }
                }
        ));

        // LW rt, offset(rs)   (opcode=0x23)
        instructionList.add(
            new BasicInstruction(
                "LW $t1,0($t2)",   // template uses numeric 0
                "LW: $t1 = MEM[$t2 + offset]",
                BasicInstructionFormat.I_FORMAT,
                "100011 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];
                        int addr = RegisterFile.getValue(rs) + imm;
                        try {
                            int v = Memory.getInstance().getWord(addr);
                            RegisterFile.updateRegister(rt, v);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // SW rt, offset(rs)   (opcode=0x2B)
        instructionList.add(
            new BasicInstruction(
                "SW $t1,0($t2)",   // template uses numeric 0
                "SW: MEM[$t2 + offset] = $t1",
                BasicInstructionFormat.I_FORMAT,
                "101011 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];
                        int addr = RegisterFile.getValue(rs) + imm;
                        int val  = RegisterFile.getValue(rt);
                        try {
                            Memory.getInstance().setWord(addr, val);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // BEQ rs, rt, label   (opcode=0x04)
        instructionList.add(
            new BasicInstruction(
                "BEQ $t1,$t2,label",
                "BEQ: branch if $t1 == $t2",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "000100 fffff sssss tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rs = op[0];
                        int rt = op[1];
                        int offset = op[2];
                        if (RegisterFile.getValue(rs) == RegisterFile.getValue(rt)) {
                            Globals.instructionSet.processBranch(offset);
                        }
                    }
                }
        ));

        // BNE rs, rt, label   (opcode=0x05)
        instructionList.add(
            new BasicInstruction(
                "BNE $t1,$t2,label",
                "BNE: branch if $t1 != $t2",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "000101 fffff sssss tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rs = op[0];
                        int rt = op[1];
                        int offset = op[2];
                        if (RegisterFile.getValue(rs) != RegisterFile.getValue(rt)) {
                            Globals.instructionSet.processBranch(offset);
                        }
                    }
                }
        ));

        // J target   (opcode=0x02)
        instructionList.add(
            new BasicInstruction(
                "J target",
                "J: unconditional jump to target address",
                BasicInstructionFormat.J_FORMAT,
                "000010 ffffffffffffffffffffffffff",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int target = op[0];
                        int nextPC = (RegisterFile.getProgramCounter() & 0xF0000000)
                                   | (target << 2);
                        Globals.instructionSet.processJump(nextPC);
                    }
                }
        ));

        /* ============================================================
         *  UNIQUE M-CRAFT INSTRUCTIONS
         * ============================================================
         *
         * MINE, PLACE, CRAFT, BREW, SMELT,
         * HOTBAR, INVLD, INVST, CHAT, SPRINT, IFMOB
         */

        // MINE rt, offset(rs)   (opcode=0x30)
        instructionList.add(
            new BasicInstruction(
                "MINE $t1,0($t2)",   // numeric literal in template
                "MINE: $t1 = WORLD[$t2 + offset]",
                BasicInstructionFormat.I_FORMAT,
                "110000 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr  = WORLD_BASE + (index * 4);

                        try {
                            int v = Memory.getInstance().getWord(addr);
                            RegisterFile.updateRegister(rt, v);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // PLACE rt, offset(rs)   (opcode=0x31)
        instructionList.add(
            new BasicInstruction(
                "PLACE $t1,0($t2)",  // numeric literal in template
                "PLACE: WORLD[$t2 + offset] = $t1",
                BasicInstructionFormat.I_FORMAT,
                "110001 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr  = WORLD_BASE + (index * 4);
                        int val   = RegisterFile.getValue(rt);

                        try {
                            Memory.getInstance().setWord(addr, val);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // CRAFT rd, rs, rt   (funct=0x30)
        instructionList.add(
            new BasicInstruction(
                "CRAFT $t1,$t2,$t3",
                "CRAFT: pack low 16 bits of $t2,$t3 into $t1",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 110000",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0];
                        int rs = op[1];
                        int rt = op[2];

                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);

                        int packed = ((a & 0xFFFF) << 16) | (b & 0xFFFF);
                        RegisterFile.updateRegister(rd, packed);
                    }
                }
        ));

        // BREW rd, rs, rt   (funct=0x31)
        instructionList.add(
            new BasicInstruction(
                "BREW $t1,$t2,$t3",
                "BREW: $t1 = ($t2 XOR ($t3 << 3)) + ($t3 >> 2)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 110001",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0];
                        int rs = op[1];
                        int rt = op[2];

                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);

                        int brewed = (a ^ (b << 3)) + (b >>> 2);
                        RegisterFile.updateRegister(rd, brewed);
                    }
                }
        ));

        // SMELT rd, rs   (funct=0x32)
        instructionList.add(
            new BasicInstruction(
                "SMELT $t1,$t2",
                "SMELT: clamp $t2 into [0,255] and store in $t1",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss 00000 fffff 00000 110010",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0];
                        int rs = op[1];

                        int v = RegisterFile.getValue(rs);
                        int r;
                        if (v < 0) r = 0;
                        else if (v > 255) r = 255;
                        else r = v;

                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // HOTBAR imm   (opcode=0x35)
        instructionList.add(
            new BasicInstruction(
                "HOTBAR -100",
                "HOTBAR: select hotbar index (0–7), stored in $k0",
                BasicInstructionFormat.I_FORMAT,
                "110101 00000 00000 ffffffffffffffff",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int imm = s.getOperands()[0];
                        int hb = imm & 0x7;
                        RegisterFile.updateRegister(26, hb); // HB stored in $k0
                    }
                }
        ));

        // INVLD rt, slot(rs)   (opcode=0x36)
        instructionList.add(
            new BasicInstruction(
                "INVLD $t1,0($t2)",
                "INVLD: load INVENTORY[$t2 + slot] into $t1",
                BasicInstructionFormat.I_FORMAT,
                "110110 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr  = INVENTORY_BASE + (index * 4);

                        try {
                            int v = Memory.getInstance().getWord(addr);
                            RegisterFile.updateRegister(rt, v);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // INVST rt, slot(rs)   (opcode=0x37)
        instructionList.add(
            new BasicInstruction(
                "INVST $t1,0($t2)",
                "INVST: store $t1 into INVENTORY[$t2 + slot]",
                BasicInstructionFormat.I_FORMAT,
                "110111 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr  = INVENTORY_BASE + (index * 4);
                        int v     = RegisterFile.getValue(rt);

                        try {
                            Memory.getInstance().setWord(addr, v);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // CHAT rt   (opcode=0x38)
        instructionList.add(
            new BasicInstruction(
                "CHAT $t1",
                "CHAT: syscall-like print of $t1 (sets $a0,$v0)",
                BasicInstructionFormat.I_FORMAT,
                "111000 fffff 00000 0000000000000000",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int rs = s.getOperands()[0];
                        int v  = RegisterFile.getValue(rs);
                        // Prepare standard MIPS print_int syscall
                        RegisterFile.updateRegister(4, v);   // $a0
                        RegisterFile.updateRegister(2, 1);   // $v0 = 1
                    }
                }
        ));

        // SPRINT rs, label   (opcode=0x3A)
        instructionList.add(
            new BasicInstruction(
                "SPRINT $t1,label",
                "SPRINT: decrement $t1, branch to label until zero",
                BasicInstructionFormat.I_BRANCH_FORMAT,
                "111010 fffff 00000 ssssssssssssssss",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rs     = op[0];
                        int offset = op[2];

                        int val = RegisterFile.getValue(rs) - 1;
                        RegisterFile.updateRegister(rs, val);

                        if (val != 0) {
                            Globals.instructionSet.processBranch(offset);
                        }
                    }
                }
        ));


        // IFMOB rt, offset(rs)   (opcode=0x3B)
        instructionList.add(
            new BasicInstruction(
                "IFMOB $t1,0($t2)",
                "IFMOB: $t1 = 1 if WORLD[$t2 + offset] < 0 else 0",
                BasicInstructionFormat.I_FORMAT,
                "111011 sssss fffff tttttttttttttttt",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt  = op[0];
                        int rs  = op[1];
                        int imm = op[2];

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr  = WORLD_BASE + (index * 4);

                        try {
                            int val = Memory.getInstance().getWord(addr);
                            RegisterFile.updateRegister(rt, (val < 0) ? 1 : 0);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));
    }
}
