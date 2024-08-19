object Dispenser {
    fun init(){}
    //Envia informacao á maquina para dispensar o produto desejado por ID
    fun dispense(productId: Int){
        SerialEmitter.send(SerialEmitter.Destination.DISPENSER,productId)
    }
    fun DispenserTEST(){
        dispense(10)
        println("Dispenses product 10")
    }
}