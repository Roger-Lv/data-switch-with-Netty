package org.bdware.sw.server.cache;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class CacheCostTimeBarChart extends JFrame {

    public CacheCostTimeBarChart(String title) {
        super(title);

        // 创建数据集
        CategoryDataset dataset = createDataset();

        // 创建图表
        JFreeChart chart = ChartFactory.createBarChart(
                "Cost Time of Cache Algorithms", // 标题
                "Cache Algorithm", // 横轴标签
                "Cost Time (ms)", // 纵轴标签
                dataset, // 数据集
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // 配置图表样式
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.2); // 调整柱状图之间的间距

        BarRenderer renderer = new BarRenderer();
        renderer.setMaximumBarWidth(0.15); // 调整柱子的宽度

        // 设置不同的颜色
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setSeriesPaint(3, Color.ORANGE);

        plot.setRenderer(renderer);

        // 设置纵轴范围
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(7500, 9800);

        // 显示图表
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        setContentPane(chartPanel);
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(7960.7, "LRU", "LRU");
        dataset.addValue(8098.1, "LFU", "LFU");
        dataset.addValue(8004.6, "SLRU/LRU-2", "SLRU/LRU-2");
        dataset.addValue(9289.3, "W-TinyLFU", "W-TinyLFU");

        return dataset;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CacheCostTimeBarChart example = new CacheCostTimeBarChart("Cache Cost Time Bar Chart");
            example.setSize(600, 400);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
