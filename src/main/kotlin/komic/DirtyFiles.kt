package komic

import komic.fs.CleanFileVisitor
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

val dirtyFiles = listOf(".DS_Store", "Thumbs.db", ".picasa.ini")
val dirtyDirectories = listOf("__MACOSX")

fun isDirtyFile(file: File): Boolean {
	if (!file.isFile) {
		return false
	}
	if (dirtyFiles.any { it.compareTo(file.name, ignoreCase = true) == 0 }) {
		return true
	}
	// works, but takes a lot of time...
	return is_monocolor_image(file)
}

fun isDirtyDirectory(file: File): Boolean {
	return file.isDirectory && (dirtyDirectories.any { it.compareTo(file.name, ignoreCase = true) == 0 } || file.list()?.isEmpty() ?: false)
}

fun isHidden(file: File): Boolean {
	return file.name.startsWith('.') && file.name != "."
}


fun clean(folder: Path): List<File> {
	val remover = CleanFileVisitor({ isDirtyFile(it) }, { isDirtyDirectory(it) }, { isHidden(it) }, { isHidden(it) })
	Files.walkFileTree(folder, remover)
	return remover.hiddenfiles + remover.hiddendirs
}

fun clean(folder: File): List<File> {
	return clean(folder.toPath())
}
