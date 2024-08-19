import isel.leic.utils.Time
import java.text.SimpleDateFormat
import java.util.*

object TUI {

    enum class AlignSide { LEFT, RIGHT, CENTER }

    enum class SpecialChar { EURO, ARROW }

    private val SpecialCharMap = mapOf(SpecialChar.EURO to 0, SpecialChar.ARROW to 1)
    private val EURO = (arrayOf(0x07, 0x08, 0x1E, 0x10, 0x1E, 0x08, 0x07, 0x00))
    private val ARROW = (arrayOf(0x04, 0x0E, 0x1F, 0x04, 0x1F, 0x0E, 0x04, 0x00))
    private val keyArray = arrayOf(EURO, ARROW)

    private const val dateHeight = 1

    fun init() {
        keyArray.forEach { LCD.writeCostumCharToCGRAM(it, keyArray.indexOf(it)) }
    }

    private fun writeCustomChar(specialChar: SpecialChar) {
        SpecialCharMap[specialChar]?.let { LCD.write(it) }
    }

    //Add condition for what line
    //adicionar para quando excede o numero de letras dar slide
    fun writeAligned(
        text: String,
        alignHeight: Int = 0,
        alignSide: AlignSide = AlignSide.LEFT,
        specialChar: SpecialChar? = null
    ) {
        when (alignSide) {
            AlignSide.RIGHT -> {
                writeAlignedSupport(
                    text,
                    alignHeight,
                    specialChar,
                    LCD.COLS - text.length - if (specialChar != null) 1 else 0
                )
            }
            AlignSide.LEFT -> {
                writeAlignedSupport(text, alignHeight, specialChar, 0)
            }
            AlignSide.CENTER -> {
                writeAlignedSupport(
                    text,
                    alignHeight,
                    specialChar,
                    (LCD.COLS - text.length - if (specialChar != null) 1 else 0) / 2
                )
            }
        }
    }

    private fun writeAlignedSupport(text: String, alignHeight: Int = 0, specialChar: SpecialChar? = null, column: Int) {
        LCD.cursor(if (alignHeight in 0..LCD.LINES) alignHeight else LCD.LINES, column)
        LCD.write(text)
        if (specialChar != null) writeCustomChar(specialChar)
    }

    fun getKeyboardInput(timeout: Long): Char? {
        val value = KBD.waitKey(timeout)
        return if (value == KBD.DEFAULT_KEY) null else value
    }

    fun clearLineLCD(line: Int) {
        LCD.cursor(line, 0)
        repeat(LCD.COLS) { LCD.write(' ') }
        LCD.cursor(line, 0)
    }

    fun clearLCD() = LCD.clear()

    private fun getDate(milliSeconds: Long, dateFormat: String): String? {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun writeDoubleCentered(line1:String, line2:String){
        clearLCD()
        writeAligned(line1, 0, AlignSide.CENTER)
        writeAligned(line2, 1,AlignSide.CENTER)
    }
    fun writeYesNoScreen(action: String) {
        clearLCD()
        writeAligned(action, 0, AlignSide.RIGHT)
        writeAligned("Yes-5", 1)
        writeAligned("No-8", 1, AlignSide.RIGHT)
    }

    fun writeCollectScreen() {
        clearLineLCD(1)
        writeAligned("Collect Products", 1, AlignSide.CENTER)
    }

    fun writeProductBuyScreen(product: Products.Product) {
        clearLineLCD(1)
        writeAligned("${product.cost}", 1, AlignSide.CENTER)
        writeCustomChar(SpecialChar.EURO)
    }

    fun writeProductSelectScreen(keysValue: Int, arrowMove: Boolean, product: Products.Product?) {
        clearLCD()
        if (product == null || product.amount == 0) {
            writeAligned(Products.PRODUCT_NA, 0, AlignSide.CENTER)
            writeAligned(
                keysValue.toString(),
                1,
                AlignSide.CENTER,
                if (arrowMove) SpecialChar.ARROW else null
            )
        } else {
            writeAligned(product.name, 0, AlignSide.CENTER)//
            writeAligned("${product.cost}", 1, AlignSide.RIGHT, SpecialChar.EURO)
            writeAligned(
                keysValue.toString().padStart(2, '0'),
                1,
                AlignSide.LEFT,
                if (arrowMove) SpecialChar.ARROW else null
            )
            writeAligned("#${product.amount.toString().padStart(2, '0')}", 1, AlignSide.CENTER)
        }
    }

    fun writeDefaultScreen(): Long {
        LCD.clear()
        writeAligned("Vending Machine", 0, AlignSide.CENTER)
        writeDateTime()
        return Time.getTimeInMillis() / 60000
    }

    fun writeDateTime() {
        writeAligned(getDate(Time.getTimeInMillis(), "dd/MM/yyyy hh:mm:ss.SSS")!!, dateHeight)
    }
}