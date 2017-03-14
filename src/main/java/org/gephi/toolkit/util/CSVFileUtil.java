package org.gephi.toolkit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class CSVFileUtil {
    public void main(String filename) {
//    	File csv = new File(getClass().getResource(filename).toURI());
    	File csv = new File(filename);
    	BufferedReader br = null;
    	try {
    		br = new BufferedReader(new FileReader(csv));
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}
    	
    	String line = "";
    	String everyLine = "";
    	try {
    		List<String> allString = new ArrayList<String>();
    		while ((line = br.readLine()) != null) {
    			everyLine = line;
    			System.out.println(everyLine);
    			allString.add(everyLine);
    		}
    		System.out.println("csv表格终所有行数：" + allString.size());
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
