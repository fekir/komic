package komic

import java.io.File

fun open_cli_editor(file_to_edit: File): Int {
	val proc = ProcessBuilder("editor", file_to_edit.name)
			.directory(File(file_to_edit.parent))
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.redirectInput(ProcessBuilder.Redirect.INHERIT)
			.start()
	return proc.waitFor()
}

fun execute_jpegoptim(jpeg_images: List<String>): Int {
	// because of "--strip-all", "exiv2 rm" is not necessary on jpg images
	val command = mutableListOf("jpegoptim", "--strip-all")
	jpeg_images.asSequence().map { img ->
		// prepend "./" to avoid confusion between filenames and params
		if (img.startsWith('-')) {
			"./$img";
		} else {
			img;
		}
	}.toCollection(command)
	val proc = ProcessBuilder(command)
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.start()
	return proc.waitFor()
}

fun execute_optipng(png_images: List<String>): Int {
	// because of "-strip all", "exiv2 rm" is not necessary on png images
	val command = mutableListOf("optipng", "-strip", "all", "--")
	png_images.toCollection(command)
	val proc = ProcessBuilder(command)
		.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		.redirectError(ProcessBuilder.Redirect.INHERIT)
		.start()
	return proc.waitFor()
}

fun execute_exiv_rm(np_jpeg_or_png_images: List<String>): Int {
	val command = mutableListOf("exiv2", "rm")
	np_jpeg_or_png_images.asSequence().map { img ->
		// prepend "./" to avoid confusion between filenames and params
		if (img.startsWith('-')) {
			"./$img";
		} else {
			img;
		}
	}.toCollection(command)
	val proc = ProcessBuilder(command)
		.redirectOutput(ProcessBuilder.Redirect.INHERIT)
		.redirectError(ProcessBuilder.Redirect.INHERIT)
		.start()
	return proc.waitFor()
}
