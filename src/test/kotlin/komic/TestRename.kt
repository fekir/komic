package komic

import komic.fs.AutoDeleteFile
import komic.fs.WindowsExplorerFileComparator
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.test.assertEquals

val filenames = listOf(
		"Filename01.jpg",
		"filename",
		"filename0",
		"filename 0",
		"Filename1.jpg",
		"Filename10.jpg",
		"filename.jpg",
		"filename2.jpg",
		"filename03.jpg",
		"filename3.jpg",
		"filename00.jpg",
		"filename0.jpg",
		"filename0b.jpg",
		"filename0b1.jpg",
		"filename0b02.jpg",
		"filename0c.jpg",
		"filename00a.jpg",
		"filename.txt",
		"filename00a.txt",
		"filename0a.txt",
		"filename01.0hjh45-test.txt",
		"filename01.0hjh46",
		"filename2.hjh45.txt",
		"filename01.1hjh45.txt",
		"filename01.hjh45.txt",
		"filename 01",
		"filename 00"
)

val sortedfilename = listOf(
		"filename",
		"filename 00",
		"filename 0",
		"filename 01",
		"filename.jpg",
		"filename.txt",
		"filename00.jpg",
		"filename00a.jpg",
		"filename00a.txt",
		"filename0",
		"filename0.jpg",
		"filename0a.txt",
		"filename0b.jpg",
		"filename0b1.jpg",
		"filename0b02.jpg",
		"filename0c.jpg",
		"filename01.0hjh45-test.txt",
		"filename01.0hjh46",
		"filename01.1hjh45.txt",
		"filename01.hjh45.txt",
		"Filename01.jpg",
		"Filename1.jpg",
		"filename2.hjh45.txt",
		"filename2.jpg",
		"filename03.jpg",
		"filename3.jpg",
		"Filename10.jpg"
)

internal class TestRename {

	@Test
	fun test_windows_comparator() {


		//adaptor for comparing files
		//Collections.sort(filenames, WindowsExplorerFileComparator)
		val filenames2 = filenames.map { File(it) }
		Collections.sort(filenames2, WindowsExplorerFileComparator)

		assertEquals(filenames2.size, sortedfilename.size)
		for (i in filenames2.indices) {
			assertEquals(sortedfilename[i], filenames2[i].name)
		}
	}

	@Test
	fun test_clean_file_ext() {
		val expected = "myext.jpg"
		val res1 = clean_file_ext("myext.jpg")
		assertEquals(expected, res1)
		val res2 = clean_file_ext("myext.JPG")
		assertEquals(expected, res2)
		val res3 = clean_file_ext("myext.JPEG")
		assertEquals(expected, res3)
		val res4 = clean_file_ext("myext.jpeg")
		assertEquals(expected, res4)
	}

	@Test
	fun test_enumerate() {
		val tmpdir = Files.createTempDirectory("to_enum_")
		AutoDeleteFile(tmpdir).use {
			val filenames = arrayListOf("001", "002a", "002b", "003", "004")
			for (i in 0 until filenames.size) {
				File(it.file, filenames[i] + ".jpg").createNewFile()
			}

			val index = enumerate_images(it.file)
			assertEquals(filenames.size, index - 1)

			val fileList = it.file.listFiles().orEmpty()
			fileList.sort()

			for (i in fileList.indices) {
				assertEquals(String.format("%03d.jpg", i + 1), fileList[i].name)
			}
		}
	}

	@Test
	fun test_prefix_enumerate() {
		val tmpdir = Files.createTempDirectory("to_prefix_")
		AutoDeleteFile(tmpdir).use {
			val prefix = "pre"
			val fileNames = arrayListOf(prefix, prefix + prefix)
			fileNames.sort()
			for (i in fileNames.indices) {
				File(it.file, fileNames[i] + ".jpg").createNewFile()
			}

			enumerate_prefix_images(it.file, prefix)
			val fileList = it.file.listFiles().orEmpty()
			assertEquals(fileNames.size, fileList.size)
			fileList.sort()
			for (i in fileList.indices) {
				assertEquals(prefix + fileNames[i] + ".jpg", fileList[i].name)
			}
		}
	}
}
