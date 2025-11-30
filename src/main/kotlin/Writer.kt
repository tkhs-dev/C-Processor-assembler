package dev.tkhs

interface Writer {
    fun write(data: ByteArray)
}

class MemoryImageWriter(val path: String) : Writer {
    override fun write(data: ByteArray) {
        data.mapIndexed { i, byte ->
            String.format("%04d %02X", i, byte.toUByte().toInt())
        }.let { lines ->
            java.io.File(path).writeText(lines.joinToString("\n"))
        }
    }
}