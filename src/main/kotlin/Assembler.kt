package dev.tkhs

class Assembler {
    fun assemble(asmCode: String): ByteArray {
        return asmCode.lines()
            .mapNotNull { AsmParser.parse(it) }
            .let { Asm2Machine.convert(it) }
            .toByteArray()
    }
}