import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 17-2-3.
  */
object DecisionTreeBinary {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.ERROR)

    val sc = new SparkContext(new SparkConf()
      .setAppName("DecisionTreeBinary")
      .setMaster("local[4]"))


    // Load and parse the data file.
    val data = MLUtils.loadLibSVMFile(sc, "hdfs://localhost:9000/user/hadoop/input/sample_libsvm_data.txt")
    // Split the data into training and test sets (30% held out for testing)
    println("样本集总数: " + data.count())
   // data.collect().foreach(println)
    val splits = data.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))

    // Train a DecisionTree model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val successCount = labelAndPreds.filter(r => r._1 == r._2).count()
    val totalCount = testData.count()
    val successRate = successCount.toDouble / totalCount
    println("测试总数: " + totalCount + "; 成功数: " + successCount)
    println("预测成功率 = " + successRate)
    println("Learned classification tree model:\n" + model.toDebugString)

    try {
      // Save and load model
      model.save(sc, "target/tmp/myDecisionTreeClassificationModel")
      val sameModel = DecisionTreeModel.load(sc, "target/tmp/myDecisionTreeClassificationModel")

      println("数据保存成功!")
    } catch {
      case e: Exception =>
        println("输出目录已经存在，请先删除原有目录!")
    }

    sc.stop()
  }
}
