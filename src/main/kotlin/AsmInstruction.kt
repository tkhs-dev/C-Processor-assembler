package dev.tkhs

import dev.tkhs.AsmInstruction.*
import kotlin.reflect.KClass

sealed class AsmInstruction {
    data object Nop: AsmInstruction()

    enum class Reg {A, B}

    data class LoadI(val reg: Reg, val value: Int): AsmInstruction()
    data class LoadD(val reg: Reg, val addr: Int): AsmInstruction()
    data object StoreD: AsmInstruction()
    data class StoreI(val value:Int): AsmInstruction()

    sealed class Op(val dest: Reg): AsmInstruction() {
        class Add(dest: Reg): Op(dest)
        class Sub(dest: Reg): Op(dest)
        class Inc(dest: Reg): Op(dest)
        class Dec(dest: Reg): Op(dest)
        class Not(dest: Reg): Op(dest)
        class And(dest: Reg): Op(dest)
        class Or(dest: Reg): Op(dest)
    }

    object Cmp: AsmInstruction()

    data class Jmp(val address: String): AsmInstruction()
    data class Jpc(val address: String): AsmInstruction()
    data class Jpn(val address: String): AsmInstruction()

    data class SetIxL(val value: Int): AsmInstruction()
    data class SetIxH(val value: Int): AsmInstruction()
    data class SetIx(val value: Int): AsmInstruction()

    var label: String? = null

    fun withLabel(label: String): AsmInstruction {
        this.label = label
        return this
    }
}

class AsmParser {
    companion object {
        val instructions = mapOf<String, KClass<out AsmInstruction>>(
            "NOP" to Nop::class,
            "LDI" to LoadI::class,
            "LDD" to LoadD::class,
            "STD" to  StoreD::class,
            "STI" to StoreI::class,
            "ADD" to Op.Add::class,
            "SUB" to Op.Sub::class,
            "INC" to Op.Inc::class,
            "DEC" to Op.Dec::class,
            "NOT" to Op.Not::class,
            "AND" to Op.And::class,
            "OR" to Op.Or::class,
            "CMP" to Cmp::class,
            "JMP" to Jmp::class,
            "JPC" to Jpc::class,
            "JPN" to Jpn::class,
            "SETIXL" to SetIxL::class,
            "SETIXH" to SetIxH::class,
            "SETIX" to SetIx::class,
        )

        fun parse(line: String): AsmInstruction? {
            val tokens = ArrayDeque(line.trim().split(Regex("\\s+")))
            if (tokens.isEmpty()) return null

            val label = if (tokens[0].endsWith(":")) {
                tokens.removeFirst().removeSuffix(":")
            } else null

            val instrToken = tokens.removeFirstOrNull() ?: return null
            val instrClass = instructions[instrToken.uppercase()] ?: return null
        }
    }
}