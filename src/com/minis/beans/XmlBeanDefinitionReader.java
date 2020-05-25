package com.minis.beans;

import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.minis.core.Resource;

public class XmlBeanDefinitionReader {
	SimpleBeanFactory bf;
	public XmlBeanDefinitionReader(SimpleBeanFactory bf) {
		this.bf = bf;
	}
	public void loadBeanDefinitions(Resource res) {
        while (res.hasNext()) {
        	Element element = (Element)res.next();
            String beanID=element.attributeValue("id");
            String beanClassName=element.attributeValue("class");

            BeanDefinition beanDefinition=new BeanDefinition(beanID,beanClassName);
            
            //handle properties
            List<Element> propertyElements = element.elements("property");
            PropertyValues PVS = new PropertyValues();
            for (Element e : propertyElements) {
            	String pType = e.attributeValue("type");
            	String pName = e.attributeValue("name");
            	String pValue = e.attributeValue("value");
            	PVS.addPropertyValue(new PropertyValue(pType, pName, pValue));
            }
        	beanDefinition.setPropertyValues(PVS);
        	//end of handle properties
        	
        	//get constructor
        	List<Element> constructorElements = element.elements("constructor-arg");       	
        	ArgumentValues AVS = new ArgumentValues();
        	for (Element e : constructorElements) {
            	String pType = e.attributeValue("type");
            	String pName = e.attributeValue("name");
            	String pValue = e.attributeValue("value");
        		AVS.addArgumentValue(new ArgumentValue(pType,pName,pValue));
        	}
    		beanDefinition.setConstructorArgumentValues(AVS);
        	//end of handle constructor

            this.bf.registerBeanDefinition(beanID,beanDefinition);
        }
		
	}
	


}
