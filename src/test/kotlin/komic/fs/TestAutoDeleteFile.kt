package komic.fs

import org.junit.Test
import java.nio.file.Files
import kotlin.test.*

internal class TestAutoDeleteFile {

	@Test
	fun `delete dir on exit`() {
		val tmpdir = Files.createTempDirectory("to_clean_")
		AutoDeleteFile(tmpdir.toFile()).use {
			// do nothing
		}
		assertFalse(Files.exists(tmpdir))
	}

	@Test
	fun `do not delete dir on exit`() {
		val tmpdir = Files.createTempDirectory("to_clean_")
		AutoDeleteFile(tmpdir.toFile()).use {
			it.detach()
		}
		assertTrue(Files.exists(tmpdir))
		tmpdir.toFile().deleteRecursively()
	}

}
