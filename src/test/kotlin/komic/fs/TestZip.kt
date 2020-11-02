package komic.fs


import komic.inducks.inducks_cat
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

internal class TestZip {

	@Test
	fun `zip and unzip`() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "test")
			zipdir.mkdir()

			for (i in 0..9) {
				PrintWriter(FileWriter(File(zipdir, "file.txt$i"), true), true).use { out ->
					out.print("file number $i")
				}
			}

			val zipfile = File(tmpdir, "test.zip")
			zip_cbz(zipdir, zipfile)
			zipdir.deleteRecursively()
			unzip(zipfile)

			// verify files
			walkFileTree(zipdir.toPath(), object : SimpleFileVisitor<Path>() {
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
	fun test_zip_unzip_default() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "test")
			zipdir.mkdir()

			for (i in 0..9) {
				PrintWriter(FileWriter(File(zipdir, "file.txt$i"), true), true).use { out ->
					out.print("file number $i")
				}
			}

			val zipfile = File(tmpdir, "test.zip")
			zip_cbz(zipdir, zipfile)
			zipdir.deleteRecursively()
			unzip(zipfile)

			// verify files
			walkFileTree(zipdir.toPath(), object : SimpleFileVisitor<Path>() {
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
	fun `cat inducks`() {
		val tmpdir = create_tmp_dir(listOf("test"), listOf())
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "test")

			val inducks_content = "[I TL 1483-A]  Paperinik divo del cinema\n" +
					"[I TL 2328]    Giù la maschera - Paure nascoste \n" +
					"[I PK 100-1]   Paperinik e la minaccia cuginiforme \n" +
					"[I TL 2561-3]  Paperinik e la complicata meringata assoluta \n" +
					"[I TL 1619-A]  Paperino e... il rivale Paperinik \n" +
					"[I PK 125-1]   Paperinik e la... primissima impresa\n" +
					"[I TL 2330-04] Giù la maschera - Radio ammissioni\n" +
					"[I TL 1632-A]  Paperinik e il mistero di \"Tuba Mascherata\"\n" +
					"[I TL 2427-03] Edi un amico per le pile: Super Edi\n" +
					"[I PK 111-1]   Paperinik e il Natale eroico\n"

			PrintWriter(FileWriter(File(zipdir, "inducks.txt"), true), true).use { out ->
				out.print(inducks_content)
			}

			val zipfile = File(tmpdir, "test.zip")
			ZipOutputStream(FileOutputStream(zipfile)).use { zout ->
				walkFileTree(zipdir.toPath(), ZipFileVisitor(zout, zipdir.toPath()))
			}

			val read_content = inducks_cat(zipfile)
			val inducks_content_as_list = inducks_content.lines()
			for (i in read_content.indices) {
				assertEquals(read_content[i], inducks_content_as_list[i])
			}
		}
	}

	@Test
	fun `mimetype comes first and is not compressed`() {
		val tmpdir = create_tmp_dir(listOf("epub"), listOf())
		AutoDeleteFile(tmpdir).use {
			val zipdir = File(tmpdir, "epub")

			val content = "application/epub+zip"
			PrintWriter(FileWriter(File(zipdir, "mimetype"), true), true).use { out ->
				out.println(content)
			}
			val zipfile = File(tmpdir, "test.zip")
			zip_epub(zipdir, zipfile)
			val buff = ByteArray(100)
			zipfile.inputStream().use { input ->
				val sz = input.read(buff)
				assertEquals('P'.toByte(), buff[0])
				assertEquals('K'.toByte(), buff[1])
				assertEquals(0x03, buff[2])
				assertEquals(0x04, buff[3])
				val from_list = buff.toString(Charsets.US_ASCII)
				if(!from_list.toList().containsAll(content.toList())){
					fail("content not found: $from_list")
				}
			}
		}
	}

}
