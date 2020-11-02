package komic

import komic.fs.un7zip
import komic.fs.zip_cbz
import komic.fs.zip_epub
import java.io.File
import java.util.*

fun open_cli_editor(file_to_edit: File): Int {
	val proc = ProcessBuilder("editor", file_to_edit.name)
			.directory(File(file_to_edit.parent))
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.redirectInput(ProcessBuilder.Redirect.INHERIT)
			.start()
	return proc.waitFor()
}

fun execute_jpegoptim(jpeg_images: List<File>): Int {
	if(jpeg_images.isEmpty()){
		return 0
	}
	// because of "--strip-all", "exiv2 rm" is not necessary on jpg images
	val command = mutableListOf("jpegoptim", "--strip-all")
	jpeg_images.asSequence().map { it.absolutePath }.toCollection(command)
	val proc = ProcessBuilder(command)
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.start()
	return proc.waitFor()
}

fun execute_optipng(png_images: List<File>): Int {
	if(png_images.isEmpty()){
		return 0
	}
	// because of "-strip all", "exiv2 rm" is not necessary on png images
	val command = mutableListOf("optipng", "-strip", "all", "--")
	png_images.asSequence().map { it.absolutePath }.toCollection(command)
	val proc = ProcessBuilder(command)
		.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		.redirectError(ProcessBuilder.Redirect.INHERIT)
		.start()
	return proc.waitFor()
}

fun execute_exiv2_rm(np_jpeg_or_png_images: List<File>): Int {
	if(np_jpeg_or_png_images.isEmpty()){
		return 0
	}
	val command = mutableListOf("exiv2", "rm")
	np_jpeg_or_png_images.asSequence().map { it.absolutePath }.toCollection(command)
	val proc = ProcessBuilder(command)
		.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		.redirectError(ProcessBuilder.Redirect.INHERIT)
		.start()
	return proc.waitFor()
}

fun optimize_files(files_ : List<File>) {
	var files : List<File> = files_.distinct().flatMap { it.walk().toList() }.filter { it.isFile }.distinct()
	if(files.isEmpty()){
		return
	}
	files = run {
		val (cbzs, files1) = files.partition { file ->
			file.extension.toLowerCase(Locale.ROOT) == "cbz"
		}
		val cbzsdirs = cbzs.map { cbz ->
			val dir = File(cbz.parent, cbz.nameWithoutExtension)
			println(dir)
			un7zip(cbz)
			dir
		}
		optimize_files(cbzsdirs)
		val cbzs_old = cbzs.map { val newfile = File(it.parent, it.name + ".back"); it.renameTo(newfile); newfile }
		val cbzs_new = cbzsdirs.map { val newfile = File(it.absolutePath + ".cbz"); zip_cbz(it, newfile); newfile }
		cbzsdirs.forEach { it.deleteRecursively() }
		files1
	}
	files = run {
		val (epubs, files1) = files.partition { file ->
			file.extension.toLowerCase(Locale.ROOT) == "epub"
		}
		val epubsdirs = epubs.map { epub ->
			val dir = File(epub.parent, epub.nameWithoutExtension)
			un7zip(epub)
			dir
		}
		optimize_files(epubsdirs)
		val epubs_old = epubs.map { val newfile = File(it.parent, it.name + ".back"); it.renameTo(newfile); newfile }
		val epubs_new = epubsdirs.map { val newfile = File(it.absolutePath + ".epub"); zip_epub(it, newfile); newfile }
		epubsdirs.forEach { it.deleteRecursively() }
		files1
	}
	files = run {
		val (jpgs, files1) = files.partition { file ->
			val ext = file.extension.toLowerCase(Locale.ROOT)
			ext == "jpg" || ext == "jpeg"
		}
		execute_jpegoptim(jpgs)
		files1
	}
	files = run {
		val (pngs, files1) = files.partition { file ->
			file.extension.toLowerCase(Locale.ROOT) == "png"
		}
		execute_optipng(pngs)
		files1
	}
	files = run {
		val (otherimg, files1) = files.partition { file ->
			val ext = file.extension.toLowerCase(Locale.ROOT)
			// formats supported by exiv2
			ext == "git" || ext == "bmp" || ext == "tiff"
		}
		execute_exiv2_rm(otherimg)
		files1
	}
}
