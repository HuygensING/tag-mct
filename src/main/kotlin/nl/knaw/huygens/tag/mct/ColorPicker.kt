package nl.knaw.huygens.tag.mct

class ColorPicker(vararg colors: String) {

    private val _colors = arrayOf(*colors)
    private var i = 0

    val nextColor: String
        get() {
            val color = _colors[i]
            i = if (i < _colors.size - 1) i + 1 else 0
            return color
        }

    fun reset() {
        i = 0
    }

}