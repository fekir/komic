package komic

import komic.fs.AutoDeleteFile
import komic.fs.WindowsExplorerFileComparator
import komic.fs.create_tmp_dir
import org.junit.Test
import java.io.File
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
	fun test_enumerate() {
		val filenames = listOf("001", "002a", "002b", "003", "004").map { "$it.jpg" }
		val tmpdir = create_tmp_dir(listOf(), filenames)
		AutoDeleteFile(tmpdir).use {
			val index = enumerate_images(tmpdir)
			assertEquals(filenames.size, index - 1)

			val fileList = tmpdir.listFiles().orEmpty()
			fileList.sort()

			for (i in fileList.indices) {
				assertEquals(String.format("%03d.jpg", i + 1), fileList[i].name)
			}
		}
	}

	@Test
	fun test_prefix_enumerate() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			val prefix = "pre"
			val fileNames = arrayListOf(prefix, prefix + prefix)
			fileNames.sort()
			for (i in fileNames.indices) {
				File(tmpdir, fileNames[i] + ".jpg").createNewFile()
			}

			enumerate_prefix_images(tmpdir, prefix)
			val fileList = tmpdir.listFiles().orEmpty()
			assertEquals(fileNames.size, fileList.size)
			fileList.sort()
			for (i in fileList.indices) {
				assertEquals(prefix + fileNames[i] + ".jpg", fileList[i].name)
			}
		}
	}
}
