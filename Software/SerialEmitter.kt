object SerialEmitter {  // Envia tramas para os diferentes módulos Serial Receiver.

    private const val BUSY = 0x40
    private const val SDX = 0x01
    private const val SCLK = 0x02
    private var BITS = 4

    enum class Destination { DISPENSER, LCD }

    // Inicia a classe
    fun init() {
        HAL.setBits(SDX)
        HAL.clrBits(SCLK)
    }

    // Envia uma trama para o SerialReceiver identificado o destino em addr e os bits de dados em ‘data’.

    fun send(addr: Destination, data: Int) {

        while(isBusy());
        var p = 0
        HAL.clrBits(SDX)
        if(Destination.LCD == addr) {
            HAL.setBits(SDX)
            p++
            BITS = 5
        }
        if(Destination.DISPENSER == addr) {
            BITS = 4
        }
        for(i in 0 until BITS){
            val bit = (data shr i) and 0x1
            p += bit
            clockSCLK(bit)
        }
        val parityCheck = p.inv() and 0x1
        clockSCLK(parityCheck)
        clockSCLK(1)
    }

    //muda o valor do bit na posicao SDX
    private fun changeSDXBit (int: Int){
        return when(int){
            1->{
                HAL.setBits(SDX)
            }
            else->{
                HAL.clrBits(SDX)
            }
        }
    }

    // Retorna true se o canal série estiver ocupado
    private fun isBusy(): Boolean {
        return HAL.isBit(BUSY)
    }

    //Realiza um clock, mudando o valor de SDX conforme o valor do bit enviado em parametros durante o tempo entre SCLK ser posto a 1 e voltar para 0.
    private fun clockSCLK(bit:Int) {
        HAL.setBits(SCLK)
        changeSDXBit(bit)
        HAL.clrBits(SCLK)
    }

    fun serialEmitterTest(){
        send(Destination.LCD,0xff)
        println("SR 0-5 ON")
        readLine()

        send(Destination.DISPENSER,0xff)
        println("SR 1-5 ON")
        readLine()
    }
}


