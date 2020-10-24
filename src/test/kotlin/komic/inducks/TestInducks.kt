package komic.inducks

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TestInducks {

	// FIXME: test query "asse assassina"
	@Test
	fun test_do_inducks_search_multiple_result_1_correct() {
		val d = do_inducks_search("Zio Paperone snob di societa")
		assertEquals(1, d.size)
		assertEquals("W US   41-02", d[0].story_code)
		assertEquals("Uncle Scrooge The Status Seeker", d[0].title)
	}

	@Test
	fun test_do_inducks_search_redirect_1_result() {
		val d = do_inducks_search("Prigioniera Fosso agonia bianca")
		assertEquals(1, d.size)
		assertEquals("D 2005-061", d[0].story_code)
		assertEquals("Uncle Scrooge The Prisoner Of White Agony Creek", d[0].title)
	}

	@Test
	fun test_do_inducks_search_many_result() {
		val d = do_inducks_search("Paperino in Vacanza")
		assertFalse(d.isEmpty())
	}

	@Test
	fun test_do_inducks_search_0_results() {
		val d = do_inducks_search("prigionieri fosso bianca")
		assertTrue(d.isEmpty())
	}

	@Test
	fun test_search_multiple_result_1_correct() {

		val response = load_inducks_search("Zio Paperone snob di societa")
		//val content = response.body()

		val parsedcontent = response.parse()

		val title = parsedcontent.title()
		assertTrue(title.contains("Search results"))

		val d = parse_inducks_results(parsedcontent)
		assertEquals(1, d.size)
		assertEquals("W US   41-02", d[0].story_code)
		assertEquals("Uncle Scrooge The Status Seeker", d[0].title)
	}

	@Test
	fun test_search_redirect_single_result() {
		val response = load_inducks_search("Roope ja Villin lännen sankarit")
		val parsed = response.parse()
		val i = parse_inducks_page(parsed)

		val title = parsed.title()
		assertTrue(title.contains("Roope ja Villin lännen sankarit"))

		assertEquals("Qfi/RD2006-02B", i.story_code)
		assertEquals("Roope ja Villin lännen sankarit", i.title)
	}

}
