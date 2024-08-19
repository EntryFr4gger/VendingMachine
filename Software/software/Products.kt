object Products{

    data class Product(val id: Int, val name: String, var amount: Int, var cost: Double)

    const val PRODUCT_NA = "Not Available"

    fun fetchProduct(keys: Int, arrProducts: Array<Product?>,makePointer:Boolean): Product? {
        return if(keys in 0..15 &&  arrProducts[keys] != null){
            if(makePointer) arrProducts[keys]!! else arrProducts[keys]!!.copy()
        }
        else {
            null
        }
    }

    fun readFileProducts(): Array<Product?>{
        val array = FileAccess.readFile("PRODUCTS.txt", 16)
        val newArray: Array<Product?> = arrayOfNulls(16)
        array.forEach {
            if(it != null){
                val li = it.split(";")
                newArray[li[0].toInt()] = Product(li[0].toInt(), li[1], li[2].toInt(), li[3].toDouble())
            }
        }
        return newArray
    }

    private fun Product.toStrings(): String = "$id;$name;$amount;$cost"

    fun writeFileProducts(arrProducts: Array<Product?>){
        println(arrProducts.asList())
        val array: Array<String> = Array(arrProducts.size) { "/" }
        var count = 0
        arrProducts.forEach {
            if (it != null)
                array[count] = it.toStrings()
            count++
        }
        println(array.asList())
        FileAccess.writeFile("PRODUCTS.txt", array)
    }
}