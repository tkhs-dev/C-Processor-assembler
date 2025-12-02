package dev.tkhs

class Assembler {
    fun assemble(asmCode: String): MachineProgram {
        return asmCode.lines()
            .mapNotNull { AsmParser.parse(it) }
            .let { Asm2Machine.convert(it) }
    }
}