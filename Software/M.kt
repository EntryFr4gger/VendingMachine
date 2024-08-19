object M {
    private const val M = 0x10 //I4

    fun isM(): Boolean{
       return HAL.isBit(M)
    }
}