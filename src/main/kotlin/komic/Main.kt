package komic

import picocli.CommandLine
import java.util.*
import kotlin.system.exitProcess


fun main(vararg args: String){
	Locale.setDefault(Locale.ROOT)
	val result = CommandLine(KomicCmd()).execute(*args)
	if(result != 0){
		exitProcess(result)
	}
}


