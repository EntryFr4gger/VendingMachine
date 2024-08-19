object CoinDeposit {
    fun depositCoinsOnFile(amount : Int){
        FileAccess.writeFile("CoinDeposit.txt", arrayOf("$amount"))
    }
}