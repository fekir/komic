package komic.fs

import java.io.File
import java.util.ArrayList
import java.util.Comparator
import java.util.regex.Pattern

object WindowsExplorerFileComparator : Comparator<File> {
	override fun compare(o1: File, o2: File): Int {
		return WindowsExplorerComparator.compare(o1.name, o2.name)
	}
}

/// Sort strings similar to windows explorer
/// Implemented as object since stateless (apart from the pattern that does no mutate)
/// Source: https://stackoverflow.com/questions/23205020/java-sort-strings-like-windows-explorer
object WindowsExplorerComparator : Comparator<String> {

	override fun compare(str1: String, str2: String): Int {
		val i1 = splitStringPreserveDelimiter(str1).iterator()
		val i2 = splitStringPreserveDelimiter(str2).iterator()
		while (true) {
			//Til here all is equal.
			if (!i1.hasNext() && !i2.hasNext()) {
				return 0
			}
			//first has no more parts -> comes first
			if (!i1.hasNext() && i2.hasNext()) {
				return -1
			}
			//first has more parts than i2 -> comes after
			if (i1.hasNext() && !i2.hasNext()) {
				return 1
			}

			val data1 = i1.next()
			val data2 = i2.next()
			var result: Int
			try {
				//If both datas are numbers, then compare numbers
				result = java.lang.Long.compare(java.lang.Long.valueOf(data1), java.lang.Long.valueOf(data2))
				//If numbers are equal than longer comes first
				if (result == 0) {
					result = -Integer.compare(data1.length, data2.length)
				}
			} catch (ex: NumberFormatException) {
				//compare text case insensitive
				result = data1.compareTo(data2, ignoreCase = true)
			}

			if (result != 0) {
				return result
			}
		}
	}

	private val splitPattern = Pattern.compile("\\d+|\\.|\\s")!!

	private fun splitStringPreserveDelimiter(str: String): List<String> {
		val matcher = splitPattern.matcher(str)
		val list = ArrayList<String>()
		var pos = 0
		while (matcher.find()) {
			list.add(str.substring(pos, matcher.start()))
			list.add(matcher.group())
			pos = matcher.end()
		}
		list.add(str.substring(pos))
		return list
	}
}

