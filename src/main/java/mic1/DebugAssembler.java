package mic1;

import mic1.assembler.Assembler;
import mic1.core.MicroInstruction;

public class DebugAssembler {
    public static void main(String[] args) {
        Assembler assembler = new Assembler();

        String code = """
START:
    B=0, C=AC, PASSA, ENC
    B=AC, A=+1, C=AC, ADD, ENC
    GOTO START
""";

        Assembler.AssemblyResult result = assembler.assemble(code);

        System.out.println("Assembly Result:");
        System.out.println(result.getCompiledOutput());
        System.out.println("\nDetailed Instructions:");

        MicroInstruction[] instructions = result.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            MicroInstruction mi = instructions[i];
            System.out.println(String.format(
                "Instr %d: addr=%d, A=%d, B=%d, C=%d, ALU=%s, COND=%s, ENC=%s",
                i, mi.getAddr(), mi.getRegA(), mi.getRegB(), mi.getRegC(),
                mi.getAluOp(), mi.getCond(), mi.isEnc()
            ));
        }
    }
}
