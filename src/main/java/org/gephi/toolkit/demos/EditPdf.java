/*
 * 用来对社区网络图pdf文件添加图例，显示每一种颜色的节点是属于哪一个社区。
 * 该类是在pdf里面添加内容，不是添加图片，所以添加图例的结果不会随着放大而变模糊。
 */
package org.gephi.toolkit.demos;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.gephi.appearance.api.Partition;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class EditPdf {
    
	/**
	 * 用来添加图例的函数
	 * @param partition gephi中partition的实例化对象，里面包含每一类别节点的详细数据，节点数、颜色等
	 * @param file 需要添加图例的pdf文件
	 */
	public void addLegend(Partition partition, String file) {
		try {
			int k = file.length();
			fileChannelCopy(new File(file), new File(file.substring(0, k - 5) + "_temp.pdf"));
			// 创建一个pdf读入流
			PdfReader reader = new PdfReader(file.substring(0, k - 5) + "_temp.pdf");
			// 根据一个pdfreader创建一个pdfStamper.用来生成新的pdf.
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(file));
			// 这个字体是itext-asian.jar中自带的 所以不用考虑操作系统环境问题.
			BaseFont bf = BaseFont.createFont("Times-Roman", "utf-8", BaseFont.NOT_EMBEDDED);																				// font
			// baseFont不支持字体样式设定.但是font字体要求操作系统支持此字体会带来移植问题.
			Font font = new Font(bf, 10);
			font.setStyle(Font.BOLD);
			font.getBaseFont();
			// 页数是从1开始的
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				// 获得pdfstamper在当前页的上层打印内容.也就是说 这些内容会覆盖在原先的pdf内容之上.
				PdfContentByte over = stamper.getOverContent(i);
				// 用pdfreader获得当前页字典对象.包含了该页的一些数据.比如该页的坐标轴信息.
				PdfDictionary p = reader.getPageN(i);
				// 拿到mediaBox 里面放着该页pdf的大小信息.
				PdfObject po = p.get(new PdfName("MediaBox"));
				System.out.println(po.isArray());
				// po是一个数组对象.里面包含了该页pdf的坐标轴范围.
				PdfArray pa = (PdfArray) po;
				int xLength = pa.getAsNumber(pa.size() - 2).intValue();
				int yLength = pa.getAsNumber(pa.size() - 1).intValue();

				int position = 0;
				int ovalWidth = yLength / 100;

				over.beginText();
				over.setFontAndSize(font.getBaseFont(), ovalWidth);
				over.setColorFill(BaseColor.BLACK);
				over.setTextMatrix(position + ovalWidth, 2 * ovalWidth + ovalWidth / 2);
				over.showText("Color of Nodes ");
				over.setTextMatrix(position + ovalWidth, ovalWidth);
				over.showText("Id of Community");
				over.endText();
				position = position + 7 * ovalWidth;
				for (Object value : partition.getSortedValues()) {
					position = position + 2 * ovalWidth;
					over.beginText();
					over.setFontAndSize(font.getBaseFont(), ovalWidth);
					Color color = partition.getColor(value);
					over.setColorFill(new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
					over.setTextMatrix(position, ovalWidth);
					over.showText(value.toString());
					over.endText();

					//用节点对应的颜色画点
					over.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
					over.setLineWidth(ovalWidth / 2);
					over.ellipse(position, 3 * ovalWidth, position + ovalWidth / 2, 2 * ovalWidth + ovalWidth / 2);
					over.stroke();
				}
				over.stroke();
			}
			stamper.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * 为了实现修改源文件，需要对文件进行复制一份，该函数用来对PDF文件的复制
	 * @param sources 源文件
	 * @param dest 目标文件
	 */
	public static void fileChannelCopy(File sources, File dest) {
		try {
			FileInputStream inputStream = new FileInputStream(sources);
			FileOutputStream outputStream = new FileOutputStream(dest);
			FileChannel fileChannepn = inputStream.getChannel();// 得到对应的文件通道
			FileChannel fileChannelout = outputStream.getChannel();// 得到对应的文件通道
			fileChannepn.transferTo(0, fileChannepn.size(), fileChannelout);// 连接两个通道，并且从in通道读取，然后写入out通道
			inputStream.close();
			fileChannepn.close();
			outputStream.close();
			fileChannelout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
