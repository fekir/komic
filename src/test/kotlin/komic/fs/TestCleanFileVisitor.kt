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
	val tmpdir = createTempDir()
	try {
		assertTrue(listdirs.all { !it.startsWith('/') })
		assertTrue(listfiles.all { !it.startsWith('/') })
		assertTrue(listdirs.all { File(tmpdir, it).mkdirs() })
		assertTrue(listfiles.all { File(tmpdir, it).createNewFile() })
		return tmpdir
	} catch (t: Throwable) {
		tmpdir.deleteRecursively()
		throw t
	}
}

internal class TestRemoveDirtyFiles {
	@Test
	fun `test create_tmp_dir`() {
		val listdirs = listOf("a/b/c", "d", "e")
		val listfiles = listOf("a/b/c/f.txt", "f.txt", "a/f.txt")
		val tmpdir = create_tmp_dir(listdirs, listfiles)
		AutoDeleteFile(tmpdir).use { _ ->
			val filesanddirs = tmpdir.walk()
			assertTrue(filesanddirs.filter { it.isDirectory  }.map { it.path.removePrefix(tmpdir.path + "/") }.toList().containsAll(listdirs))
			assertTrue(filesanddirs.filter { it.isFile  }.map { it.path.removePrefix(tmpdir.path + "/") }.toList().containsAll(listfiles))
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

			val files = tmpdir.walk()
			assertTrue(files.filter { it.isFile }.all { !isDirtyFile(it) })
			assertTrue(files.filter { it.isDirectory  }.all { !isDirtyDirectory(it) })
		}
	}
}
