package komic

import komic.fs.*
import org.junit.Ignore
import org.junit.Test
import java.io.*
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class TestCLI {

	@Test
	fun cli_create_cbz(){
		val tmpdir = create_tmp_dir()
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "test")
			zipdir.mkdir()

			for (i in 0..9) {
				PrintWriter(FileWriter(File(zipdir, "file.txt$i"), true), true).use { out ->
					out.println("file number $i")
				}
			}

			main("create", "--format=cbz", zipdir.absolutePath)
			zipdir.deleteRecursively()
			val zipfile = File(tmpdir, "test.cbz")
			assertTrue(zipfile.exists())
			unzip(zipfile)

			// verify files
			Files.walkFileTree(zipdir.toPath(), object : SimpleFileVisitor<Path>() {
				override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
					assertFalse(attr.isDirectory, "should not be directory")
					val content = String(Files.readAllBytes(path))
					val name = path.toFile().name
					val index = Character.getNumericValue(name[name.length - 1])
					assertEquals("file number $index\n", content)
					return FileVisitResult.CONTINUE
				}
			})
		}
	}

	@Test
	fun cli_optimize_cbz(){
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			//komic create --format cbz comic; rm -r comic; cp comic.cbz comic.cbz2; komic optimize comic.cbz
			val comicdir = File(tmpdir, "comic")
			File("data/comic").copyRecursively(comicdir)
			main("create", "--format", "cbz", comicdir.absolutePath)
			val cbz = File(tmpdir, "comic.cbz")
			assertTrue(cbz.exists())
			comicdir.deleteRecursively()
			main("optimize", cbz.absolutePath)
			assertTrue(cbz.length() < File(tmpdir, "comic.cbz.back").length())
		}
	}

	@Test
	fun cli_optimize_dir_jpg(){
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			val image = File("data/comic/001.jpg")
			val newimage = File(tmpdir, "001.jpg")
			image.copyTo(File(tmpdir, "001.jpg"))
			val size0 = image.length()
			main("optimize", tmpdir.absolutePath)
			val size1 = newimage.length()
			// test size is now smaller
			assertTrue( size1 < size0 )
		}
	}
	@Test
	fun cli_optimize_file_jpg(){
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			val image = File("data/comic/001.jpg")
			val newimage = File(tmpdir, "001.jpg")
			image.copyTo(File(tmpdir, "001.jpg"))
			val size0 = image.length()
			main("optimize", newimage.absolutePath)
			val size1 = newimage.length()
			// test size is now smaller
			assertTrue( size1 < size0 )
		}
	}

	@Test
	fun cli_extract() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use { _ ->
			val archive = File(tmpdir, "data/test.7z")
			File("data/test.7z").copyTo(archive)
			main("extract", archive.toString())
			archive.delete()

			val files = tmpdir.walk().filter { it.isFile }
			assertEquals(2, files.count())
			assertTrue(files.map { it.path.removePrefix(tmpdir.path + "/data/test/") }.toList().containsAll(listOf("file.txt", "file2.txt")))
		}
	}

	@Test
	fun cli_enumerate(){
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use { _ ->
			val filenames2 = filenames.map { val f = File(tmpdir, it); f.createNewFile(); f.canonicalPath}
			main("enumerate", "--recursive=no", tmpdir.toString())


			val imagefiles = tmpdir.walk().filter { it.isFile && it.name.endsWith(".jpg") }
			assertEquals(filenames.filter { it.endsWith(".jpg") }.size, imagefiles.count())
			val expected = mutableListOf<String>()
			for (i in 1 until 15) {
				expected.add(String.format("%03d.jpg", i))
			}
			assertTrue(imagefiles.map { it.path.removePrefix(tmpdir.path + "/") }.toList().containsAll(expected))
		}
	}

	@Test
	fun cli_clean(){
		// FIXME: add hidden file and check for warning
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use { _ ->
			File(tmpdir, ".picasa.ini").createNewFile()
			File(tmpdir, "001.jpg").createNewFile()
			run {
				val files = tmpdir.walk().filter { it.isFile }
				assertEquals(2, files.count())
			}
			main("clean", tmpdir.toString())
			run {
				val files = tmpdir.walk().filter { it.isFile }
				assertEquals(1, files.count())
			}
		}
	}
}
