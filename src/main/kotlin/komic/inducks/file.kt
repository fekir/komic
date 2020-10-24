package komic.inducks

import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipFile


fun inducks_cat(cbz: File): List<String> {
	val toreturn: MutableList<String> = mutableListOf()
	ZipFile(cbz).use { zipFile ->
		val zipEntries = zipFile.entries()
		while (zipEntries.hasMoreElements()) {
			val entry = zipEntries.nextElement()
			val fileName = entry.name
			if (fileName.endsWith("inducks.txt")) {
				val buff = zipFile.getInputStream(entry).bufferedReader(Charsets.UTF_8)
				val content = buff.readLines()
				toreturn.addAll(content)
			}
		}
	}
	return toreturn
}

fun inducks_clean_single_file(file: File) {
	if (file.name != "inducks.txt") {
		return
	}
	assert(file.isFile)
	// verify it text file
	val lines = file.bufferedReader()
			.readLines()
			.asSequence()
			.map { it.replace(160.toChar(), ' ') }
			.map { it.trim() }
			.filterNot { it.isEmpty() }
			.toList()
	file.bufferedWriter().use { br ->
		lines.forEach { line -> br.write(line + "\n") }
	}
}

fun inducks_clean_directory(file: File) {
	assert(file.isDirectory)
	Files.walkFileTree(file.toPath(), object : SimpleFileVisitor<Path>() {
		override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
			assert(!attr.isDirectory)
			inducks_clean_single_file(path.toFile())
			return FileVisitResult.CONTINUE
		}
	})
}

fun inducks_clean(file: File) {
	if (file.isDirectory) {
		inducks_clean_directory(file)
	} else {
		inducks_clean_single_file(file)
	}
}
