# Minis
A mini Spring platform for learning.
We plan to implement IoC,MVC,JDBCTemplate and AOP from scratch.

IoC is the core of Minis. We will use a bean factory to manage all required beans.

Modified in local IntelliJ IDEA.

Use DefaultSingletonBeanRegistry to maintain beans as a repository. 
Modify beanfactory to just provide getBean(),defined as below:
  public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory
  
When ClassPathXmlApplicationContext is instantiated, no bean instance will be created.
the action to create a real bean instance is postponed to getBean() method.

