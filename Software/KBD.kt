import isel.leic.utils.Time

object KBD {  // Ler teclas. Métodos retornam ‘0’..’9’,’#’,’*’ ou NONE.
    private const val NONE = 0
    const val DEFAULT_KEY = NONE.toChar()
    private const val KEY = 0x0F
    private const val VAL = 0x80
    private const val ACK = 0x80
    private val keyArray = arrayOf('1','4','7','*','2','5','8','0','3','6','9','#')
    // Inicia a classe
    fun init() {
        HAL.clrBits(ACK)
    }

    // Retorna de imediato a tecla premida ou NONE se não há tecla premida.
    private fun getKey(): Char {
        var key = DEFAULT_KEY
        if (HAL.isBit(VAL) && HAL.readBits(KEY)< keyArray.size) {
            key = keyArray[HAL.readBits(KEY)]
            HAL.setBits(ACK)
            while (HAL.isBit(VAL)) {}
            HAL.clrBits(ACK)
        }
        return key
    }

    // Retorna quando a tecla for premida ou NONE após decorrido ‘timeout’ milisegundos.
    fun waitKey(timeout: Long): Char {
        var key = DEFAULT_KEY
        val timeToStop = Time.getTimeInMillis()+timeout
        while(Time.getTimeInMillis()<timeToStop){
            key = getKey()
            if (key!= DEFAULT_KEY) break
        }
        return key
    }

    fun kbdTest(){ //nao esquecer da parte visual deu certo mais e suposto ver isto
        Time.sleep(2000)
        val result = getKey()
        if (result==DEFAULT_KEY) println("Test is positive if you werent clicking anything") else println("Test is positive if you were clicking $result")
        readLine()

        val result2 = waitKey(5000)
        if (result2==DEFAULT_KEY) println("Test is positive if you werent clicking anything and 5 seconds have elapsed") else println("Test is positive if you were clicking $result2 and it took less than 5 seconds")
        readLine()
    }
}