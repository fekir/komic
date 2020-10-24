package komic

import komic.fs.AutoDeleteFile
import komic.fs.WindowsExplorerFileComparator
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.test.assertEquals


internal class TestRename {

	@Test
	fun test_windows_comparator() {
		val filenames = arrayListOf(
				File("Filename01.jpg"),
				File("filename"),
				File("filename0"),
				File("filename 0"),
				File("Filename1.jpg"),
				File("Filename10.jpg"),
				File("filename.jpg"),
				File("filename2.jpg"),
				File("filename03.jpg"),
				File("filename3.jpg"),
				File("filename00.jpg"),
				File("filename0.jpg"),
				File("filename0b.jpg"),
				File("filename0b1.jpg"),
				File("filename0b02.jpg"),
				File("filename0c.jpg"),
				File("filename00a.jpg"),
				File("filename.txt"),
				File("filename00a.txt"),
				File("filename0a.txt"),
				File("filename01.0hjh45-test.txt"),
				File("filename01.0hjh46"),
				File("filename2.hjh45.txt"),
				File("filename01.1hjh45.txt"),
				File("filename01.hjh45.txt"),
				File("filename 01"),
				File("filename 00")
		)

		//adaptor for comparing files
		Collections.sort(filenames, WindowsExplorerFileComparator)


		val sortedfilename = arrayListOf(
				File("filename"),
				File("filename 00"),
				File("filename 0"),
				File("filename 01"),
				File("filename.jpg"),
				File("filename.txt"),
				File("filename00.jpg"),
				File("filename00a.jpg"),
				File("filename00a.txt"),
				File("filename0"),
				File("filename0.jpg"),
				File("filename0a.txt"),
				File("filename0b.jpg"),
				File("filename0b1.jpg"),
				File("filename0b02.jpg"),
				File("filename0c.jpg"),
				File("filename01.0hjh45-test.txt"),
				File("filename01.0hjh46"),
				File("filename01.1hjh45.txt"),
				File("filename01.hjh45.txt"),
				File("Filename01.jpg"),
				File("Filename1.jpg"),
				File("filename2.hjh45.txt"),
				File("filename2.jpg"),
				File("filename03.jpg"),
				File("filename3.jpg"),
				File("Filename10.jpg")
		)
		assertEquals(filenames.size, sortedfilename.size)
		for (i in 0 until filenames.size) {
			assertEquals(sortedfilename[i].name, filenames[i].name)
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

			val fileList = it.file.listFiles()
			fileList.sort()

			for (i in 0 until fileList.size) {
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
			for (i in 0 until fileNames.size) {
				File(it.file, fileNames[i] + ".jpg").createNewFile()
			}

			enumerate_prefix_images(it.file, prefix)
			val fileList = it.file.listFiles()
			fileList.sort()
			for (i in 0 until fileList.size) {
				assertEquals(prefix + fileNames[i] + ".jpg", fileList[i].name)
			}
		}
	}
}
