package dev.tkhs

sealed class MachineInstruction(val opcode: Int) {
    sealed class ZeroWord(opCode: Int) : MachineInstruction(opCode) {
        data object LDDA : ZeroWord(0xE0)
        data object LDDB : ZeroWord(0xE1)
        data object STDA : ZeroWord(0xF0)
        data object STDB : ZeroWord(0xF4)
        data object ADDA : ZeroWord(0x80)
        data object SUBA : ZeroWord(0x81)
        data object ANDA : ZeroWord(0x82)
        data object ORA : ZeroWord(0x83)
        data object NOTA : ZeroWord(0x84)
        data object INCA : ZeroWord(0x85)
        data object DECA : ZeroWord(0x86)
        data object ADDB : ZeroWord(0x90)
        data object SUBB : ZeroWord(0x91)
        data object ANDB : ZeroWord(0x92)
        data object ORB : ZeroWord(0x93)
        data object NOTB : ZeroWord(0x98)
        data object INCB : ZeroWord(0x99)
        data object DECB : ZeroWord(0x9a)
        data object CMP : ZeroWord(0xA1)
        data object NOP : ZeroWord(0x00)

        override fun toBytes(): List<Byte> {
            return listOf(opcode.toByte())
        }
    }
    sealed class OneWord(opCode: Int, val value: Byte) : MachineInstruction(opCode) {
        class SETIXH(value: Byte) : OneWord(0xD0, value)
        class SETIXL(value: Byte) : OneWord(0xD1, value)
        class LDIA(value: Byte) : OneWord(0xD8, value)
        class LDIB(value: Byte) : OneWord(0xD9, value)
        class STDI(value: Byte) : OneWord(0xF8, value)

        override fun toBytes(): List<Byte> {
            return listOf(opcode.toByte(), value)
        }
    }
    sealed class TwoWord(opCode: Int, val value: Address) : MachineInstruction(opCode) {
        class JP(addr: Address) : TwoWord(0x60, addr)
        class JPC(addr: Address) : TwoWord(0x40, addr)
        class JPN(addr: Address) : TwoWord(0x50, addr)

        override fun toBytes(): List<Byte> {
            if (value is Address.Addr) {
                val addr = value.addr.toInt()
                return listOf(
                    opcode.toByte(),
                    (addr shr 8).toByte(),
                    (addr and 0xFF).toByte()
                )
            } else {
                throw IllegalStateException("Address.Label cannot be converted to bytes directly")
            }
        }
    }

    abstract fun toBytes(): List<Byte>
}

sealed class Address{
    data class Label(val name: String): Address()
    data class Addr(val addr: Short): Address()
}