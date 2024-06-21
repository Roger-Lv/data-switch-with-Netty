package org.bdware.sw.server.cache;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class CacheHitRatioChart extends JFrame {

    public CacheHitRatioChart(String title) {
        super(title);

        XYSeries lruSeries = new XYSeries("LRU");
        XYSeries lfuSeries = new XYSeries("LFU");
        XYSeries slruSeries = new XYSeries("SLRU/LRU-2");
        XYSeries wTinyLFUSeries = new XYSeries("W-TinyLFU");

        // 添加数据点
        addDataPoints(lruSeries, new double[]{5000,10000, 20000, 50000, 100000, 200000,250000, 320000,390000,450000},
                new double[]{0.2659323,
                        0.27217743,
                        0.28244174,
                        0.3103901,
                        0.3556148,
                        0.44529372,
                        0.48989448,
                        0.55249995,
                        0.61505455,
                        0.6687762});
        addDataPoints(lfuSeries, new double[]{5000,10000, 20000, 50000, 100000, 200000,250000, 320000,390000,450000},
                new double[]{0.26891664,
                        0.27242577,
                        0.27914342,
                        0.2989052,
                        0.33269405,
                        0.40445623,
                        0.44296995,
                        0.50057906,
                        0.5644128,
                        0.6260558});
        addDataPoints(slruSeries, new double[]{5000,10000, 20000, 50000, 100000, 200000,250000, 320000,390000,450000},
                new double[]{0.26872593,
                        0.27361467,
                        0.28289327,
                        0.3096791,
                        0.35379303,
                        0.44208786,
                        0.4861129,
                        0.54857635,
                        0.6114476,
                        0.66602546});
        addDataPoints(wTinyLFUSeries, new double[]{5000,10000, 20000, 50000, 100000, 200000,250000, 320000,390000,450000},
                new double[]{0.26908994,
                        0.27254054,
                        0.27867672,
                        0.29501554,
                        0.32393175,
                        0.40219498,
                        0.45047322,
                        0.51745385,
                        0.59093684,
                        0.658009});

        // 创建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(lruSeries);
        dataset.addSeries(lfuSeries);
        dataset.addSeries(slruSeries);
        dataset.addSeries(wTinyLFUSeries);

        // 创建图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Cache Hit Ratio & Capacity", // 标题
                "Capacity", // 横轴标签
                "Hit Ratio", // 纵轴标签
                dataset, // 数据集
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // 配置图表样式
        XYPlot plot = chart.getXYPlot();
        plot.setDomainPannable(true); // 允许横轴缩放
        plot.setRangePannable(true);  // 允许纵轴缩放

        // 设置纵轴范围
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(0.25, 0.7); // 根据实际情况调整纵轴范围

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // 设置第一条线加粗
        renderer.setSeriesStroke(1, new BasicStroke(2.0f)); // 设置第二条线加粗
        renderer.setSeriesStroke(2, new BasicStroke(2.0f)); // 设置第三条线加粗
        renderer.setSeriesStroke(3, new BasicStroke(2.0f)); // 设置第四条线加粗

        plot.setRenderer(renderer);

        // 显示图表
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private void addDataPoints(XYSeries series, double[] xValues, double[] yValues) {
        for (int i = 0; i < xValues.length; i++) {
            series.add(xValues[i], yValues[i]);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CacheHitRatioChart example = new CacheHitRatioChart("Cache Hit Ratio Chart");
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}


