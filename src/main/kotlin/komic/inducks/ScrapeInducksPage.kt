package komic.inducks

import komic.ComicException
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


fun load_inducks_search(query_: String, maxredirects: Int = 10): Connection.Response {
	val query = query_.trim().replace(" ", "%20")
	var counter = 0

	var location = "https://inducks.org/simp.php?d2=$query"
	var response = Jsoup.connect(location).followRedirects(false).execute()
	var new_location = response.header("location")?.replace(" ", "%20") // URLEncoder also replaces // and other characters
	while (new_location != null && new_location != location) {
		if (counter > maxredirects) {
			throw ComicException("Too many redirects (final url: $new_location)")
		}
		counter++
		location = new_location
		// fix location, may contain whitespace instead of %20
		response = Jsoup.connect(location).followRedirects(false).execute()
		new_location = response.header("location")?.replace(" ", "%20")
	}
	return response
}


fun parse_inducks_page(document: Document): inducks {
	val content = document.body().html()

	// FIXME: searching for br is not very robust
	val b_story_code = "Story code"
	val i_story_code = content.indexOf(b_story_code)
	if (i_story_code < 0) {
		throw ComicException("Unable to find \"$b_story_code\" inside HTML document")
	}
	val ib_story_code = content.indexOf("<dd>", i_story_code)
	val ie_story_code = content.indexOf("</dd>", i_story_code)
	val story_code = content.substring(ib_story_code + "<dd>".length, ie_story_code)
			.replace("&nbsp;", " ")
			.replace(160.toChar(), ' ')
			.trim()

	val b_title = "Title"
	val i_title = content.indexOf(b_title, i_story_code)
	if (i_title < 0) {
		throw ComicException("Unable to find \"$b_title\" inside HTML document")
	}
	val ib_title = content.indexOf("<i>", i_title)
	val ie_title = content.indexOf("</i>", i_title)
	val title = content.substring(ib_title + "<i>".length, ie_title).trim()

	return inducks(story_code, title)
}

fun parse_inducks_results(document: Document): List<inducks> {
	val body = document.body()

	val spans = body.getElementsByTag("span")


	if (spans.isEmpty()) {
		return emptyList()
	}


	val attr = spans[0].attr("style")

	val attr_to_find = "background-color:"
	val i_attr = attr.indexOf(attr_to_find)
	val bgcolor = attr.substring(i_attr + attr_to_find.length) // FIXME: as good as fixed

	val table_body = body.getElementsByTag("tbody").last()

	val colored_elements = table_body.getElementsByAttributeValue("bgcolor", bgcolor)

	val toreturn: MutableList<inducks> = mutableListOf()
	for (colored_element in colored_elements) {
		val link = colored_element.getElementsByTag("a")
		val story_code = link.attr("href")
				.replace("story.php?c=", "")
				.replace('+', ' ')
				.replace(160.toChar(), ' ')
				.trim()

		val nextcolumn = link.parents()[1].getElementsByTag("td")[1]

		val title = nextcolumn.text().trim()
		toreturn.add(inducks(story_code, title))
	}
	return toreturn
}


fun do_inducks_search(query: String): List<inducks> {
	val response = load_inducks_search(query)

	val parsedcontent = response.parse()

	val title = parsedcontent.title()

	if (title.contains("Search results")) {
		return parse_inducks_results(parsedcontent)
	}
	return listOf(parse_inducks_page(parsedcontent))
}
