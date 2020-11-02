package komic.fs


import komic.ComicException
import komic.dirtyDirectories
import net.sf.sevenzipjbinding.*
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile


fun flat_dir(directory : File){
	val subdirs = directory.list()
	if (subdirs?.size != 1) {
		return
	}
	val subdir = File(directory, subdirs[0])
	val files = subdir.list()
	files?.forEach {file ->
		// FIXME: possible name clash between file and subdir
		File(subdir, file).renameTo(File(directory, file))
	}
	subdir.delete()
}

fun un7zip(cbz: File) {
	val directory = File(cbz.parent, cbz.nameWithoutExtension)
	if (directory.exists()) { // FIXME: check empty and check if we need to use internal directory
		throw ComicException("directory " + directory.absolutePath + " already exists")
	}

	un7zip(cbz.path, directory)
	flat_dir(directory)
}

fun un7zip(file: String, extractPath: File) {
	if(extractPath.exists()){
		throw ComicException("path already exists")
	}
	extractPath.mkdir()
	RandomAccessFile(File(file), "r").use { randomAccessFile ->
		SevenZip.openInArchive(null, RandomAccessFileInStream(randomAccessFile)).use { inArchive ->
			inArchive.extract(null, false, MyExtractCallback(inArchive, extractPath))
		}
	}
}

// FIXME: do not use directly as opens/closes multiple resources
// create algorithm that sorts over items and accepts predicate?
fun zipContainsFile(crx: File, filename: String): Boolean {
	RandomAccessFile(crx, "r").use { randomAccessFile ->
		SevenZip.openInArchive(null, RandomAccessFileInStream(randomAccessFile)).use { inArchive ->
			for (item in inArchive.simpleInterface.archiveItems) {
				if (item.path == filename) return true
			}
			return false
		}
	}
}


class MySequentialOutStream(private val fos: FileOutputStream) : ISequentialOutStream, AutoCloseable {
	override fun write(data: ByteArray): Int {
		fos.write(data)
		return data.size
	}

	override fun close() {
		fos.close()
	}
}

class MyExtractCallback(private val inArchive: IInArchive, private val extractPath: File) : IArchiveExtractCallback {

	override fun getStream(index: Int, extractAskMode: ExtractAskMode): ISequentialOutStream? {

		val filePath = inArchive.getStringProperty(index, PropID.PATH)
		val isfolder = inArchive.getStringProperty(index, PropID.IS_FOLDER)

		val path = File(extractPath.path, filePath)

		if(dirtyDirectories.any { path.absolutePath.contains("/$it", ignoreCase = true) }){ // avoid extracting, as result we might have only one directory
			// FIXME: add warning or flag for overriding behaviour
			return null
		}

		if (isfolder == "+") {
			if (!path.exists() && !path.mkdirs()) {
				throw RuntimeException("unable to create dirs")
			}
			return null
		}

		path.parentFile.mkdirs()
		// Possible resource leak..., marked MySequentialOutStream as autocloseable, i guess there is nothing better to do...
		// the alternative without leak would be opening and closing the file every time...
		val fos = FileOutputStream(path, true)
		return MySequentialOutStream(fos)
	}

	override
	fun prepareOperation(extractAskMode: ExtractAskMode) {
	}

	override
	fun setOperationResult(extractOperationResult: ExtractOperationResult) {
	}

	override
	fun setCompleted(completeValue: Long) {
	}

	override
	fun setTotal(total: Long) {
	}
}
