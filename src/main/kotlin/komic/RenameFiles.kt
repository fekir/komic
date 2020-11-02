package komic

import komic.fs.FileNameLengthComparator
import komic.fs.WindowsExplorerFileComparator
import java.io.File
import java.util.*

fun is_image_file_name(file: File): Boolean {
	val ext = file.extension.toLowerCase(Locale.ROOT)
	return ext.endsWith("jpg") || ext.endsWith("jpeg") || ext.endsWith("png") || ext.endsWith("gif") || ext.endsWith("tiff")
}


enum class recursive {
	yes, no
}

fun enumerate_prefix_images(folder: File, prefix: String) {
	val fileList = folder.listFiles { file ->
		file.isDirectory || is_image_file_name(file) }.orEmpty().toList()

	// avoid overwriting files
	Collections.sort(fileList, FileNameLengthComparator)
	for (file in fileList) {
		if (file.isDirectory) {
			enumerate_prefix_images(file, prefix)
		}
		file.renameTo(File(folder, prefix + file.name))
	}
}

fun enumerate_images(folder: File, begin: Int = 1, rec_enum: recursive = recursive.yes): Int {
	// add prefix to avoid overwriting files
	enumerate_prefix_images(folder, "ren")

	val fileList = folder.listFiles { file ->
		file.isDirectory || is_image_file_name(file) }.orEmpty().toList()

	Collections.sort(fileList, WindowsExplorerFileComparator)

	var index = begin
	for (file in fileList) {
		if (file.isDirectory) {
			if (rec_enum == recursive.yes) {
				index = enumerate_images(file, index, recursive.yes)
			} else {
				enumerate_images(file, 1, recursive.no)
			}
		} else {
			val extension = file.extension.toLowerCase(Locale.ROOT)
			// FIXME: add function for detecting image type
			file.renameTo(File(folder, String.format("%03d.$extension", index)))
			index++
		}
	}
	return index
}
