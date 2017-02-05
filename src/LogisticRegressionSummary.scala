import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.classification.{BinaryLogisticRegressionSummary, LogisticRegression}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
  * Created by hadoop on 17-1-9.
  */
object LogisticRegressionSummary {
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("LogisticRegressionSummaryInHDFS")
      .master("spark://spark-B470:7077")
      .getOrCreate()

    import spark.implicits._
    // Load training data
   // val training = spark.read.format("libsvm").load("/usr/local/spark/data/mllib/sample_libsvm_data.txt")
    val training = spark.read.format("libsvm").load("hdfs://localhost:9000/user/hadoop/input/sample_libsvm_data.txt")

    val lr = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)

    // Fit the model
    val lrModel = lr.fit(training)

    // $example on$
    // Extract the summary from the returned LogisticRegressionModel instance trained in the earlier
    // example
    val trainingSummary = lrModel.summary

    // Obtain the objective per iteration.
    val objectiveHistory = trainingSummary.objectiveHistory
    objectiveHistory.foreach(loss => println(loss))

    // Obtain the metrics useful to judge performance on test data.
    // We cast the summary to a BinaryLogisticRegressionSummary since the problem is a
    // binary classification problem.
    val binarySummary = trainingSummary.asInstanceOf[BinaryLogisticRegressionSummary]

    // Obtain the receiver-operating characteristic as a dataframe and areaUnderROC.
    val roc = binarySummary.roc
    roc.show()
    println(binarySummary.areaUnderROC)

    // Set the model threshold to maximize F-Measure
    val fMeasure = binarySummary.fMeasureByThreshold
    val maxFMeasure = fMeasure.select(max("F-Measure")).head().getDouble(0)
    val bestThreshold = fMeasure.where($"F-Measure" === maxFMeasure)
      .select("threshold").head().getDouble(0)
    lrModel.setThreshold(bestThreshold)
    // $example off$

    spark.stop()

  }
}
