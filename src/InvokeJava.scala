import java.io.PrintWriter

import scala.io.Source



/**
  * Created by hadoop on 17-1-6.
  */
object InvokeJava {
  def main(args: Array[String]): Unit = {
    val javaClass = new JavaClass()
    var result = javaClass.add(1, 12)
    println("1+12=" + result)
    result = javaClass.sub(100, 12)
    println(result)

    val outFile = "test.txt"
    writeToFile(outFile, "Hello Scala, Scala Call Java API")

    println("ListSize: " + javaClass.getList.size())
    val list = javaClass.getList
    println("size: " + list.size())


//    for (i <- list) {
//      println(i)
//    }
    println(javaClass.getList.get(1).get("Haha"))

    ChecksumAccumulator.calculate("Welcome to Scala Chinese community")
    val ck = new ChecksumAccumulator
    println(ck.checksum())

    val file = Source.fromURL("http://www.baidu.com")
    file.foreach(print)
  }

  def writeToFile(outFile: String, content: String): Unit = {
    val out = new PrintWriter(outFile)
    out.write(content)
    out.close()
  }
}
