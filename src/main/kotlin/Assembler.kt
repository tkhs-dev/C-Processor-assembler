package dev.tkhs

class Assembler {
    fun assemble(asmCode: String): MachineProgram {
        return asmCode.lines()
            .filter { it.isNotBlank() && !it.trimStart().startsWith(";") }
            .mapNotNull { AsmParser.parse(it) }
            .let { Asm2Machine.convert(it) }
    }
}