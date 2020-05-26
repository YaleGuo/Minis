package com.minis.beans;

import java.net.URL;
import java.util.ArrayList;
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
    		
            //handle properties
            List<Element> propertyElements = element.elements("property");
            PropertyValues PVS = new PropertyValues();
            List<String> refs = new ArrayList<>();
            for (Element e : propertyElements) {
            	String pType = e.attributeValue("type");
            	String pName = e.attributeValue("name");
            	String pValue = e.attributeValue("value");
            	String pRef = e.attributeValue("ref");
            	String pV = "";
            	boolean isRef = false;
            	if (pValue != null && !pValue.equals("")) {
            		isRef = false;
            		pV = pValue;
            	} else if (pRef != null && !pRef.equals("")) {
            		isRef = true;
            		pV = pRef;
            		refs.add(pRef);
            	}
            	PVS.addPropertyValue(new PropertyValue(pType, pName, pV, isRef));
            }
        	beanDefinition.setPropertyValues(PVS);
        	String[] refArray = refs.toArray(new String[0]);
        	beanDefinition.setDependsOn(refArray);
        	//end of handle properties

            this.bf.registerBeanDefinition(beanID,beanDefinition);
        }
	}
	


}
