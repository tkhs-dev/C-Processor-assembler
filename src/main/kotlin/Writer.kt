package dev.tkhs

interface Writer {
    fun write(data: ByteArray)
}

class MemoryImageWriter(val path: String) : Writer {
    override fun write(data: ByteArray) {
        data.mapIndexed { i, byte ->
            if (byte != 0.toByte()) {
                String.format("%04X %02X", i, byte.toUByte().toInt())
            } else {
                null
            }
        }.filterNotNull().let { lines ->
            java.io.File(path).writeText(lines.joinToString("\n"))
        }
    }
}