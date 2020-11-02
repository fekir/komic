package komic.fs

import org.junit.Test
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class Test7zip {

	@Test
	fun `extract 7z`() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use { _ ->
			val tmpdir = File(tmpdir, "temp")
			un7zip("data/test.7z", tmpdir)

			val files = tmpdir.walk().filter { it.isFile }.toList()
			assertEquals(2, files.size)
			assertTrue(files.map { it.path.removePrefix(tmpdir.path + "/") }.containsAll(listOf("test/file.txt", "test/file2.txt")))
		}
	}

	@Test
	fun `extract 7z with dirty dir`() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use { _ ->
			val tmpdir = File(tmpdir, "temp")
			un7zip("data/test-with-dirty-dir.7z", tmpdir)

			val files = tmpdir.walk().filter { it.isFile }.toList()
			assertEquals(2, files.size)
			assertTrue(files.map { it.path.removePrefix(tmpdir.path + "/") }.containsAll(listOf("test/file.txt", "test/file2.txt")))
		}
	}

	@Test
	fun `extract rar`() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use { _ ->
			val tmpdir = File(tmpdir, "temp")
			un7zip("data/test.rar", tmpdir)

			val files = tmpdir.walk().filter { it.isFile }.toList()
			assertEquals(2, files.size)
			assertTrue(files.map { it.path.removePrefix(tmpdir.path + "/") }.containsAll(listOf("test/file.txt", "test/file2.txt")))
		}
	}


	@Test
	fun `zip and un7zip`() {
		val tmpdir = create_tmp_dir(listOf("test"))
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "test")

			for (i in 0..9) {
				PrintWriter(FileWriter(File(zipdir, "file.txt$i"), true), true).use { out ->
					out.print("file number $i")
				}
			}

			val zipfile = File(tmpdir, "test.zip")
			zip_cbz(zipdir, zipfile)
			zipdir.deleteRecursively()
			un7zip(zipfile)

			// verify files
			Files.walkFileTree(zipdir.toPath(), object : SimpleFileVisitor<Path>() {
				override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
					assertFalse(attr.isDirectory, "should not be directory")
					val content = String(Files.readAllBytes(path))
					val name = path.toFile().name
					val index = Character.getNumericValue(name[name.length - 1])
					assertEquals("file number $index", content)
					return FileVisitResult.CONTINUE
				}
			})
		}
	}

	@Test
	fun `zip and un7zip default`() {
		val tmpdir = create_tmp_dir(listOf("test"))
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "test")

			for (i in 0..9) {
				PrintWriter(FileWriter(File(zipdir, "file.txt$i"), true), true).use { out ->
					out.print("file number $i")
				}
			}

			val zipfile = File(tmpdir, "test.zip")
			zip_cbz(zipdir, zipfile)
			zipdir.deleteRecursively()
			un7zip(zipfile)

			// verify files
			Files.walkFileTree(zipdir.toPath(), object : SimpleFileVisitor<Path>() {
				override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
					assertFalse(attr.isDirectory, "should not be directory")
					val content = String(Files.readAllBytes(path))
					val name = path.toFile().name
					val index = Character.getNumericValue(name[name.length - 1])
					assertEquals("file number $index", content)
					return FileVisitResult.CONTINUE
				}
			})
		}
	}

}
