import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by hadoop on 16-11-12.
  */
object SparkStream {
  def main(args: Array[String]): Unit = {
    println("==== SparkStream Test ====")
    val conf = new SparkConf().setAppName("SparkStream").setMaster("local")
    val ssc = new StreamingContext(conf, Seconds(2))
//    val lines = ssc.textFileStream("/home/hadoop/words/shakespear/")
    val lines = ssc.socketTextStream("localhost", 7777)
    val errorLines = lines.filter(_.contains("error"))
    errorLines.print()

    ssc.start() //触发开始流计算
    ssc.awaitTermination()
  }
}
