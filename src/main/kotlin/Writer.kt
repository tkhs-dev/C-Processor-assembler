package dev.tkhs

interface Writer {
    fun write(data: MachineProgram)
}

class MemoryImageWriter(val path: String) : Writer {
    override fun write(data: MachineProgram) {
        data.toMemoryMap().let { memoryMap ->
            val lines = memoryMap.entries.sortedBy { it.key }.map { (addr, byte) ->
                String.format("%04X    %02X", addr, byte)
            }
            java.io.File(path).writeText(lines.joinToString("\n"))
        }
    }
}