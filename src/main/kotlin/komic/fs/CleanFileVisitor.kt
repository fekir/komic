package komic.fs

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes


class CleanFileVisitor(
		private val isDirtyFilefun: (File) -> Boolean,
		private val isDirtyPathfun: (File) -> Boolean,
		private val isDirectorytoWarnfun: (File) -> Boolean,
		private val isFiletoWarnfun: (File) -> Boolean)
	: SimpleFileVisitor<Path>() {

	val hiddenfiles: MutableList<File> = mutableListOf()
	val hiddendirs: MutableList<File> = mutableListOf()

	// remove dirty files, not called for directories
	override fun visitFile(file_: Path, attr: BasicFileAttributes): FileVisitResult {
		assert(!attr.isDirectory)
		val file = file_.toFile()
		assert(!file.isDirectory)
		if (attr.isRegularFile) {
			if (this.isDirtyFilefun(file)) {
				file.delete()
			} else if (this.isFiletoWarnfun(file)) {
				hiddenfiles.add(file)
			}
		}
		return FileVisitResult.CONTINUE
	}

	// remove dirty directories, not called for files, after deleting files
	override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
		val file = dir.toFile()
		assert(!file.isFile)
		if (file.isDirectory) {
			if (this.isDirtyPathfun(file)) {
				file.deleteRecursively()
			} else if (this.isDirectorytoWarnfun(file)) {
				hiddendirs.add(file)
			}
		}
		return FileVisitResult.CONTINUE
	}
}

