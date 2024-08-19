import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter

object FileAccess {
    private fun createReader(fileName: String): BufferedReader {
        return BufferedReader(FileReader(fileName))
    }

    private fun createWriter(fileName: String): PrintWriter {
        return PrintWriter(fileName)
    }

    fun readFile(fileName: String, size: Int): Array<String?>{
        val array: Array<String?> = arrayOfNulls(size)
        val productFile = createReader(fileName)
        var line: String?
        var count = 0
        line = productFile.readLine()
        while (line != null) {
            if(count < 16) {
                array[count] = line
                count++
            }
            line = productFile.readLine()
        }
        return array
    }

    fun writeFile(fileName: String, array: Array<String>) {
        val write = createWriter(fileName)
        array.forEach {
            if (it != "/")
                write.println(it)
        }
        write.close()
    }
}