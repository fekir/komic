package komic

import komic.fs.un7zip
import komic.fs.validate_cbz
import komic.fs.zip_cbz
import komic.fs.zip_epub
import komic.inducks.inducks_cat
import picocli.CommandLine
import java.io.File


// FIXME:
//  all command with output should accept --out to specify file/folder location and sensible default

@CommandLine.Command(name = "optimize", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["Optimizes files (images, cbz, ...) in-place"])
class KomicCmdOptimize : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity="1")
	private val files = emptyList<File>()

	override fun run() {
		optimize_files(files)
	}
}

@CommandLine.Command(name = "extract", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["Extract archives (cbz, zip, epub, rar, ...)"])
class KomicCmdExtract : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity="1")
	private val files = emptyList<File>()

	override fun run() {
		files.asSequence().filter { it.isFile }.forEach { un7zip(it) }
	}
}

@CommandLine.Command(name = "enumerate", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["Enumerates images in folder 001.jpg, 002.jpg, ..."])
class KomicCmdEnumerate : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity="1")
	private val files = emptyList<File>()

	@CommandLine.Option(names = [ "--recursive" ], description = ["If \"no\", images in subdirectories begins by 001.",  "Valid values: \${COMPLETION-CANDIDATES}", "Default: \${DEFAULT-VALUE}"])
	private val rec = recursive.no

	override fun run() {
		files.filter { it.isDirectory }.forEach { enumerate_images(it, 1, rec) }
	}
}

@CommandLine.Command(name = "clean", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["Remove dirty/unwanted files", "Creates a warning if hidden files found"])
class KomicCmdClean : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity="1")
	private val files = emptyList<File>()

	override fun run() {
		files.filter { it.isDirectory }.forEach { dir ->
			val hiddenfiles = clean(dir)
			if (hiddenfiles.isNotEmpty()) {
				System.err.println("Hidden files found:")
				hiddenfiles.forEach { System.err.println("\t" + it) }
			}
		}
	}
}

@CommandLine.Command(name = "validate", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["Validates cbz files (file name, if dirty files present, ...)"])
class KomicCmdValidate : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity="1")
	private val files = emptyList<File>()

	override fun run() {
		files.forEach { file ->
			val res = validate_cbz(file)
			if (res.isNotEmpty()) {
				println("Validation for $file failed:")
				println(res)
			}
		}
	}
}

@CommandLine.Command(name = "create", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["Creates a .cbz from a folder"])
class KomicCmdCreate : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity="1")
	private val files = emptyList<File>()

	private enum class Format{cbz, epub}
	@CommandLine.Option(names = [ "--format" ], required = true, description = ["Format of digital archive", "Valid values: \${COMPLETION-CANDIDATES}"])
	private val f = Format.cbz

	override fun run() {
		files.filter { it.isDirectory }.forEach {
			when(f){
				Format.cbz -> zip_cbz(it, File(it.absolutePath + ".cbz"))
				Format.epub -> zip_epub(it, File(it.absolutePath + ".epub"))
			}
		}
	}
}

@CommandLine.Command(name = "cat", mixinStandardHelpOptions = true, showEndOfOptionsDelimiterInUsageHelp = true, description = ["I'm a subcommand for handling inducks data"])
class KomicCmdInducksCat : Runnable {
	@CommandLine.Parameters(paramLabel = "files", description = ["files to process"], arity = "1")
	private val files = emptyList<File>()
	override fun run() {
		files.forEach { file ->
			val entries = inducks_cat(file)
			if (entries.isEmpty()) {
				System.err.println("No entries found in $file")
			}
			for (line in entries) {
				println(line)
			}
		}
	}
}

@CommandLine.Command(name = "inducks",
		mixinStandardHelpOptions = true,
		description = ["I'm a subcommand for handling inducks data"],
		subcommands =  [KomicCmdInducksCat::class]
)
class KomicCmdInducks : Runnable {
	@CommandLine.Spec
	val spec: CommandLine.Model.CommandSpec? = null
	override fun run() {
		throw CommandLine.ParameterException(spec?.commandLine(), "Specify a subcommand")
	}
}

@CommandLine.Command(name = "komic",
		version = ["komic v1.0.0"],
		mixinStandardHelpOptions = true,
		description = ["FIXME"],
		subcommands = [
			KomicCmdEnumerate::class, KomicCmdOptimize::class, KomicCmdExtract::class, KomicCmdClean::class, KomicCmdValidate::class,
			KomicCmdInducks::class, KomicCmdCreate::class
		]
)
class KomicCmd : Runnable {
	@CommandLine.Spec
	val spec: CommandLine.Model.CommandSpec? = null
	override fun run() {
		throw CommandLine.ParameterException(spec?.commandLine(), "Specify a subcommand")
	}
}
