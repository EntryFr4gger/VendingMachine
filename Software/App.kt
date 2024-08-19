import isel.leic.utils.Time
import kotlin.math.pow
import kotlin.system.exitProcess

//full interlock na situacao da moeda entrar, para pagar
//Mudar a forma como as keys funcionam para a situacao do v=v*10+key (modulo de max)

fun main(args: Array<String>){
    App.app()
}

object App {
    //Faltam mambos do coin accept
    private const val ONLY_COIN_IN_THE_WORLD = 0.50
    private const val NUMBER_OF_DIGITS = 2
    private const val SELECT_TIMEOUT = 7000
    private const val MAXIMUM_NUM_OF_PRODUCTS = 15 //starts at 0 to


    private var keysValue: Int = 0
    private var lastKeyTime: Long = Time.getTimeInMillis()
    private var lastTD: Long = Time.getTimeInMillis() / 60000
    private var productArray: Array<Products.Product?> = Products.readFileProducts()
    private var arrowMove = false
    private var currentKey: Char? = null


    //very intressante

    private fun init() {
        HAL.init()
        KBD.init()
        SerialEmitter.init()
        LCD.init()
        Dispenser.init()
        CoinAcceptor.init()
        TUI.init()

    }

    fun app() {
        init()
        TUI.writeDefaultScreen()
        while (true) {
            if (M.isM()) {
                maintenace()
                TUI.writeDefaultScreen()
            }
            updateKeys(50, true)
            if (currentKey == '#') {
                productDispense()
            } else {
                updateDateTime()
            }
        }
    }

    private fun productDispense() {
        currentKey = null
        val currentProduct = getKeysReturnProduct()
        if (currentKey == '#' && (currentProduct != null) && (currentProduct.amount >0)) {
            buyProduct(currentProduct)
            if (currentKey == '#') {
                cancelBuy()
            }
        }
        if ((currentProduct != null) && (currentProduct.cost == 0.00)) productBought(currentProduct)
        resetToDefault(true, true)
    }

    private fun buyProduct(currentProduct: Products.Product) {
        TUI.writeProductBuyScreen(currentProduct)
        currentKey = null
        while (currentKey != '#') {
            if (CoinAcceptor.hasCoin()) {
                CoinAcceptor.acceptCoin()
                currentProduct.cost -= ONLY_COIN_IN_THE_WORLD
                if (currentProduct.cost == 0.00) {
                    break
                }
                TUI.writeProductBuyScreen(currentProduct)
            }
            updateKeys(50)
        }
    }

    private fun cancelBuy() {
        TUI.writeDoubleCentered("Operation","Canceled")
        Time.sleep(1000)
        resetToDefault(true, true)
        CoinAcceptor.ejectCoins()
    }

    private fun productBought(currentProduct: Products.Product) {
        TUI.writeCollectScreen()
        productArray[currentProduct.id]!!.amount -= 1
        Dispenser.dispense(currentProduct.id)
        TUI.writeDoubleCentered("Thank you","see you again")
        Time.sleep(1000)
        resetToDefault(true, true)
        CoinAcceptor.collectCoins()
    }

    private fun updateKeys(timeout: Long, justCurrentKey: Boolean = false) {
        currentKey = TUI.getKeyboardInput(timeout)
        if (!justCurrentKey) {
            if (arrowMove) updateKeysForWalk() else updateKeysForNum()
        }
        if (currentKey != null) updateLastInput()
    }

    private fun updateKeysForNum() {
        if (currentKey != null) {
            val keyInt = Character.getNumericValue(currentKey!!)
            if (keyInt in 0..10) {
                keysValue = (((keysValue * 10 + keyInt) % (10.0.pow(NUMBER_OF_DIGITS.toDouble()))).toInt())
            }
        }
    }

    private fun updateKeysForWalk() {
        if (currentKey == '2') {
            if (keysValue < MAXIMUM_NUM_OF_PRODUCTS) {
                do {
                    keysValue += 1
                } while (productArray[keysValue] == null)
            } else {
                keysValue =
                    MAXIMUM_NUM_OF_PRODUCTS
            }
            updateLastInput()
        }
        if (currentKey == '8') {
            if (keysValue > 0) {
                do {
                    keysValue -= 1
                } while (productArray[keysValue] == null)
            } else {
                keysValue = 0
            }
            updateLastInput()
        }
    }

    private fun getKeysReturnProduct(): Products.Product? {
        var currentProduct = Products.fetchProduct(keysValue, productArray, false)
        TUI.writeProductSelectScreen(keysValue, arrowMove,currentProduct)
        var oldKeys: Int
        while (currentKey != '#' && !resetToDefault(false)) {
            oldKeys = keysValue
            updateKeys(50)
            currentProduct = Products.fetchProduct(keysValue, productArray, false)
            if (currentKey == '*') {
                arrowMove = !arrowMove
                if(keysValue> MAXIMUM_NUM_OF_PRODUCTS) keysValue= MAXIMUM_NUM_OF_PRODUCTS
            }
            if (!(oldKeys == keysValue && currentKey != '*')) {
                TUI.writeProductSelectScreen(keysValue, arrowMove,currentProduct)
            }
        }
        return currentProduct
    }

    private fun resetToDefault(justReset: Boolean, resetScreen: Boolean = false): Boolean {
        if (lastKeyTime + SELECT_TIMEOUT <= Time.getTimeInMillis() || justReset) {
            keysValue = 0
            currentKey = null
            updateLastInput()
            if (resetScreen) TUI.writeDefaultScreen()
            return true
        }
        return false
    }

    private fun updateLastInput() {
        lastKeyTime = Time.getTimeInMillis()
    }

    private fun updateDateTime() {
        if (Time.getTimeInMillis() / 60000 != lastTD) {
            lastTD = Time.getTimeInMillis() / 60000
            TUI.writeDateTime()
        }
    }

    private fun maintenace() {
        while (M.isM()) {
            var key: Char?
            while (true) {
                TUI.clearLCD()
                TUI.writeAligned("Maintenance Mode")
                TUI.writeAligned("1-Dispense Test", 1)
                key = TUI.getKeyboardInput(1000)
                if (key != null || !M.isM()) break
                TUI.clearLineLCD(1)
                TUI.writeAligned("2-Update Prod.", 1)
                key = TUI.getKeyboardInput(1000)
                if (key != null || !M.isM()) break
                TUI.clearLineLCD(1)
                TUI.writeAligned("3-Remove Prod.", 1)
                key = TUI.getKeyboardInput(1000)
                if (key != null || !M.isM()) break
                TUI.clearLineLCD(1)
                TUI.writeAligned("4-Shutdown", 1)
                key = TUI.getKeyboardInput(1000)
                if (key != null || !M.isM()) break
            }
            when (key) {
                '1' -> maintenanceDispenseTest()
                '2' -> maintenanceUpdateProd()
                '3' -> maintenanceRemoveProd()
                '4' -> maintenanceShutdown()
            }
        }
    }

    private fun maintenanceDispenseTest() {
        TUI.writeDoubleCentered("Test","Choose Product")
        val currentProduct = getKeysReturnProduct()
        if (currentKey == '#' && currentProduct != null && currentProduct.amount!=0) {
            productBought(currentProduct)
        }
        resetToDefault(true)
    }

    private fun maintenanceUpdateProd() {
        TUI.writeDoubleCentered("Update","Choose Product")
        val currentProduct: Products.Product? = getKeysReturnProduct()
        if (currentKey == '#' && currentProduct != null) {
            TUI.clearLCD()
            resetToDefault(true, false)
            while (true) {
                TUI.writeDoubleCentered(currentProduct.name,keysValue.toString().padStart(2, '0'))
                updateKeys(5000)
                if (currentKey == '#') {
                    productArray[currentProduct.id]!!.amount = currentProduct.amount + keysValue
                    break
                }
            }
        }
        resetToDefault(true)
    }


    private fun maintenanceRemoveProd() {
        TUI.writeDoubleCentered("Remove","Choose Product")
        val currentProduct: Products.Product? = getKeysReturnProduct()
        if (currentKey == '#' && currentProduct != null) {
            TUI.writeYesNoScreen("Remove product")
            while (currentKey == '#') {
                when (TUI.getKeyboardInput(1000)) {
                    '5' -> {
                        productArray[currentProduct.id] = null
                        break
                    }
                    '8' -> break
                }
            }
        }
        resetToDefault(true)
    }

    private fun maintenanceShutdown() {
        TUI.writeYesNoScreen("Shutdown")
        while (true) {
            when (TUI.getKeyboardInput(5000)) {
                '5' -> {
                    LCD.clear()
                    LCD.write("Bye Bye")
                    Time.sleep(500)
                    Products.writeFileProducts(productArray)
                    CoinDeposit.depositCoinsOnFile(CoinAcceptor.COIN_AMOUNT)
                    exitProcess(0)
                }
                else -> break
            }
        }
    }
}