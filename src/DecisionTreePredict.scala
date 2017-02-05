import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.jfree.data.category.DefaultCategoryDataset
import org.joda.time.{DateTime, Duration}


/**
  * Created by hadoop on 17-2-4.
  */
object DecisionTreePredict {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.ERROR)

    val sc = new SparkContext(new SparkConf()
      .setAppName("DecisionTreePredict")
      .setMaster("local[4]"))

    println("RunDecisionTreePredict")
    println("============数据准备阶段==============")
    val (trainData, validationData, testData, categoriesMap) = PrepareData(sc)
    trainData.persist(); validationData.persist(); testData.persist()
    println("============训练评估阶段==============")
    println()
    print("是否需要进行参数调校 (Y:是  N:否) ? ")
    if (readLine() == "Y") {
      val model = parametersTunning(trainData, validationData)
      println("==========测试阶段===============")
      val auc = evaluateModel(model, testData)
      println("使用testata测试最佳模型,结果 AUC:" + auc)
      println("==========预测数据===============")
      PredictData(sc, model, categoriesMap)
    } else {
      val model = trainEvaluate(trainData, validationData)
      println("==========测试阶段===============")
      val auc = evaluateModel(model, testData)
      println("使用testData测试最佳模型,结果 AUC:" + auc)
      println("==========预测数据===============")
      PredictData(sc, model, categoriesMap)
    }


//    val model = trainEvaluate(trainData, validationData)
//    println("============测试   阶段==============")
//    val auc = evaluateModel(model, testData)
//    println("使用testData测试最佳模型，结果AUC:" + auc)
//    println("============预测数据阶段==============")
//    PredictData(sc, model, categoriesMap)
//    trainData.unpersist(); validationData.unpersist(); testData.unpersist()

    sc.stop()
  }

  def PrepareData(sc: SparkContext): (RDD[LabeledPoint],
    RDD[LabeledPoint], RDD[LabeledPoint], Map[String, Int]) = {
    //-----------1. 导入/转换数据 --------------
    println("开始导入数据...")
    val rawDataWithHeader = sc.textFile("data/train.tsv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex {
      (idx, iter) => if (idx == 0) iter.drop(1) else  iter
    }
    val lines = rawData.map(_.split("\t"))
    println("共计: " + lines.count().toString + " 条")
    //-----------2. 创建训练评估所需数据 --------------
    val categoriesMap = lines.map(fields => fields(3)).distinct.collect().
      zipWithIndex.toMap
    val labelpointRDD = lines.map { fields =>
      val trFields = fields.map(_.replaceAll("\"", ""))
      val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
      val categoryIdx = categoriesMap(fields(3))
      categoryFeaturesArray(categoryIdx) = 1
      val numericalFeatures = trFields.slice(4, fields.size - 1)
        .map(d => if (d == "?") 0.0 else d.toDouble)
      val label = trFields(fields.size - 1).toDouble
      val Arra = categoryFeaturesArray ++ numericalFeatures
      LabeledPoint(label, Vectors.dense(Arra))
    }
    //-----------3. 以随机方式将数据分为3个部分 --------------
    val Array(trainData, validationData, testData) = labelpointRDD.randomSplit(Array(0.8, 0.1, 0.1))
    println("将数据分trainData:" + trainData.count() + " validationData:" +
      validationData.count() + " testData:" + testData.count())

    return (trainData, validationData, testData, categoriesMap)
  }

  def trainEvaluate(trainData: RDD[LabeledPoint],
                    validationData: RDD[LabeledPoint]) : DecisionTreeModel = {
    println("开始训练...")
    val (model, time) = trainModel(trainData, "entropy", 10, 10)
    println("训练完成，所需时间： " + time + "毫秒")
    val AUC = evaluateModel(model, validationData)
    println("评估结果 AUC = " + AUC)
    return model
  }

  def trainModel(trainData: RDD[LabeledPoint], impurity: String,
                 maxDepth: Int, maxBins: Int): (DecisionTreeModel, Double) = {
    val startTime = new DateTime()
    val model = DecisionTree.trainClassifier(trainData, 2, Map[Int, Int](),
      impurity, maxDepth, maxBins)
    val endTime = new DateTime()
    val duration = new Duration(startTime, endTime)
    (model, duration.getMillis)
  }

  def evaluateModel(model: DecisionTreeModel,
                    validationData: RDD[LabeledPoint]) : (Double) = {
    val scoreAndLabels = validationData.map { data =>
      val predict = model.predict(data.features)
      (predict, data.label)
    }
    val Metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val AUC = Metrics.areaUnderROC()
    (AUC)
  }

  def PredictData(sc: SparkContext, model: DecisionTreeModel,
                  categoriesMap: Map[String, Int]) : Unit = {
    // 1. 导入并转换数据
    val rawDataWithHeader = sc.textFile("data/test.tsv")
    val rawData = rawDataWithHeader.mapPartitionsWithIndex {
      (idx, iter) => if (idx == 0) iter.drop(1) else  iter
    }
    val lines = rawData.map(_.split("\t"))
    println("共计: " + lines.count().toString + " 条")
    //-----------2. 创建训练评估所需数据 --------------
    val dataRDD = lines.take(20).map { fields =>
      val trFields = fields.map(_.replaceAll("\"", ""))
      val categoryFeaturesArray = Array.ofDim[Double](categoriesMap.size)
      val cateforyIdx = categoriesMap(fields(3))
      categoryFeaturesArray(cateforyIdx) = 1
      val numericalFeatures = trFields.slice(4, fields.size).
        map(d => if (d == "?") 0.0 else d.toDouble)
      val label = 0
      //------------------------
      val url = trFields(0)
      val Features = Vectors.dense(categoryFeaturesArray ++ numericalFeatures)
      val predict = model.predict(Features).toInt
      val predictDesc = { predict match {
        case 0 => "暂时性网页(ephemeral)";
        case 1 => "长青网页(evergreen)";
      } }

      println("网址: " + url + " ===>预测：" + predictDesc)

      Unit
    }
  }

  def parametersTunning(trainData: RDD[LabeledPoint],
                        validationData: RDD[LabeledPoint]) : DecisionTreeModel = {
    println("-----评估 Impurity参数使用 gini, entropy---------")
    evaluateParameter(trainData, validationData, "impurity", Array("gini", "entropy"), Array(10), Array(10))
    println("-----评估MaxDepth参数使用 (3, 5, 10, 15, 20)---------")
    evaluateParameter(trainData, validationData, "maxDepth", Array("gini"), Array(3, 5, 10, 15, 20, 25), Array(10))
    println("-----评估maxBins参数使用 (3, 5, 10, 50, 100)---------")
    evaluateParameter(trainData, validationData, "maxBins", Array("gini"), Array(10), Array(3, 5, 10, 50, 100, 200))
    println("-----所有参数交叉评估找出最好的参数组合---------")
    val bestModel = evaluateAllParameter(trainData, validationData, Array("gini", "entropy"),
      Array(3, 5, 10, 15, 20), Array(3, 5, 10, 50, 100))
    return (bestModel)
  }

  def evaluateParameter(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint],
                        evaluateParameter: String, impurityArray: Array[String], maxdepthArray: Array[Int], maxBinsArray: Array[Int]) =
  {

    var dataBarChart = new DefaultCategoryDataset()

    var dataLineChart = new DefaultCategoryDataset()
    for (impurity <- impurityArray; maxDepth <- maxdepthArray; maxBins <- maxBinsArray) {
      val (model, time) = trainModel(trainData, impurity, maxDepth, maxBins)
      val auc = evaluateModel(model, validationData)

      val parameterData =
        evaluateParameter match {
          case "impurity" => impurity;
          case "maxDepth" => maxDepth;
          case "maxBins"  => maxBins
        }
      dataBarChart.addValue(auc, evaluateParameter, parameterData.toString())
      dataLineChart.addValue(time, "Time", parameterData.toString())
    }
    Chart.plotBarLineChart("DecisionTree evaluations " + evaluateParameter, evaluateParameter, "AUC", 0.58, 0.7, "Time", dataBarChart, dataLineChart)
  }

  def evaluateAllParameter(trainData: RDD[LabeledPoint], validationData: RDD[LabeledPoint], impurityArray: Array[String], maxdepthArray: Array[Int], maxBinsArray: Array[Int]): DecisionTreeModel =
  {
    val evaluationsArray =
      for (impurity <- impurityArray; maxDepth <- maxdepthArray; maxBins <- maxBinsArray) yield {
        val (model, time) = trainModel(trainData, impurity, maxDepth, maxBins)
        val auc = evaluateModel(model, validationData)
        (impurity, maxDepth, maxBins, auc)
      }
    val BestEval = (evaluationsArray.sortBy(_._4).reverse)(0)
    println("调校后最佳参数：impurity:" + BestEval._1 + "  ,maxDepth:" + BestEval._2 + "  ,maxBins:" + BestEval._3
      + "  ,结果AUC = " + BestEval._4)
    val (bestModel, time) = trainModel(trainData.union(validationData), BestEval._1, BestEval._2, BestEval._3)
    return bestModel
  }

}
