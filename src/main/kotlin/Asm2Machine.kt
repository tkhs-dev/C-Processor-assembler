package dev.tkhs

class Asm2Machine {
    companion object {
        fun convert(instr: List<AsmInstruction>): MachineProgram{
            val machineInstrs = instr.flatMap { map(it) }
            val program = MachineProgram(machineInstrs)
            check(program)
            return program
        }

        private fun check(program: MachineProgram) {
            program.program.forEach { (addr, instr) ->
                if (instr !is MachineInstruction.Data) {
                    val nextAddr = addr + instr.getSize()
                    program.program[nextAddr]?.let { nextInstr ->
                        if (nextInstr is MachineInstruction.Data) {
                            throw AssembleError("Instruction at address $addr overlaps with data at address $nextAddr")
                        }
                    }
                }
                // check unresolved labels
                if (instr is MachineInstruction.TwoWord && instr.value is Address.Label) {
                    val label = (instr.value as Address.Label).name
                    throw AssembleError("Unresolved label: $label")
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
                is AsmInstruction.Jmp -> listOf(MachineInstruction.JP(convAddress(instr.address)))
                is AsmInstruction.Jpc -> listOf(MachineInstruction.JPC(convAddress(instr.address)))
                is AsmInstruction.Jpz -> listOf(MachineInstruction.JPZ(convAddress(instr.address)))
                is AsmInstruction.Jps -> listOf(MachineInstruction.JPS(convAddress(instr.address)))
                is AsmInstruction.SetIxL -> listOf(MachineInstruction.SETIXL(Address.Addr(instr.value.toUShort())))
                is AsmInstruction.SetIxH -> listOf(MachineInstruction.SETIXH(Address.Addr(instr.value.toUShort())))
                is AsmInstruction.SetIx -> listOf(
                    MachineInstruction.SETIXL(convAddressLow(instr.address)),
                    MachineInstruction.SETIXH(convAddressHigh(instr.address))
                )
                is AsmInstruction.DC -> {
                    listOf(
                        MachineInstruction.Data(1, byteArrayOf(instr.value.toByte()))
                    )
                }
                is AsmInstruction.DS -> {
                    listOf(
                        MachineInstruction.Data(instr.size, ByteArray(instr.size))
                    )
                }
            }.let { mapped ->
                instr.label?.let { label ->
                    mapped.first().label = label
                }
                mapped
            }
        }
    }
}

private fun convAddress(addr: AsmInstruction.Address): Address {
    return when (addr) {
        is AsmInstruction.Address.Label -> Address.Label(addr.name)
        is AsmInstruction.Address.Addr -> Address.Addr(addr.addr)
    }
}

private fun convAddressLow(addr: AsmInstruction.Address): Address {
    return when (addr) {
        is AsmInstruction.Address.Label -> Address.LabelLow(addr.name)
        is AsmInstruction.Address.Addr -> Address.Addr((addr.addr.toInt() and 0xFF).toUShort())
    }
}

private fun convAddressHigh(addr: AsmInstruction.Address): Address {
    return when (addr) {
        is AsmInstruction.Address.Label -> Address.LabelHigh(addr.name)
        is AsmInstruction.Address.Addr -> Address.Addr(((addr.addr.toInt() shr 8) and 0xFF).toUShort())
    }
}