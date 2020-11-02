package komic.fs

import org.junit.Test
import java.nio.file.Files
import kotlin.test.*

internal class TestAutoDeleteFile {

	@Test
	fun `delete dir on exit`() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			// do nothing
		}
		assertFalse(tmpdir.exists())
	}

	@Test
	fun `do not delete dir on exit`() {
		val tmpdir = createTempDir()
		AutoDeleteFile(tmpdir).use {
			it.detach()
		}
		assertTrue(tmpdir.exists())
		tmpdir.deleteRecursively()
	}

}
