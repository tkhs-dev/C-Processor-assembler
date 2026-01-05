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

class MifWriter(val path: String) : Writer {
    override fun write(data: MachineProgram) {
        data.toMemoryMap().let { memoryMap ->
            val depth = 1024
            val width = 8
            val header = """
                WIDTH=$width;
                DEPTH=$depth;
                
                ADDRESS_RADIX=HEX;
                DATA_RADIX=HEX;
                
                CONTENT BEGIN
            """.trimIndent()
            val footer = "END;"
            // XXX : XX;
            // unspecified addresses -> [XXX..XXX] : 00;
            val bodyLines = mutableListOf<String>()
            var currentAddr = 0
            val sortedEntries = memoryMap.entries.sortedBy { it.key }
            for ((addr, byte) in sortedEntries) {
                if (addr > currentAddr) {
                    if (addr - currentAddr == 1) {
                        // single address
                        bodyLines.add(String.format("%03X : 00;", currentAddr))
                    } else {
                        // range
                        bodyLines.add(String.format("[%03X..%03X] : 00;", currentAddr, addr - 1))
                    }
                    currentAddr = addr
                }
                bodyLines.add(String.format("%03X : %02X;", addr, byte))
                currentAddr = addr + 1
            }
            if (currentAddr < depth) {
                bodyLines.add(String.format("[%03X..%03X] : 00;", currentAddr, depth - 1))
            }
            val content = listOf(header) + bodyLines + listOf(footer)
            java.io.File(path).writeText(content.joinToString("\n"))
        }
    }
}