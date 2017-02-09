import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, SVMWithSGD}
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 17-1-9.
  */
object LRWithLBFS {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.ERROR);

    val javaObj = new JavaClass()
    val strList = javaObj.getStrList
    println(strList)

    val conf = new SparkConf()
      .setAppName("LogisticRegressionWithLBFS")
      .setMaster("spark://spark-B470:7077")
    val sc = new SparkContext(conf)


    //    val accum = sc.longAccumulator("My Accumulator")
    //    sc.parallelize(Array(1, 2, 3, 4, 5, 6), 1).foreach(x => accum.add(x))
    //    println(accum.value)

//    println()
//    val collect = sc.parallelize(javaObj.getIntArray.toIndexedSeq).collect()
//    collect.foreach(printX)

//    println()
//    sc.parallelize(javaObj.getIntList.toArray()).collect().foreach(printX)
    println()
    val originData = sc.parallelize(javaObj.getFeatFireStr.toArray)
    originData.collect().foreach(println)
    val originRDD = originData.map(x => x + "").map{line =>
      val splits = line.split(",")
      LabeledPoint(splits(7).toDouble, Vectors.dense(splits(1).toDouble, splits(2).toDouble,
        splits(3).toDouble, splits(4).toDouble, splits(5).toDouble, splits(6).toDouble))
    }

    val splitData = originRDD.randomSplit(Array(0.6, 0.4), seed = 11L)
    val trainData = splitData(0).cache()
    val testData  = splitData(1)
    println("测试集: ")
    trainData.collect().foreach(println)
    println("训练集: ")
    testData.collect().foreach(println)

    /*逻辑回归方法*/
    val featFireModel = new LogisticRegressionWithLBFGS().setNumClasses(2).run(trainData)
    val predictAndLabel = testData.map { case LabeledPoint(label, features) =>
      val prediction = featFireModel.predict(features)
      (prediction, label)
    }
    predictAndLabel.collect().foreach(println)
    val me = new MulticlassMetrics(predictAndLabel)
    println("准确率: " + me.accuracy)


    /*SVM方法*/
    val numIterations = 100
    val SVMModel = SVMWithSGD.train(trainData, numIterations)
    // Clear the default threshold.
    SVMModel.clearThreshold()
    // Compute raw scores on the test set.
    val scoreAndLabels = testData.map { point =>
      val score = SVMModel.predict(point.features)
      (score, point.label)
    }

    scoreAndLabels.collect().foreach(println)
    // Get evaluation metrics.
    val SVMMetrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = SVMMetrics.areaUnderROC()
    println("Area under ROC = " + auROC)



    val data = MLUtils.loadLibSVMFile(sc, "hdfs://localhost:9000/user/hadoop/input/sample_libsvm_data.txt")

    // Split data into training (60%) and test (40%).
    val splits = data.randomSplit(Array(0.3, 0.7), seed = 1L)
    val training = splits(0).cache()
    val test = splits(1)

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(2)
      .run(training)
    // Compute raw scores on the test set.
    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }
    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy
    println(s"Accuracy = $accuracy")


    // Save and load model
    //  model.save(sc, "target/tmp/scalaLogisticRegressionWithLBFGSModel")
    //  val sameModel = LogisticRegressionModel.load(sc,
    //    "target/tmp/scalaLogisticRegressionWithLBFGSModel")


    val points = Array(
      LabeledPoint(0.0, Vectors.dense(0.245)),
      LabeledPoint(0.0, Vectors.dense(0.247)),
      LabeledPoint(1.0, Vectors.dense(0.285)),
      LabeledPoint(1.0, Vectors.dense(0.299)),
      LabeledPoint(1.0, Vectors.dense(0.327)),
      LabeledPoint(1.0, Vectors.dense(0.347)),
      LabeledPoint(0.0, Vectors.dense(0.356)),
      LabeledPoint(1.0, Vectors.dense(0.36)),
      LabeledPoint(0.0, Vectors.dense(0.363)),
      LabeledPoint(1.0, Vectors.dense(0.364)),
      LabeledPoint(0.0, Vectors.dense(0.398)),
      LabeledPoint(1.0, Vectors.dense(0.4)),
      LabeledPoint(0.0, Vectors.dense(0.409)),
      LabeledPoint(1.0, Vectors.dense(0.421)),
      LabeledPoint(0.0, Vectors.dense(0.432)),
      LabeledPoint(1.0, Vectors.dense(0.473)),
      LabeledPoint(1.0, Vectors.dense(0.509)),
      LabeledPoint(1.0, Vectors.dense(0.529)),
      LabeledPoint(0.0, Vectors.dense(0.561)),
      LabeledPoint(0.0, Vectors.dense(0.569)),
      LabeledPoint(1.0, Vectors.dense(0.594)),
      LabeledPoint(1.0, Vectors.dense(0.638)),
      LabeledPoint(1.0, Vectors.dense(0.656)),
      LabeledPoint(1.0, Vectors.dense(0.816)),
      LabeledPoint(1.0, Vectors.dense(0.853)),
      LabeledPoint(1.0, Vectors.dense(0.938)),
      LabeledPoint(1.0, Vectors.dense(1.036)),
      LabeledPoint(1.0, Vectors.dense(1.045)))

    val spiderRDD = sc.parallelize(points)
    val lr = new LogisticRegressionWithLBFGS().setNumClasses(2)
    val predict = lr.run(spiderRDD).predict(Vectors.dense(0.938))
    println("预测结果: " + predict)

    sc.stop()
  }

  def printX(x: Any): Unit = {
    print(x + ", ")
  }
}
