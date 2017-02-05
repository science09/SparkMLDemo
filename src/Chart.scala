import java.awt.Color

import org.jfree.chart._
import org.jfree.chart.axis.{NumberAxis, _}
import org.jfree.chart.labels.StandardCategoryToolTipGenerator
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset

/**
  * Created by hadoop on 17-2-5.
  */
object Chart {

  def plotBarLineChart(Title: String, xLabel: String,
                       yBarLabel: String, yBarMin: Double,
                       yBarMax: Double, yLineLabel: String,
                       dataBarChart : DefaultCategoryDataset,
                       dataLineChart: DefaultCategoryDataset): Unit = {

    //画出Bar Chart
    val chart = ChartFactory
      .createBarChart(
        "", // Bar Chart 标题
        xLabel, // X轴标题
        yBarLabel, // Bar Chart 标题 y轴标题l
        dataBarChart , // Bar Chart数据
        org.jfree.chart.plot.PlotOrientation.VERTICAL,//画图方向垂直
        true, // 包含 legend
        true, // 显示tooltips
        false // 不要URL generator
      );
    //取得plot
    val plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
    plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
    plot.setDataset(1, dataLineChart); plot.mapDatasetToRangeAxis(1, 1)
    //画直方图y轴
    val vn = plot.getRangeAxis(); vn.setRange(yBarMin, yBarMax);  vn.setAutoTickUnitSelection(true)
    //画折线图y轴
    val axis2 = new NumberAxis(yLineLabel); plot.setRangeAxis(1, axis2);
    val renderer2 = new LineAndShapeRenderer()
    renderer2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
    //设置先画直方图,再画折线图以免折线图被盖掉
    plot.setRenderer(1, renderer2);plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    //创建画框
    val frame = new ChartFrame(Title,chart); frame.setSize(500, 500);
    frame.pack(); frame.setVisible(true)
  }
}
