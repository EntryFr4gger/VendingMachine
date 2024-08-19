object CoinAcceptor { // Implementa a interface com o moedeiro.
    private const val COIN_INPUT = 0x20 // I5
    private const val COIN_ACCEPT = 0x10 // O4
    private const val COIN_COLLECT = 0x20 // O5
    private const val COIN_EJECT = 0x40 // O6
    private var coinsInAcceptor = 0
    var COIN_AMOUNT = 0

    // Inicia a classe
    fun init() {
        HAL.clrBits(COIN_ACCEPT)
        HAL.clrBits(COIN_COLLECT)
        HAL.clrBits(COIN_EJECT)
    }
    // Retorna true se foi introduzida uma nova moeda.
    fun hasCoin(): Boolean {
        return HAL.isBit(COIN_INPUT)
    }
    // Informa o moedeiro que a moeda foi contabilizada.
    fun acceptCoin() {
        HAL.setBits(COIN_ACCEPT)
        HAL.clrBits(COIN_ACCEPT)
        coinsInAcceptor++
    }
    // Devolve as moedas que estão no moedeiro.
    fun ejectCoins() {
        HAL.setBits(COIN_EJECT)
        HAL.clrBits(COIN_EJECT)
        coinsInAcceptor = 0
    }
    // Recolhe as moedas que estão no moedeiro.
    fun collectCoins() {
        HAL.setBits(COIN_COLLECT)
        HAL.clrBits(COIN_COLLECT)
        COIN_AMOUNT += coinsInAcceptor
        coinsInAcceptor = 0
    }
}