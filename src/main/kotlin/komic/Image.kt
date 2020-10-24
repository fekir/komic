package komic

import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min

// FIXME: convert to is_white image, save white as color, if 1pixel diff white return false
fun is_monocolor_image(file: File): Boolean {

	val image = ImageIO.read(file) ?: return false
	val first_rgb = image.getRGB(0, 0)

	// not checking all pixels, but the diagonal is probably good enough, and takes 1/nth of the time
	for (x in 0 until min(image.width, image.height)) {
		if (image.getRGB(x, x) != first_rgb) {
			return false
		}
	}

	return true
}
