import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 16-11-8.
  */
//object SogouResult {
//  def main(args: Array[String]) {
//    if (args.length == 0) {
//      System.err.println("Usage: SogouResult <file1> <file2>")
//      System.exit(1)
//    }
//
//    val conf = new SparkConf().setAppName("SogouResult").setMaster("local")
//    val sc = new SparkContext(conf)
//
//    //session查询次数排行榜
//    val rdd1 = sc.textFile(args(0)).map(_.split("\t")).filter(_.length == 6)
//    val rdd2 = rdd1.map(x => (x(1), 1)).reduceByKey(_ + _).map(x => (x._2, x._1)).sortByKey(false).map(x => (x._2, x._1))
//    rdd2.saveAsTextFile(args(1))
//    sc.stop()
//  }
//}

object WordCount {
  def main(args: Array[String]): Unit = {
    val appName = "WordCount"
    val conf = new SparkConf().setAppName(appName).setMaster("local")
    val sc = new SparkContext(conf)

    // inputFile和stopWordFile表示了输入文件的路径，请根据实际情况修改
    val inputFiles = "/home/hadoop/words/shakespear/*"
    val stopWordFile = "/home/hadoop/words/stopword.txt"
    val inputRDD = sc.textFile(inputFiles)
    val stopRDD = sc.textFile(stopWordFile)

    val symList = Array[Char]('\t', '(', ')', '.', ',', '!', '?' , '|', '[', ']', '-', ';', ':', ''')

    def replaceAndSplit(s: String): Array[String] = {
      var str = s
      for(c <- symList)
        str = str.replace(c, ' ').toLowerCase()
      str.split("\\s+")
    }

    val inputRDDv1 = inputRDD.flatMap(replaceAndSplit)
    val stopList = stopRDD.map(x => x.trim()).collect()

    val inputRDDv2 = inputRDDv1.filter(x => !(stopList contains x))
    val inputRDDv3 = inputRDDv2.map(x => (x,1))

    val inputRDDv4 = inputRDDv3.reduceByKey(_ + _)
    inputRDDv4.saveAsTextFile("/home/hadoop/words/v4output")

    val inputRDDv5 = inputRDDv4.map(x => x.swap)
    val inputRDDv6 = inputRDDv5.sortByKey(false)
    val inputRDDv7 = inputRDDv6.map(x => x.swap).keys
    val top100 = inputRDDv7.take(100)

    val outputFile = "/home/hadoop/words/result"
    val result = sc.parallelize(top100)
    result.saveAsTextFile(outputFile)
  }
}
