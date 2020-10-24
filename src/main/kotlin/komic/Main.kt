package komic

import komic.fs.un7zip
import komic.fs.validate_cbz
import komic.fs.zip_cbz
import komic.inducks.do_inducks_search
import komic.inducks.format
import komic.inducks.inducks_cat
import komic.inducks.inducks_clean
import java.awt.Desktop
import java.io.File
import java.io.FileNotFoundException
import java.net.URI

const val opt_inducks = "inducks"
const val opt_inducks_cat = "cat"
const val opt_inducks_trim = "trim"
const val opt_inducks_query = "query"
const val opt_inducks_browse = "browse"
const val opt_inducks_edit = "edit"
const val opt_clean = "clean"
const val opt_enumerate = "enumerate"
const val opt_optimize = "optimize"
const val opt_cbz = "cbz"
const val opt_cbz_extract = "extract"
const val opt_cbz_create = "create"
const val opt_cbz_check = "check"


fun print_help() {
	val usage = "Usage:\n" +
			"  -h, --help, ?: show this help message \n" +
			"\n\n$opt_inducks: This parameters are relative to inducks operations\n" +
			"  $opt_inducks $opt_inducks_cat <list of cbz>:                       cat inducks files inside of cbz\n" +
			"  $opt_inducks $opt_inducks_trim <list of cbz>:                      trim inducks files inside of directory\n" +
			"  $opt_inducks $opt_inducks_query <query>:                           query inducks info online\n" +
			"  $opt_inducks $opt_inducks_browse <story_code>:                     open story code in browser\n" +
			"  $opt_inducks $opt_inducks_edit [--create-after-save] <directory>:  edit inducks value\n" +
			"\n\n$opt_cbz: This parameters are for working with comic books\n" +
			"  $opt_cbz $opt_cbz_create <list of dir>:  create one cbz per directory\n" +
			"  $opt_cbz $opt_cbz_extract <list of cbz>: extract every comic book (cbz, cbr, 7z, and other formats)\n" +
			"  $opt_cbz $opt_cbz_check <list of cbz>:   verifies the cbz (cbz and directories only!)\n" +
			"\n\nOther options:\n" +
			"  $opt_clean <list of dir>:           remove dirty files in every directory\n\n" +
			"  $opt_enumerate [--norec] <list of dir>: enumerate images inside every directory from 1\n" +
			"      if --norec, then the enumeration in subdirectories start from 1 again\n" +
			"  $opt_optimize <list of jpeg files>: optimize images with jpegoptim\n" +
			""
	print(usage)
}

fun cmdline_inducks_cat(files: List<String>) {
	for (file in files) {
		val entries = inducks_cat(File(file))
		if (entries.isEmpty()) {
			println("No entries found in $file")
		}
		for (line in entries) {
			println(line)
		}
	}
}

fun cmdline_inducks_trim(files: List<String>) {
	for (file in files) {
		inducks_clean(File(file))
	}
}

fun cmdline_inducks_query(query: String) {
	val results = do_inducks_search(query)
	if (results.isEmpty()) {
		println("No results found")
	} else {
		File("/tmp/comic_last_query.txt").bufferedWriter().use { fout ->
			results.forEach {
				val to_print = format(it)
				println(to_print)
				fout.write(to_print)
				fout.newLine()
			}
		}
	}
}

fun cmdline_inducks_browse(story_code: String) {
	val desktop = Desktop.getDesktop()
	// desktop.isSupported(Desktop.Action.BROWSE)
	val uri = "https://inducks.org/story.php?c=" + story_code.replace(' ', '+')
	desktop.browse(URI(uri))
}

fun cmdline_inducks_edit(dir: String, create_cbz: Boolean = false) {
	val d = File(dir)
	if (!d.exists() || !d.isDirectory) {
		throw FileNotFoundException("There is no directory \"$d\"")
	}
	val tmp_file = createTempFile("comic_inducks_", ".txt")
	val last_query = File("/tmp/comic_last_query.txt")
	if (last_query.exists()) {
		last_query.copyTo(tmp_file, true)
	}

	val ret = open_cli_editor(tmp_file)
	if (ret == 0 && tmp_file.bufferedReader().readLine() != null) { // saved changes and not empty
		val f = File(d, "inducks.txt")
		try {
			tmp_file.copyTo(f, false)

			// FIXME_ ignore if file empty
			if (create_cbz) {
				cmdline_cbz_create(listOf(dir))
			}
		} catch (ex: FileAlreadyExistsException) {
			println("There is already some file in the directory \"" + f.parent + "\", you can find your edited content at $tmp_file")
			return // do not(!) delete tmp_file or create cbz
		}
	}
	tmp_file.delete() // only if successfully copied
}

fun cmdline_extract(dirs: List<String>) {
	dirs
			.asSequence()
			.map { File(it) }
			.filter { !it.isDirectory }
			.forEach { un7zip(it) }
}

fun cmdline_clean(dirs: List<String>) {
	dirs
			.asSequence()
			.map { File(it) }
			.filter { it.isDirectory }
			.forEach { dir ->
				val hiddenfiles = clean(dir)
				if (!hiddenfiles.isEmpty()) {
					println("Hidden files found:")
					hiddenfiles.forEach { println("\t" + it) }
				}
			}
}

fun cmdline_cbz_create(dirs: List<String>) {
	dirs
			.asSequence()
			.map { File(it) }
			.filter { it.isDirectory }
			.forEach { zip_cbz(it, File(it.name + ".cbz")) }
}

fun cmdline_archive_check(dirs: List<String>) {
	dirs
			.asSequence()
			.map { File(it) }
			.forEach {
				val res = validate_cbz(it)
				if (!res.isEmpty()) {
					System.out.println("Validation for $it failed:")
					System.out.println(res)
				}
			}
}

fun cmdline_enumerate(recursive_: Boolean, dirs: List<String>) {
	val rec = if (recursive_) {
		recursive.no
	} else {
		recursive.yes
	}
	dirs
			.asSequence()
			.map { File(it) }
			.filter { it.isDirectory }
			.forEach { enumerate_images(it, 1, rec) }
}

fun cmdline_optimize(files: List<String>) {
	val jpgs = files.filter { file->
		file.endsWith(".jpg", ignoreCase = true) ||
		file.endsWith(".jpeg", ignoreCase = true);
	}
	if(jpgs.isNotEmpty()) {
		execute_jpegoptim(jpgs)
	}
	val pngs = files.filter { file->
		file.endsWith(".png", ignoreCase = true);
	}
	if(pngs.isNotEmpty()){
		execute_optipng(pngs)
	}
	val other_img = files.filter { file ->
		!file.endsWith(".jpg", ignoreCase = true) &&
		!file.endsWith(".jpeg", ignoreCase = true) &&
		!file.endsWith(".png", ignoreCase = true);
	}
	if(other_img.isNotEmpty()){
		execute_exiv_rm(other_img);
	}
}


fun main(args: Array<String>) {
	if (args.isEmpty() || args[0] == "-h" || args[0] == "?" || args[0] == "--help") {
		print_help()
		return
	}
	if (args.size > 1) {
		if (args[0] == opt_clean) {
			val files = args.drop(1)
			cmdline_clean(files)
			return
		}
		if (args[0] == opt_enumerate) {
			val notrecursive = (args.size > 2) && ((args[1] == "--norec"))

			val files = args.drop(if (notrecursive) {
				2
			} else {
				1
			})
			cmdline_enumerate(notrecursive, files)
			return
		}
		if (args[0] == opt_optimize) {
			cmdline_optimize(args.drop(1))
			return
		}
		if (args.size > 2) {
			if (args[0] == opt_inducks) {
				if (args[1] == opt_inducks_cat) {
					val files = args.drop(2)
					cmdline_inducks_cat(files)
					return
				}
				if (args[1] == opt_inducks_trim) {
					val files = args.drop(2)
					cmdline_inducks_trim(files)
					return
				}
				if (args[1] == opt_inducks_query) {
					if (args.size > 3) {
						println("Unknown parameter: " + args[3])
					}
					cmdline_inducks_query(args[2])
					return
				}
				if (args[1] == opt_inducks_browse) {
					if (args.size > 3) {
						println("Unknown parameter: " + args[3])
					}
					cmdline_inducks_browse(args[2])
					return
				}
				if (args[1] == opt_inducks_edit) {
					val create_cbz = (args.size > 3 && args[2] == "--create-after-save")
					if (!create_cbz && args.size > 3) {
						println("Unknown parameter: " + args[3])
						return
					}
					cmdline_inducks_edit(args[if (create_cbz) {
						3
					} else {
						2
					}], create_cbz)
					return
				}
			}
			if (args[0] == opt_cbz) {
				if (args[1] == opt_cbz_extract) {
					val files = args.drop(2)
					cmdline_extract(files)
					return
				}
				if (args[1] == opt_cbz_create) {
					val files = args.drop(2)
					cmdline_cbz_create(files)
					return
				}
				if (args[1] == opt_cbz_check) {
					cmdline_archive_check(args.drop(2))
					return
				}
			}
		}
	}

	println("No sensible arguments passed")
	print_help()
}
