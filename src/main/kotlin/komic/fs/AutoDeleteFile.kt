package komic.fs

import java.io.Closeable
import java.io.File
import java.nio.file.Path

class AutoDeleteFile(val file: File) : Closeable {
	private var detached = false

	constructor(p: Path) : this(p.toFile())

	override fun close() {
		if (!detached) {
			file.deleteRecursively()
		}
	}

	/**
	 * Do not delete the file/path on close
	 */
	fun detach() {
		detached = true;
	}
}
