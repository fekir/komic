package komic

import komic.fs.FileNameLengthComparator
import komic.fs.WindowsExplorerFileComparator
import java.io.File
import java.net.URLDecoder
import java.util.*


fun make_file_ext_lowercase(file_name: String): String {
	val i = file_name.lastIndexOf('.')
	if (i > 0) {
		val extension = file_name.substring(i + 1).toLowerCase()
		return file_name.replaceRange(i + 1, file_name.length, extension)
	}
	return file_name
}

fun clean_file_ext(file_name_: String): String {
	return make_file_ext_lowercase(file_name_)
			.replace(Regex("\\.jpeg$"), ".jpg")
}

fun is_image_file_name(file_name_: String, clean : Boolean = true): Boolean {
	val file_name = if(clean) clean_file_ext(file_name_) else file_name_
	return file_name.endsWith(".jpg") || file_name.endsWith(".png") || file_name.endsWith(".gif") || file_name.endsWith(".tif")
}


enum class recursive {
	yes, no
}

fun enumerate_prefix_images(folder: File, prefix: String) {
	val fileList = folder.listFiles()
			.filter { it.isDirectory || is_image_file_name(it.name) }
	Collections.sort(fileList, FileNameLengthComparator)
	for (file in fileList) {
		if (file.isDirectory) {
			enumerate_prefix_images(file, prefix)
		}
		file.renameTo(File(folder, prefix + file.name))
	}
}

fun enumerate_images(folder: File, begin: Int = 1, rec_enum: recursive = recursive.yes): Int {
	enumerate_prefix_images(folder, "ren")

	val fileList = folder.listFiles()
			.filter { it.isDirectory || is_image_file_name(it.name) }

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
			val i = file.name.lastIndexOf('.')
			assert(i > 0)
			val extension = file.name.substring(i + 1).toLowerCase()
			// FIXME: add function for detecting image type
			file.renameTo(File(folder, String.format("%03d.$extension", index)))
			index++
		}
	}
	return index
}
