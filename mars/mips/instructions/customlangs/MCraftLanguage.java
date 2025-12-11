package mars.mips.instructions.customlangs;

import mars.simulator.*;
import mars.mips.hardware.*;
import mars.*;
import mars.util.*;
import mars.mips.instructions.*;

public class MCraftLanguage extends CustomAssembly {

    private static final int WORLD_BASE = 0x20000000;

    @Override
    public String getName() {
        return "M-CRAFT Language";
    }

    @Override
    public String getDescription() {
        return "Minecraft-inspired ISA with MC-prefixed arithmetic and WORLD operations.";
    }

    @Override
    protected void populate() {

        // mcadd
        instructionList.add(
            new BasicInstruction(
                "mcadd $t1,$t2,$t3",
                "Add: $t1 = $t2 + $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100000",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int sum = RegisterFile.getValue(rs) + RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, sum);
                    }
                }
        ));

        // mcsub
        instructionList.add(
            new BasicInstruction(
                "mcsub $t1,$t2,$t3",
                "Subtract: $t1 = $t2 - $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100010",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int diff = RegisterFile.getValue(rs) - RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, diff);
                    }
                }
        ));

        // mcand
        instructionList.add(
            new BasicInstruction(
                "mcand $t1,$t2,$t3",
                "Bitwise AND: $t1 = $t2 & $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100100",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int r = RegisterFile.getValue(rs) & RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // mcor
        instructionList.add(
            new BasicInstruction(
                "mcor $t1,$t2,$t3",
                "Bitwise OR: $t1 = $t2 | $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100101",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int r = RegisterFile.getValue(rs) | RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // mcxor
        instructionList.add(
            new BasicInstruction(
                "mcxor $t1,$t2,$t3",
                "Bitwise XOR: $t1 = $t2 ^ $t3",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 100110",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int r = RegisterFile.getValue(rs) ^ RegisterFile.getValue(rt);
                        RegisterFile.updateRegister(rd, r);
                    }
                }
        ));

        // mcslt
        instructionList.add(
            new BasicInstruction(
                "mcslt $t1,$t2,$t3",
                "Set if less: $t1 = ($t2 < $t3 ? 1 : 0)",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 101010",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];
                        int result = (RegisterFile.getValue(rs) < RegisterFile.getValue(rt)) ? 1 : 0;
                        RegisterFile.updateRegister(rd, result);
                    }
                }
        ));

        // mcaddi
        instructionList.add(
            new BasicInstruction(
                "mcaddi $t1,$t2,imm",
                "Add immediate: $t1 = $t2 + imm",
                BasicInstructionFormat.I_FORMAT,
                "001000 sssss fffff tttttttttttttttt",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt = op[0], rs = op[1], imm = op[2];
                        int sum = RegisterFile.getValue(rs) + imm;
                        RegisterFile.updateRegister(rt, sum);
                    }
                }
        ));

        // mclw
        instructionList.add(
            new BasicInstruction(
                "mclw $t1,offset($t2)",
                "Load word: $t1 = MEM[$t2 + offset]",
                BasicInstructionFormat.I_FORMAT,
                "100011 sssss fffff tttttttttttttttt",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt = op[0], rs = op[1], imm = op[2];
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

        // mcsw
        instructionList.add(
            new BasicInstruction(
                "mcsw $t1,offset($t2)",
                "Store word: MEM[$t2 + offset] = $t1",
                BasicInstructionFormat.I_FORMAT,
                "101011 sssss fffff tttttttttttttttt",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt = op[0], rs = op[1], imm = op[2];
                        int addr = RegisterFile.getValue(rs) + imm;
                        int val = RegisterFile.getValue(rt);

                        try {
                            Memory.getInstance().setWord(addr, val);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // mcmov
        instructionList.add(
            new BasicInstruction(
                "mcmov $t1,$t2",
                "Move: $t1 = $t2",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss 00000 fffff 00000 100111",
                new SimulationCode() {
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        RegisterFile.updateRegister(op[0], RegisterFile.getValue(op[1]));
                    }
                }
        ));

        // MINE
        instructionList.add(
            new BasicInstruction(
                "MINE $t1,offset($t2)",
                "Mine block: $t1 = WORLD[$t2 + offset]",
                BasicInstructionFormat.I_FORMAT,
                "110000 sssss fffff tttttttttttttttt",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt = op[0], rs = op[1], imm = op[2];

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr = WORLD_BASE + (index * 4);

                        try {
                            int v = Memory.getInstance().getWord(addr);
                            RegisterFile.updateRegister(rt, v);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // PLACE
        instructionList.add(
            new BasicInstruction(
                "PLACE $t1,offset($t2)",
                "Place block: WORLD[$t2 + offset] = $t1",
                BasicInstructionFormat.I_FORMAT,
                "110001 sssss fffff tttttttttttttttt",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rt = op[0], rs = op[1], imm = op[2];
                        int val = RegisterFile.getValue(rt);

                        int index = RegisterFile.getValue(rs) + imm;
                        int addr = WORLD_BASE + (index * 4);

                        try {
                            Memory.getInstance().setWord(addr, val);
                        } catch (AddressErrorException e) {
                            throw new ProcessingException(s, e);
                        }
                    }
                }
        ));

        // CRAFT
        instructionList.add(
            new BasicInstruction(
                "CRAFT $t1,$t2,$t3",
                "Craft: pack 16 bits from $t2 and $t3 into $t1",
                BasicInstructionFormat.R_FORMAT,
                "000000 sssss ttttt fffff 00000 110000",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rd = op[0], rs = op[1], rt = op[2];

                        int a = RegisterFile.getValue(rs);
                        int b = RegisterFile.getValue(rt);

                        int packed = ((a & 0xFFFF) << 16) | (b & 0xFFFF);
                        RegisterFile.updateRegister(rd, packed);
                    }
                }
        ));

        // HOTBAR
        instructionList.add(
            new BasicInstruction(
                "HOTBAR imm",
                "Select hotbar (0–7), stored in $k0",
                BasicInstructionFormat.I_FORMAT,
                "110101 00000 00000 ffffffffffffffff",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int imm = s.getOperands()[0];
                        RegisterFile.updateRegister(26, imm & 0x7);  // $k0
                    }
                }
        ));

        // CHAT
        instructionList.add(
            new BasicInstruction(
                "CHAT $t1",
                "Prepare syscall print_int using $t1",
                BasicInstructionFormat.R_FORMAT,
                "000000 fffff 00000 00000 00000 111000",
                new SimulationCode(){
                    public void simulate(ProgramStatement s) throws ProcessingException {
                        int[] op = s.getOperands();
                        int rs = op[0];
                        RegisterFile.updateRegister(4, RegisterFile.getValue(rs)); // $a0
                        RegisterFile.updateRegister(2, 1); // $v0 = 1
                    }
                }
        ));
    }
}
