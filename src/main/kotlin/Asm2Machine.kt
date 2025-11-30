package dev.tkhs

class Asm2Machine {
    companion object {
        fun convert(instr: List<AsmInstruction>): List<MachineInstruction>{
            val instrs = instr.flatMap { map(it) }
            resolveLabels(instrs)
            check(instrs)
            return instrs
        }

        private fun check(instrs: List<MachineInstruction>) {
            instrs.forEach { instr ->
                if (instr is MachineInstruction.TwoWord && instr.value is Address.Label) {
                    val label = (instr.value as Address.Label).name
                    throw AssembleError("Unresolved label: $label")
                }
            }
        }

        private fun resolveLabels(instrs: List<MachineInstruction>) {
            val labelMap = mutableMapOf<String, Int>()
            var pc = 0
            instrs.forEach { instr ->
                println("$pc: $instr, ${instr.label}")
                instr.label?.let { label ->
                    if (labelMap.containsKey(label)) {
                        throw AssembleError("Duplicate label: $label")
                    }
                    labelMap[label] = pc
                }
                pc += instr.getSize()
            }

            instrs.forEach { instr ->
                if (instr is MachineInstruction.TwoWord && instr.value is Address.Label) {
                    val label = (instr.value as Address.Label).name
                    val addr = labelMap[label]
                        ?: throw AssembleError("Undefined label: $label")
                    instr.value = Address.Addr(addr.toShort())
                }
            }
        }

        private fun map(instr: AsmInstruction): List<MachineInstruction> {
            return when (instr) {
                is AsmInstruction.Nop -> listOf(MachineInstruction.NOP())
                is AsmInstruction.LoadI -> when (instr.reg) {
                    AsmInstruction.Reg.A -> listOf(MachineInstruction.LDIA(instr.value.toByte()))
                    AsmInstruction.Reg.B -> listOf(MachineInstruction.LDIB(instr.value.toByte()))
                }

                is AsmInstruction.LoadD -> when (instr.reg) {
                    AsmInstruction.Reg.A -> listOf(MachineInstruction.LDDA())
                    AsmInstruction.Reg.B -> listOf(MachineInstruction.LDDB())
                }

                is AsmInstruction.StoreD -> when (instr.reg) {
                    AsmInstruction.Reg.A -> listOf(MachineInstruction.STDA())
                    AsmInstruction.Reg.B -> listOf(MachineInstruction.STDB())
                }

                is AsmInstruction.StoreI -> listOf(MachineInstruction.STDI(instr.value.toByte()))
                is AsmInstruction.Op -> when (instr) {
                    is AsmInstruction.Op.Add -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.ADDA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.ADDB())
                    }

                    is AsmInstruction.Op.Sub -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.SUBA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.SUBB())
                    }

                    is AsmInstruction.Op.Inc -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.INCA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.INCB())
                    }

                    is AsmInstruction.Op.Dec -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.DECA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.DECB())
                    }

                    is AsmInstruction.Op.Not -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.NOTA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.NOTB())
                    }

                    is AsmInstruction.Op.And -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.ANDA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.ANDB())
                    }

                    is AsmInstruction.Op.Or -> when (instr.dest) {
                        AsmInstruction.Reg.A -> listOf(MachineInstruction.ORA())
                        AsmInstruction.Reg.B -> listOf(MachineInstruction.ORB())
                    }
                }

                is AsmInstruction.Cmp -> listOf(MachineInstruction.CMP())
                is AsmInstruction.Jmp -> listOf(MachineInstruction.JP(Address.Label(instr.address)))
                is AsmInstruction.Jpc -> listOf(MachineInstruction.JPC(Address.Label(instr.address)))
                is AsmInstruction.Jpn -> listOf(MachineInstruction.JPN(Address.Label(instr.address)))
                is AsmInstruction.SetIxL -> listOf(MachineInstruction.SETIXL(instr.value.toByte()))
                is AsmInstruction.SetIxH -> listOf(MachineInstruction.SETIXH(instr.value.toByte()))
                is AsmInstruction.SetIx -> listOf(
                    MachineInstruction.SETIXL((instr.value and 0xFF).toByte()),
                    MachineInstruction.SETIXH(((instr.value shr 8) and 0xFF).toByte())
                )
            }.let { mapped ->
                instr.label?.let { label ->
                    mapped.first().label = label
                }
                mapped
            }
        }
    }
}