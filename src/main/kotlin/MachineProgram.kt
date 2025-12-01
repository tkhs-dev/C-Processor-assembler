package dev.tkhs

class MachineProgram(private val instructions: List<MachineInstruction>) {
    lateinit var program: Map<Int, MachineInstruction>

    init {
        resolveLabels()
    }

    private fun calculateAddresses() {
        var mainPc = 0
        var dataPc = instructions.sumOf { it.getSize() }
        val addressMap = mutableMapOf<Int, MachineInstruction>()
        instructions.forEach { instr ->
            val pc = when (instr) {
                is MachineInstruction.DATA -> {
                    val addr = dataPc
                    dataPc += instr.getSize()
                    addr
                }

                else -> {
                    val addr = mainPc
                    mainPc += instr.getSize()
                    addr
                }
            }
            addressMap[pc] = instr
        }
        program = addressMap
    }

    private fun resolveLabels() {
        calculateAddresses()
        val labelMap = mutableMapOf<String, Int>()
        program.forEach { (addr, instr) ->
            instr.label?.let { label ->
                if (labelMap.containsKey(label)) {
                    throw AssembleError("Duplicate label: $label")
                }
                labelMap[label] = addr
            }
        }
        program.values.forEach { instr ->
            if (instr is MachineInstruction.TwoWord) {
                if (instr.value is Address.Label) {
                    val label = (instr.value as Address.Label).name
                    val address = labelMap[label]
                        ?: throw AssembleError("Unresolved label: $label")
                    instr.value = Address.Addr(address.toShort())
                }
            }
        }
    }

    fun toByteArray(): ByteArray {
        val size = program.keys.maxOrNull()?.plus(1) ?: 0
        val byteArray = ByteArray(size)
        program.forEach { (addr, instr) ->
            val bytes = instr.toBytes()
            bytes.forEachIndexed { index, byte ->
                byteArray[addr + index] = byte
            }
        }
        return byteArray
    }
}