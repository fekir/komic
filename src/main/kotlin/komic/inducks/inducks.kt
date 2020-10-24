package komic.inducks


data class inducks(val story_code: String, val title: String)

fun format(i: inducks): String {
	return "[${i.story_code}] ${i.title}"
}
