package com.minis.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

public class XmlConfigReader {
	public XmlConfigReader() {
	}
	public Map<String,MappingValue> loadConfig(Resource res) {
		Map<String,MappingValue> mappings = new HashMap<>();
		
        while (res.hasNext()) {
        	Element element = (Element)res.next();
            String beanID=element.attributeValue("id");
            String beanClassName=element.attributeValue("class");
            String beanMethod=element.attributeValue("value");

            mappings.put(beanID, new MappingValue(beanID,beanClassName,beanMethod));
        }
        
        return mappings;
	}
	


}
