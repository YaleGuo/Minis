package com.minis.beans;

import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.minis.core.Resource;

public class XmlBeanDefinitionReader {
	BeanFactory bf;
	public XmlBeanDefinitionReader(BeanFactory bf) {
		this.bf = bf;
	}
	public void loadBeanDefinitions(Resource res) {
        while (res.hasNext()) {
        	Element element = (Element)res.next();
            String beanID=element.attributeValue("id");
            String beanClassName=element.attributeValue("class");
            BeanDefinition beanDefinition=new BeanDefinition(beanID,beanClassName);
            this.bf.registerBeanDefinition(beanDefinition);
        }
		
	}
	


}
