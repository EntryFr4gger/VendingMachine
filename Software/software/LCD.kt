import isel.leic.utils.Time

object LCD {
    // Dimensão do display.
    const val LINES = 2
    const val COLS = 16
    private const val E_MASK = 0x20 // 00100000
    private const val RS_MASK = 0x10 //00010000
    private const val DATA_BITS_MASK = 0x0F //LCD data bits
    private const val SERIAL_MODE = true //modo de envio de informacao do LCD
    private const val FUNCTION_SET_SPECIAL4_INTERFACE_8BITS = 0x3
    private const val FUNCTION_SET_SPECIAL4_INTERFACE_4BITS = 0x2
    private const val FUNCTION_SET_INTERFACE_4BITS_2LINE_DISPLAY_FONT_5X7_DOTS = 0x28
    private const val DISPLAY_OFF = 0x8
    private const val ENTRY_MODE_INCREMENT_CURSOR_NO_DISPLAY_SHIFT = 0X6
    private const val DISPLAY_ON_CURSOR_ON_BLINK_CURSOR_ON = 0X0F
    private const val SET_DDRAM_ADDRESS = 0x80
    private const val CLEAR_DISPLAY = 0x01
    private const val DDRAM_ADDRESS_NEXT_LINE = 0x40
    private const val HALF_BYTE = 4


    fun writeCostumCharToCGRAM(array: Array<Int>,posiValue: Int){
        writeCMD(0x40+posiValue*8)
        repeat(8){
            writeDATA(array[it])
        }
        cursor(0,0)
    }


    // Envia a sequência de iniciação para comunicação a 4bits.
    fun init(){
        Time.sleep(15)
        writeNibble(false,FUNCTION_SET_SPECIAL4_INTERFACE_8BITS)
        Time.sleep(5)
        writeNibble(false,FUNCTION_SET_SPECIAL4_INTERFACE_8BITS)
        Time.sleep(1)
        writeNibble(false,FUNCTION_SET_SPECIAL4_INTERFACE_8BITS)
        writeNibble(false,FUNCTION_SET_SPECIAL4_INTERFACE_4BITS)
        writeCMD(FUNCTION_SET_INTERFACE_4BITS_2LINE_DISPLAY_FONT_5X7_DOTS)
        writeCMD(DISPLAY_OFF)
        writeCMD(CLEAR_DISPLAY)
        writeCMD(ENTRY_MODE_INCREMENT_CURSOR_NO_DISPLAY_SHIFT)
        writeCMD(DISPLAY_ON_CURSOR_ON_BLINK_CURSOR_ON)
    }

    // Escreve um nibble de comando/dados no LCDprivate
    private fun writeNibble(rs: Boolean, data: Int){
        when(SERIAL_MODE){
            true-> writeNibbleSerial(rs,data)
            else-> writeNibbleParallel(rs,data)
        }
    }

    // Faz o efeito do writeNibble, mas em serie
    private fun writeNibbleSerial(rs: Boolean, data: Int){
        val rsAndData = data shl 1 or if(rs) 1 else 0
        SerialEmitter.send(SerialEmitter.Destination.LCD,rsAndData)
    }

    // Faz o efeito do writeNibble, mas em paralelo
    private fun writeNibbleParallel(rs: Boolean, data: Int){
        //e,rs,outro
        val rsBit = if(rs) RS_MASK else 0
        HAL.writeBits(RS_MASK,rsBit)
        Time.sleep(1)
        HAL.writeBits(RS_MASK+E_MASK,rsBit + E_MASK)
        Time.sleep(1)
        HAL.writeBits(RS_MASK+E_MASK+DATA_BITS_MASK,rsBit+E_MASK+data)
        Time.sleep(1)
        HAL.clrBits(E_MASK)
    }

    // Escreve um byte de comando/dados no LCDprivate
    private fun writeByte(rs: Boolean, data: Int){
        writeNibble(rs,data shr HALF_BYTE)
        writeNibble(rs,(data and DATA_BITS_MASK))
    }

    // Escreve um comando no LCD
    private fun writeCMD(data: Int) = writeByte(false,data)

    // Escreve um dado no LCD
    private fun writeDATA(data: Int) = writeByte(true,data)

    // Escreve um caráter na posição corrent.toInt() -> .code
    fun write(c: Char) = writeDATA(c.code)

    fun write(i: Int) = writeDATA(i)

    // Escreve uma string na posição corrente.
    fun write(text: String){
        for(element in text) { write(element) }
    }

    // Envia comando para posicionar cursor (‘line’:0..LINES-1 , ‘column’:0..COLS-1)
    fun cursor(line: Int,column: Int) = writeCMD(SET_DDRAM_ADDRESS+column+(if(line!=0)line*DDRAM_ADDRESS_NEXT_LINE else 0))

    // 1.52ms (return home) + 37 microseconds * 0x80 (total cells), worst case (for the others instructions +1ms)~5.48
    fun clear() {writeCMD(CLEAR_DISPLAY)
    Time.sleep(6)}

    fun lcdTest(){ //nao esquecer da parte visual deu certo mais e suposto ver isto
        write("Muito Divertido")
        println("Muito divertido deve aparecer escrito no ecrã")
        readLine()

        cursor(1,7)
        println("Deve aparecer o cursor a piscar na segunda linha, meio do ecrã")
        readLine()

        clear()
        println("Deve limpar o ecra e aparecer o cursor a piscar na posicao 0 linha 0")
        readLine()
    }
}