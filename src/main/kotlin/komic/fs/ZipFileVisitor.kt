package komic.fs

import komic.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern
import java.util.zip.Deflater.BEST_COMPRESSION
import java.util.zip.Deflater.NO_COMPRESSION
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

// rootdir is the directory where the zip file should get located, ie
// mydir/comicdir/imagexxx.jpg
//  -> "mydir/comicdir/imagexxx.jpg" are the files
//  -> "mydir" is the rootdir
//  -> every entry should be "comicdir/imagexxx.jpg"
class ZipFileVisitor(private val zout: ZipOutputStream, private val rootdir: Path?, private val entries_to_ignore : List<File> = emptyList()) : SimpleFileVisitor<Path>() {

	override fun visitFile(file: Path, attr: BasicFileAttributes): FileVisitResult {
		if (attr.isRegularFile && !entries_to_ignore.contains(file.toFile())) {
			val newfile = if (rootdir != null) file.toFile().relativeTo(rootdir.toFile()) else file.toFile()
			if(isDirtyFile(newfile)){ // skip those
				FileVisitResult.CONTINUE
			}
			val ze = ZipEntry(newfile.toString())
			zout.putNextEntry(ze)
			try {
				FileInputStream(file.toFile()).copyTo(zout)
			} finally {
				zout.closeEntry()
			}
		}
		return FileVisitResult.CONTINUE
	}
}

// unused, just for testing
fun unzip(cbz: File) {

	val directory = File(cbz.parent, cbz.name.substring(0, cbz.name.lastIndexOf(".")))
	if (directory.exists()) { // FIXME: check empty and check if we need to use internal directory
		throw ComicException("directory " + directory.absolutePath + " already exists")
	}
	directory.mkdir()

	ZipFile(cbz).use { zipFile ->
		val zipEntries = zipFile.entries()
		while (zipEntries.hasMoreElements()) {
			val entry = zipEntries.nextElement()
			val newFile = File(directory, entry.name)
			if (entry.isDirectory) {
				newFile.mkdirs()
			} else {
				newFile.parentFile.mkdirs()
				zipFile.getInputStream(entry).copyTo(FileOutputStream(newFile), zipFile.size())
			}
		}
	}
	// FIXME: split to separate method
	val subdirs = directory.list()
	if (subdirs?.size == 1) {
		val subdir = File(directory, subdirs[0])
		val files = subdir.list()
		files?.forEach {file ->
			// FIXME: possible name clash between file and subdir
			File(subdir, file).renameTo(File(directory, file))
		}
		subdir.delete()
		if (directory.name != subdir.name) {
			directory.renameTo(File(directory.name + " --- " + subdir.name))
		}
	}
}

// FIXME: needs testing
// read somewhere that there should be no compression, but cannot find solid argument (yes, low-end devices might need some time more to extract elements..)
// check repack-epub
//  * uses -D for no creating entries
//  * strip file attributes(apparently already discarded: https://stackoverflow.com/questions/5146003/is-there-a-way-in-java-to-preserve-created-and-accessed-attributes-of-a-file-whe)
// given "/path/dir/" creates /path/dir.cbz"
fun zip_cbz(dir: File, outfile: File) {
	ZipOutputStream(FileOutputStream(outfile)).use { zout ->
		zout.setLevel(BEST_COMPRESSION)
		Files.walkFileTree(dir.toPath(), ZipFileVisitor(zout, dir.toPath().parent))
	}
}

fun update(cbz: File) {
// https://stackoverflow.com/questions/11502260/modifying-a-text-file-in-a-zip-archive-in-java#21457205
}

/// true if equal (modulo .cbz)
/// or if directory (ends with /) is equal to filename
fun comparename(zip: ZipEntry, cbz: File): Boolean {
	val file1 = zip.name
	val file2 = cbz.name
	if (file1 == file2) {
		return true
	}
	if ("$file1.cbz" == file2 || "$file2.cbz" == file1) {
		return true
	}

	if (file1.endsWith('/') && file1.substring(0, file1.length - 1) == file2) {
		return true
	}
	if (file2.endsWith('/') && file2.substring(0, file2.length - 1) == file1) {
		return true
	}
	return false
}


fun validate_name(name:String) : Boolean {
	return !name.contains('\n')
}

/// Validations for epub:
///  * filename (no strange characters)
///  x file are optimally compressed
///  * dirty files/directories
///  x file permissions
/// FIXME: are those needed?
///  * mimetype file is first file https://github.com/w3c/publ-epub-revision/issues/1309
///  * META-INF dir
///  * OEBS


/// Validations for cbz:
///  * filename (no strange characters)
///  * exactly one "root" directory with correct name
///  * file names are 3 digits
///  * no gaps between files
///  x file are optimally compressed
///  * dirty files/directories
///  x file permissions
fun validate_cbz(cbz: File): List<String> {
	val toreturn = mutableListOf<String>()

	if(!validate_name(cbz.name)){
		toreturn.add("Name is not safe!")
	}

	val ints = mutableListOf<Int>()
	val internalpaths = mutableListOf<String>()
	// FIXME: pattern should capture .*/
	// capture of .*/ should all be equals
	// capture of \\d{3} should have no missing values, and begin from 1 (would not work on subfolders...)
	val pattern = "(.*/)(\\d{3})\\.[a-z]{3}"

	//val filenames = mutableListOf<String>()
	ZipFile(cbz).use { zipFile ->
		val zipEntries = zipFile.entries()
		while (zipEntries.hasMoreElements()) {
			val entry = zipEntries.nextElement()
			// check that images are sorted in form ddd.ext, and that there are no missing values
			if (entry.isDirectory) {
				// no useful checks AFAIK
				if (comparename(entry, cbz)) {
					toreturn.add("Entry ${entry.name} does not coincide with ${cbz.name}")
				}
				if (dirtyDirectories.any { it.compareTo(entry.name, ignoreCase = true) == 0 }) {
					toreturn.add("Dirty directory ${entry.name} detected")
				}
			} else {
				// could validate that text files are utf8-encoded, and images are optimal or something like that
				//zipFile.getInputStream(entry).copyTo(FileOutputStream(newFile), zipFile.size())
				if (is_image_file_name(entry.name)) {
					val p = Pattern.compile(pattern)
					val m = p.matcher(entry.name)
					val found = m.find()
					if (found) {
						internalpaths.add(m.group(1))
						ints.add(m.group(2).toInt())
					} else {
						toreturn.add("Skipped ${entry.name}")
					}
				} else {
					if (dirtyFiles.any { it.compareTo(entry.name, ignoreCase = true) == 0 }) {
						toreturn.add("Dirty file ${entry.name} detected")
					}
				}
			}
		}
	}

	if (internalpaths.isEmpty()) {
		toreturn.add("no internal paths")
	} else {
		val parts = internalpaths[0].split("/")
		if (internalpaths.any { parts[0] != it.split("/")[0] }) {
			toreturn.add("Not all root paths are equal")
		}
	}
	ints.sort()
	if (ints.asSequence().distinct().count() != ints.count()) {
		toreturn.add("File names are not unique")
		return toreturn
	}

	// makes sense only if filenames unique
	var checkval = 0
	for (i in ints) {
		++checkval
		if (i != checkval) {
			toreturn.add("First missing image nr $checkval")
			break // other checks do not make sense anymore
		}
	}
	return toreturn
}
