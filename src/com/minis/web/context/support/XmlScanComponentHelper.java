package com.minis.web.context.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class XmlScanComponentHelper {
	    public static List<String> getNodeValue(URL xmlPath) {
	    	List<String> packages = new ArrayList<>();
	        SAXReader saxReader=new SAXReader();
			Document document = null;
			try {
				document = saxReader.read(xmlPath);
			} catch (DocumentException e) {
				e.printStackTrace();
			}
	        Element root = document.getRootElement();
			List elements = root.elements("component-scan");
			for (Object element : elements) {
				packages.add(((Element)element).attributeValue("base-package"));
			}
			
			return packages;
	    }

}
