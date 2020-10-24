package komic.fs

import java.io.File
import java.util.Comparator

/// Use to sort file names by length
/// implemented as object since stateless
object FileNameLengthComparator : Comparator<File> {
	override fun compare(o1: File, o2: File): Int {
		// no need to use if-else because in practice filenames can't be long enough to cause overflows
		return o2.name.length - o1.name.length
	}
}
