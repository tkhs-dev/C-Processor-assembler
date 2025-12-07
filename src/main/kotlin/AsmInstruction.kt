package dev.tkhs

import dev.tkhs.AsmInstruction.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

sealed class AsmInstruction {
    class Nop: AsmInstruction()

    enum class Reg {A, B}

    sealed class Address {
        data class Addr(val addr: UShort): Address()
        data class Label(val name: String): Address()
    }

    data class LoadI(val reg: Reg, val value: Int): AsmInstruction()
    data class LoadD(val reg: Reg): AsmInstruction()
    data class StoreD(val reg: Reg): AsmInstruction()
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

    class Cmp: AsmInstruction()

    data class Jmp(val address: Address): AsmInstruction()
    data class Jpc(val address: Address): AsmInstruction()
    data class Jpz(val address: Address): AsmInstruction()
    data class Jps(val address: Address): AsmInstruction()

    data class SetIxL(val value: Int): AsmInstruction()
    data class SetIxH(val value: Int): AsmInstruction()
    data class SetIx(val address: Address): AsmInstruction()

    data class DC(val value: Int): AsmInstruction()
    data class DS(val size: Int): AsmInstruction()

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
            "JPZ" to Jpz::class,
            "JPS" to Jps::class,
            "SETIXL" to SetIxL::class,
            "SETIXH" to SetIxH::class,
            "SETIX" to SetIx::class,
            "DC" to DC::class,
            "DS" to DS::class
        )

        fun parse(line: String): AsmInstruction? {
            val tokens = ArrayDeque(line.trim().split(Regex("\\s+")))
            if (tokens.isEmpty()) return null

            val label = if (tokens[0].endsWith(":")) {
                tokens.removeFirst().removeSuffix(":")
            } else null

            val instrToken = tokens.removeFirstOrNull() ?: return null
            val instrClass = instructions[instrToken.uppercase()] ?: throw AssertionError("Unknown label '$instrToken'")

            val params = mutableListOf<Any>()
            for (param in instrClass.primaryConstructor?.parameters.orEmpty()) {
                if(!tokens.isEmpty()){
                    val token = tokens.removeFirst()
                    when (param.type.jvmErasure) {
                        AsmInstruction.Reg::class -> {
                            params.add(parseReg(token))
                        }
                        AsmInstruction.Address::class -> {
                            val address = if (token.matches(Regex("^[0-9]+$"))) {
                                AsmInstruction.Address.Addr(token.toUShort())
                            } else if (token.matches(Regex("^0x[0-9a-fA-F]+$"))) {
                                AsmInstruction.Address.Addr(token.substring(2).toUShort(16))
                            } else {
                                AsmInstruction.Address.Label(token)
                            }
                            params.add(address)
                        }
                        Int::class -> {
                            val intValue = if (token.startsWith("0x")) {
                                token.substring(2).toIntOrNull(16)
                            } else {
                                token.toIntOrNull()
                            } ?: throw AssertionError("Invalid integer value '$token'")
                            params.add(intValue)
                        }
                        String::class -> {
                            params.add(token)
                        }
                        else -> throw AssertionError("Unsupported parameter type '${param.type}'")
                    }
                } else {
                    throw AssertionError("Insufficient parameters for instruction '$instrToken'")
                }
            }
            val instr =  instrClass.primaryConstructor?.call(*params.toTypedArray()) ?: instrClass.objectInstance
            return if (label != null) {
                instr!!.withLabel(label)
            } else {
                instr!!
            }
        }

        private fun parseReg(token: String): AsmInstruction.Reg {
            return when (token.uppercase()) {
                "A" -> AsmInstruction.Reg.A
                "B" -> AsmInstruction.Reg.B
                else -> throw AssertionError("Unknown register '$token'")
            }
        }

        private fun parseInt(token: String): Int {
            return token.toIntOrNull() ?: throw AssertionError("Invalid integer value '$token'")
        }
    }
}