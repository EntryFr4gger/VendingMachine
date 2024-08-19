import isel.leic.UsbPort

object HAL {   // Virtualiza o acesso ao sistema UsbPort
    // Inicia a classe
    private var usbPortOut = 0x00
    fun init() {
        usbPortOut = 0x00
        doOutput()
    }

    // Retorna true se o bit tiver o valor lógico ‘1’, tem teste
    fun isBit(mask: Int): Boolean = readBits(mask) == mask

    // Retorna os valores dos bits representados por mask presentes no UsbPort, tem teste
    fun readBits(mask: Int): Int = (getInput() and mask)

    // Escreve nos bits representados por mask o valor de value, tem teste
    fun writeBits(mask: Int, value: Int) {
        usbPortOut = (usbPortOut and mask.inv()) or (value and mask)
        doOutput()
    }

    // Coloca os bits representados por mask no valor lógico ‘1’, tem teste
    fun setBits(mask: Int) {
        usbPortOut = usbPortOut or mask
        doOutput()
    }

    // Coloca os bits representados por mask no valor lógico ‘0’, tem teste
    fun clrBits(mask: Int) {
        usbPortOut = usbPortOut and mask.inv()
        doOutput()
    }

    private fun doOutput() = UsbPort.out(usbPortOut.inv())

    private fun getInput() : Int {
        UsbPort.`in`()
        return UsbPort.`in`().inv()
    }

    fun halTest(){ //nao esquecer da parte visual deu certo mais e suposto ver isto
        writeBits(0xFF,0x55)
        if(usbPortOut == 0x55) (println("test 1 positive")) else println("test 1 negative")
        println("bit output should be 01010101")
        readLine()

        val readInput = readBits(0xff)
        if(UsbPort.`in`().inv() and 0xff == readInput) (println("test 2 positive")) else println("test 2 negative")
        println("all input bits should be $readInput")
        readLine()

        val readBit = isBit(0x00)
        if(readBit) (println("test 3 positive")) else println("test 3 negative")
        println("lowest value bit should be$readBit")
        readLine()

        var complicado = usbPortOut
        setBits(0x08)
        if(usbPortOut==complicado+0x08) (println("test 4 positive")) else println("test 4 negative")
        println("after O3 is set to positive")
        readLine()

        complicado = usbPortOut
        clrBits(0x08)
        if(usbPortOut==complicado-0x08) (println("test 5 positive")) else println("test 5 negative")
        println("O3 should be cleared")
        readLine()
    }
}