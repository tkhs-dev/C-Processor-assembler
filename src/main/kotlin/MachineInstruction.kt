package dev.tkhs

sealed class MachineInstruction(val opcode: Int) {
    var label: String? = null
    sealed class ZeroWord(opCode: Int) : MachineInstruction(opCode) {
        override fun toBytes(): List<Byte> {
            return listOf(opcode.toByte())
        }

        override fun getSize(): Int {
            return 1
        }
    }

    sealed class OneWord(opCode: Int, val value: Byte) : MachineInstruction(opCode) {
        override fun toBytes(): List<Byte> {
            return listOf(opcode.toByte(), value)
        }

        override fun getSize(): Int {
            return 2
        }
    }

    sealed class AddressOneWord(opCode: Int, var value: Address) : MachineInstruction(opCode) {
        override fun toBytes(): List<Byte> {
            if (value is Address.Addr) {
                val addr = (value as Address.Addr).addr.toInt()
                return listOf(
                    opcode.toByte(),
                    (addr and 0xFF).toByte()
                )
            } else {
                throw IllegalStateException("Address.Label cannot be converted to bytes directly")
            }
        }

        override fun getSize(): Int {
            return 2
        }
    }

    sealed class TwoWord(opCode: Int, var value: Address) : MachineInstruction(opCode) {
        override fun toBytes(): List<Byte> {
            if (value is Address.Addr) {
                val addr = (value as Address.Addr).addr.toInt()
                return listOf(
                    opcode.toByte(),
                    (addr shr 8).toByte(),
                    (addr and 0xFF).toByte()
                )
            } else {
                throw IllegalStateException("Address.Label cannot be converted to bytes directly")
            }
        }

        override fun getSize(): Int {
            return 3
        }
    }

    class Data(val words: Int, val data: ByteArray) : MachineInstruction(0) {
        override fun toBytes(): List<Byte> {
            return data.toList()
        }

        override fun getSize(): Int {
            return words
        }
    }

    class LDDA : ZeroWord(0xE0)
    class LDDB : ZeroWord(0xE1)
    class STDA : ZeroWord(0xF0)
    class STDB : ZeroWord(0xF4)
    class ADDA : ZeroWord(0x80)
    class SUBA : ZeroWord(0x81)
    class MULA : ZeroWord(0x76)
    class ANDA : ZeroWord(0x82)
    class ORA : ZeroWord(0x83)
    class NOTA : ZeroWord(0x84)
    class INCA : ZeroWord(0x85)
    class DECA : ZeroWord(0x86)
    class ADDB : ZeroWord(0x90)
    class SUBB : ZeroWord(0x91)
    class MULB : ZeroWord(0x77)
    class ANDB : ZeroWord(0x92)
    class ORB : ZeroWord(0x93)
    class NOTB : ZeroWord(0x98)
    class INCB : ZeroWord(0x99)
    class DECB : ZeroWord(0x9a)
    class CMP : ZeroWord(0xA1)
    class MOVAB : ZeroWord(0x74)
    class MOVBA : ZeroWord(0x75)
    class NOP : ZeroWord(0x00)

    class SETIXH(value: Address) : AddressOneWord(0xD0, value)
    class SETIXL(value: Address) : AddressOneWord(0xD1, value)
    class LDIA(value: Byte) : OneWord(0xD8, value)
    class LDIB(value: Byte) : OneWord(0xD9, value)
    class STDI(value: Byte) : OneWord(0xF8, value)
    class SLL(value: Byte) : OneWord(0x70, value)
    class SRL(value: Byte) : OneWord(0x71, value)
    class SLA(value: Byte) : OneWord(0x72, value)
    class SRA(value: Byte) : OneWord(0x73, value)

    class JP(addr: Address) : TwoWord(0x60, addr)
    class JPC(addr: Address) : TwoWord(0x40, addr)
    class JPZ(addr: Address) : TwoWord(0x50, addr)

    abstract fun toBytes(): List<Byte>
    abstract fun getSize(): Int

    fun withLabel(label: String): MachineInstruction {
        this.label = label
        return this
    }
}

sealed class Address{
    data class Label(val name: String): Address()
    data class Addr(val addr: UShort): Address()
    data class LabelLow(val name: String): Address()
    data class LabelHigh(val name: String): Address()
}