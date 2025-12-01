package dev.tkhs

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

class CPAssembler : CliktCommand() {
    val input: String by option().default("input.asm")
    val output: String by option().default("output.txt")

    override fun run() {
        val asmCode = try {
            java.io.File(input).readText()
        } catch (e: Exception) {
            println("Error reading input file: ${e.message}")
            return
        }
        try {
            val assembler = Assembler()
            val machineCode = assembler.assemble(asmCode)
            val writer = MemoryImageWriter(output)
            writer.write(machineCode)
        } catch (e: AssembleError){
            println("Assembly Error: ${e.message}")
            return
        }
        println("Assembled successfully to $output")
    }

}

fun main(args: Array<String>) = CPAssembler().main(args)