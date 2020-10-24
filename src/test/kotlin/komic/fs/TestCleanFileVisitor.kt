package komic.fs

import komic.clean
import komic.isDirtyDirectory
import komic.isDirtyFile
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.test.*

// FIXME: move to helpers for testing

fun create_tmp_dir(listdirs: List<String> = emptyList(), listfiles: List<String> = emptyList()): File {
	val tmpdir = Files.createTempDirectory("to_clean_")
	val toreturn = tmpdir.toFile()
	try {
		assertTrue(listdirs.all { !it.startsWith('/') })
		assertTrue(listfiles.all { !it.startsWith('/') })
		assertTrue(listdirs.all { File(toreturn, it).mkdirs() })
		assertTrue(listfiles.all { File(toreturn, it).createNewFile() })
		return toreturn
	} catch (t: Throwable) {
		toreturn.deleteRecursively()
		throw t
	}
}

class ListFilesAndDirs : SimpleFileVisitor<Path>() {
	val listed_files: MutableList<File> = mutableListOf()
	val listed_dirs: MutableList<File> = mutableListOf()

	override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
		assert(!attr.isDirectory)
		if (attr.isRegularFile) {
			listed_files.add(path.toFile())
		}
		return FileVisitResult.CONTINUE
	}

	override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
		val file = dir.toFile()
		assert(!file.isFile)
		if (file.isDirectory) {
			listed_dirs.add(file)
		}
		return FileVisitResult.CONTINUE
	}
}

internal class TestRemoveDirtyFiles {
	@Test
	fun `test create_tmp_dir`() {
		val listdirs = listOf("a/b/c", "d", "e")
		val listfiles = listOf("a/b/c/f.txt", "f.txt", "a/f.txt")
		val tmpdir = create_tmp_dir(listdirs, listfiles)
		AutoDeleteFile(tmpdir).use { _ ->
			val visitor = ListFilesAndDirs();
			Files.walkFileTree(tmpdir.toPath(), visitor)
			assertTrue(visitor.listed_dirs.map { it.path.removePrefix(tmpdir.path + "/") }.containsAll(listdirs));
			assertTrue(visitor.listed_files.map { it.path.removePrefix(tmpdir.path + "/") }.containsAll(listfiles))
		}
	}

	@Test
	fun `warn about hidden files`() {
		val listdirs = listOf("a/b/c", "d", "e")
		val listfiles = listOf("a/b/c/.txt", ".txt", "a/.txt")
		val tmpdir = create_tmp_dir(listdirs, listfiles)
		AutoDeleteFile(tmpdir).use { _ ->
			val fwarns = clean(tmpdir)
			val swarns = fwarns.map { it.path.removePrefix(tmpdir.path + "/") }
			assertTrue(swarns.containsAll(listfiles))
		}
	}

	@Test
	fun `warn about hidden directories`() {
		val listdirs = listOf("a/b/.c", ".d", ".e")
		val listfiles = listOf("a/b/.c/f.txt", ".d/f.txt", ".e/f.txt")
		val tmpdir = create_tmp_dir(listdirs, listfiles)
		AutoDeleteFile(tmpdir).use { _ ->
			val fwarns = clean(tmpdir)
			val swarns = fwarns.map { it.path.removePrefix(tmpdir.path + "/") }
			assertTrue(swarns.containsAll(listdirs))
		}
	}

	@Test
	fun `clean files`() {
		val listdirs = listOf("__MACOSX", "emptydir1", "emptydir2", "subdir")
		val listfiles = listOf(".DS_Store", "Thumbs.db", "subdir/Thumbs.db", "goodfile1.txt", "goodfile2.txt", "subdir/goodfile3.txt", "subdir/.hidden.txt")
		val tmpdir = create_tmp_dir(listdirs, listfiles)
		AutoDeleteFile(tmpdir).use { _ ->
			val warns = clean(tmpdir)
			assertFalse(warns.isEmpty())

			val list_files = ListFilesAndDirs();
			Files.walkFileTree(tmpdir.toPath(), list_files)

			assertTrue(list_files.listed_files.all { !isDirtyFile(it) });
			assertTrue(list_files.listed_dirs.all { !isDirtyDirectory(it) });
		}

	}
}
