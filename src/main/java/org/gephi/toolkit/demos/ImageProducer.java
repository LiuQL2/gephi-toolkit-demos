/*
 * 改类根据gephi中partition中的信息产生一个图例图片，然后将图例图片添加到pdf文件中。
 */
package org.gephi.toolkit.demos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.gephi.appearance.api.Partition;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


public class ImageProducer {
	
	/**
	 * 用于产生图例
	 * @param partition gephi中包含各种类别颜色、节点信息的partition对象。
	 * @param legend 需要产生的目标图例文件
	 * @param imageWidth 图例图片的宽度
	 * @param imageHeight 图例图片的高度
	 */
    public void produceImage(Partition partition, String legend, int imageWidth, int imageHeight) {
    	int center = imageHeight / 2;
    	int ovalWidth = center - (imageHeight / 10 );
    	BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    	Graphics graphics = image.getGraphics();
    	try {    		
    		Font font = new Font("新宋体", Font.PLAIN, ovalWidth);
    		graphics.setFont(font);
    		graphics.fillRect(0, 0, imageWidth, imageHeight); 
    		graphics.setColor(new Color(0,0,0));
    		graphics.drawString(" Color of nodes  ", ovalWidth/2, (center + ovalWidth) / 2);
    		graphics.drawString("Id of communities", ovalWidth/2, (3 * center + ovalWidth) / 2);
    		int position = 5*imageHeight;
    		for (Object value: partition.getSortedValues()) {
    			Color  color = partition.getColor(value);
        		graphics.setColor(color);
        		graphics.fillOval(position, (center-ovalWidth) / 2, ovalWidth, ovalWidth);
        		graphics.drawString(value.toString(),  position+ovalWidth/4, (3 * center+ovalWidth)/2);
        		
        		position = position + ovalWidth * 3;
    		}
    		ImageIO.write(image,  "PNG", new File(legend));
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	graphics.dispose();
  
    }
    
    /**
     * 为pdf文件添加图片
     * @param file 需要添加图片的pdf文件
     * @param legend 需要被添加的图片
     */
    public void addLegend(String file, String legend) {
    	int k = file.length();
    	try {
    		fileChannelCopy(new File(file), new File(file.substring(0, k-5)+ "_temp.pdf"));
        	PdfReader pdfReader = new PdfReader(file.substring(0, k-5)+ "_temp.pdf");
        	PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));
        	PdfContentByte overContent = pdfStamper.getOverContent(1);
        	
        	PdfDictionary pdfDictionary = pdfReader.getPageN(1);
        	PdfObject pdfObject = pdfDictionary.get(new PdfName("MediaBox"));
        	PdfArray pdfArray = (PdfArray) pdfObject;
        	Image image = Image.getInstance(legend);
        	image.setAbsolutePosition(20,20);
//        	image.setAlignment(Image.ALIGN_CENTER);
        	overContent.addImage(image);
        	overContent.stroke();
        	overContent.closePath();
        	pdfStamper.close();
        	pdfReader.close();
        	} catch (Exception ex) {
        		ex.printStackTrace();
        		}
    	}
    
    /**
     * 复制pdf文件
     * @param sources 源文件
     * @param dest 目标文件
     */
    public static void fileChannelCopy(File sources, File dest) {
    	try {
    		FileInputStream inputStream = new FileInputStream(sources);
    		FileOutputStream outputStream = new FileOutputStream(dest);
       	    FileChannel fileChannepn = inputStream.getChannel();//得到对应的文件通道
       	    FileChannel fileChannelout = outputStream.getChannel();//得到对应的文件通道
       	    fileChannepn.transferTo(0, fileChannepn.size(), fileChannelout);//连接两个通道，并且从in通道读取，然后写入out通道
       	    inputStream.close();
       	    fileChannepn.close();
       	    outputStream.close();
       	    fileChannelout.close();
       	    } catch (Exception e) {
       	    	e.printStackTrace();
       	    	}
    	}
    
  
    
}
