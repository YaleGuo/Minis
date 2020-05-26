	
Preface
	我们的目标是自己动手写一个类似于Spring的框架，包含IoC，MVC，JDBCTemplat，AOP等基础特性。
	这个框架是演示性质的，为了学习而建，但是并不简陋，所以是一个可以运行使用的框架。这个框架
	还有一个目的，为了引导人去读Spring源代码，所以目录结构，Class名Interface名与Spring的
	名字是一样的，甚至内部的method和field名字也是基本一样的。
	这个小的框架叫做Minis（mini-Spring）
	
-----------------------------------IOC--------------------------------------
1.	
	Spring框架的核心是IoC（注意，核心不是Core），所以我们也从构建一个IoC容器开始构建Minis。
	在我们的术语中，IoC和DI同义，并不做概念上的区分。客户程序用到的bean交给Minis去注入。
	我们把IoC容器管理的程序对象叫做bean。容器启动时，为所有声明的bean创建实例并统一管理。
	
	这个IoC容器就表现为一个简单的ClassPathXmlApplicationContext类，内部记录beandefinition以及存放bean的map：
    private List<BeanDefinition> beanDefinitions=new ArrayList<BeanDefinition>();
    private Map<String, Object> singletons =new HashMap<String, Object>();
    
	beandefinition是bean的定义声明，现在只是简单记录id和class name。
    	private String id;
    	private String className;
	
	为了让用户灵活配置bean，用一个外部的XML文件进行配置，格式如下：
	<?xml version="1.0" encoding="UTF-8"?>
	<beans>
		<bean id="aservice" class="com.beginner.test.AServiceImpl"></bean> 
	</beans>
	Minis启动的时候，从外部XML文件中读取bean的定义，生成beandefinition对象并存放到beanDefinitions list中。
	（通过dom4j解析XML文件，要用到第三方包dom4j-1.6.1.jar）
    
  	Minis通过Class.forName加载bean，加载后立即 new instance（非Lazy模式，以后再考虑Lazy模式），并放到bean map中： 
    singletons.put(beanDefinition.getId(),Class.forName(beanDefinition.getClassName()).newInstance());
    
	这个负责存放bean的map，我们起名叫singletons，这个名字隐含的意思是现在Minis中bean是singleton单例的，
	未来再考虑prototype特性。
	
	这样，ClassPathXmlApplicationContext一新建，就装载并实例化了所有的bean。
    
	客户程序使用Minis容器，目前采用编程式。
	先创建一个容器，将beans.xml作为参数，这个文件里面是所有声明的bean。
    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
  
 	然后客户程序再通过编程式从context中获取bean
   	AService aService=(AService)ctx.getBean("aservice");
   
	到此有了一个微型的容器了。
	不足：beandefinitin信息太少；装载XML的方法不能override；需要客户程序编程式初始化容器；不能自动注入bean；没有创建bean的API接口。

   
2.
	微型容器的代码写在一个类中ClassPathXmlApplicationContext，我们要进一步扩展它，使得它更加强大
	更加可配置。
	按照各负其责的思路，把独立的功能都各自独立出去，用ClassPathXmlApplicationContext集成。
	
	首先把读写XML文件的程序独立出来，	文件中的内容抽象成Resource的概念，用ClassPathXmlResource
	来负责文件格式解析，把XML文件中读取出来的内容放在Resource中。
	用类XmlBeanDefinitionReader从ClassPathXmlResource中读取数据，组成beandefinition注册到容器中。
	数据的演变路径：XML文件 - Resource - Bean Definition 
	
	增加NoSuchBeanDefinitionException
		
	管理bean的容器核心功能也单独放在一个类BeanFactory中，接口如下：
		Object getBean(String beanName) throws NoSuchBeanDefinitionException;
		void registerBeanDefinition(BeanDefinition bd);
	我们用SimpleBeanFactory类实现，把以前放在ClassPathXmlApplicationContext里面的数据放在这个类里面：
	    private List<BeanDefinition> beanDefinitions=new ArrayList<>();
    	private List<String> beanNames=new ArrayList<>();
    	private Map<String, Object> singletons =new HashMap<>();

	最基本的getBean()实现了延后new instance：    	
    public Object getBean(String beanName) throws NoSuchBeanDefinitionException{
        Object singleton = singletons.get(beanName);
        if (singleton == null) {
        	int i = beanNames.indexOf(beanName);
        	if (i == -1) {
        		throw new NoSuchBeanDefinitionException();
        	}
        	else {
        		BeanDefinition bd = beanDefinitions.get(i);
            	singleton=Class.forName(bd.getClassName()).newInstance();
				singletons.put(bd.getId(),singleton);
        	}       	
        }
        return singleton;
    }

	这样原本ClassPathXmlApplicationContext就演变成了一个集成环境，包含了 BeanFactory，使用reader装载:
    public ClassPathXmlApplicationContext(String fileName){
    	Resource res = new ClassPathXmlResource(fileName);
    	BeanFactory bf = new SimpleBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(res);
        this.beanFactory = bf;
    }
    constructor里面仅仅加载了bean的定义，没有生成bean实例，实例由getBean方法延迟加载。
    
	并由它代理访问beanfactory里面的getBean()方法：
	public Object getBean(String beanName) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getBean(beanName);
	}
	
	Minis的package结构演变成了:
	com.minis.beans
	com.minis.context
	com.minis.core

	客户程序没有变化，还是采用编程式。

	
3.
	把从BeanFactory里面进一步抽取出DefaultSingletonBeanRegistry，registry里面放beans/singletons:
	    protected List<String> beanNames=new ArrayList<>();
    	protected Map<String, Object> singletons =new ConcurrentHashMap<>(256);
    	
    	void registerSingleton(String beanName, Object singletonObject);
    	Object getSingleton(String beanName);
    	boolean containsSingleton(String beanName);
    	String[] getSingletonNames();
	Registry相当于一个仓库，只负责保有beans/singletons，BeanFactory提供对外的接口getBean().
	注：考虑多线程，此处用的ConcurrentHashMap。
	
	SimpleBeanFactory新定义如下，继承DefaultSingletonBeanRegistry：
	public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory{
    	private Map<String,BeanDefinition> beanDefinitions=new ConcurrentHashMap<>(256);
    	
    	Object getBean(String name) throws BeansException;
		boolean containsBean(String name);
		void registerBean(String beanName, Object obj);
    }
	
	ClassPathXmlApplicationContext构建的时候只读了bean的定义信息，没有生成bean。
	在BeanFactory的接口getBean()中判断，还没有生成bean就现生成。
	
	增加了BeanException.
	
	此处我们准备了AplicationEvent和ApplicationEventPublisher为后来使用。
	

4.
	进一步丰富BeanDefinition, 有了新的属性：
		String SCOPE_SINGLETON = "singleton";
		String SCOPE_PROTOTYPE = "prototype";
		private String scope=SCOPE_SINGLETON;		
		private boolean lazyInit = false;
		private String[] dependsOn;
		private ArgumentValues constructorArgumentValues;
		private PropertyValues propertyValues;
		private String initMethodName;
		private volatile Object beanClass;
	    private String id;
	    private String className;
	主要的新增属性是constructorArguments和propertyValues.用于反映bean中的构造方法和属性，以备后用。
	
	抽取出一个管理beandefinition的接口：
		public interface BeanDefinitionRegistry {
			void registerBeanDefinition(String name, BeanDefinition bd);
			void removeBeanDefinition(String name);
			BeanDefinition getBeanDefinition(String name);
			boolean containsBeanDefinition(String name);
		}
		
	BeanFactory提供了更多接口：
	    Object getBean(String name) throws BeansException;
		boolean containsBean(String name);
		boolean isSingleton(String name);
		boolean isPrototype(String name);
		Class<?> getType(String name);

	扩展核心实现类SimpleBeanFactory，新定义如下：
	class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory,BeanDefinitionRegistry
	
	核心的getBean()方法，new instance的单独抽出来ingleton=createBean(bd)：
    public Object getBean(String beanName) throws BeansException{
        Object singleton = this.getSingleton(beanName);
        if (singleton == null) {
        		BeanDefinition bd = beanDefinitionMap.get(beanName);
            	singleton=createBean(bd);
				this.registerBean(beanName, singleton);
        }
        if (singleton == null) {
        	throw new BeansException("bean is null.");
        }
        return singleton;
    }
    createBean()方法，现在还是只简单地new instance。
    private Object createBean(BeanDefinition bd) {
		Object obj = null;
		try {
    		obj = Class.forName(bd.getClassName()).newInstance();
		}
		return obj;
	}
	后面会结合构造方法和属性，扩展成一个更加完善的createBean().
	
		
5.
	增强createBean()，实现了通过特定的构造方法创建bean，并可以设置Property。
	Bean的XML配置如下：
	<bean id="aservice" class="com.minis.test.AServiceImpl"> 
		<constructor-arg type="String" name="name" value="abc"/>
		<constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
	</bean>
	
	从beandefinition中获取构造方法和Property
	    	//handle constructor
    		ArgumentValues argumentValues = bd.getConstructorArgumentValues();
    		if (!argumentValues.isEmpty()) {
        		Class<?>[] paramTypes = new Class<?>[argumentValues.getArgumentCount()];
        		Object[] paramValues =   new Object[argumentValues.getArgumentCount()];  
    			for (int i=0; i<argumentValues.getArgumentCount(); i++) {
    				ArgumentValue argumentValue = argumentValues.getIndexedArgumentValue(i);
    				if ("String".equals(argumentValue.getType()) || "java.lang.String".equals(argumentValue.getType())) {
    					paramTypes[i] = String.class;
        				paramValues[i] = argumentValue.getValue();
    				}
    				else if ("Integer".equals(argumentValue.getType()) || "java.lang.Integer".equals(argumentValue.getType())) {
    					paramTypes[i] = Integer.class;
        				paramValues[i] = Integer.valueOf((String) argumentValue.getValue());
    				}
    				else if ("int".equals(argumentValue.getType())) {
    					paramTypes[i] = int.class;
        				paramValues[i] = Integer.valueOf((String) argumentValue.getValue()).intValue();
    				}
    				else {
    					paramTypes[i] = String.class;
        				paramValues[i] = argumentValue.getValue();    					
    				}
    			}
				try {
					con = clz.getConstructor(paramTypes);
					obj = con.newInstance(paramValues);
				} 
			}
		从上面代码可以看出，目前我们采用的是argumentValues.getIndexedArgumentValue()这个方法，按照构造方法参数的
		索引值使用参数。并且目前只支持String和integer类型。
		
		//handle properties
		PropertyValues propertyValues = bd.getPropertyValues();
		if (!propertyValues.isEmpty()) {
			for (int i=0; i<propertyValues.size(); i++) {
				PropertyValue propertyValue = propertyValues.getPropertyValueList().get(i);
				String pName = propertyValue.getName();
				String pType = propertyValue.getType();
    			Object pValue = propertyValue.getValue();
    			
    			Class<?>[] paramTypes = new Class<?>[1];    			
				if ("String".equals(pType) || "java.lang.String".equals(pType)) {
					paramTypes[0] = String.class;
				}
				else if ("Integer".equals(pType) || "java.lang.Integer".equals(pType)) {
					paramTypes[0] = Integer.class;
				}
				else if ("int".equals(pType)) {
					paramTypes[0] = int.class;
				}
				else {
					paramTypes[0] = String.class;
				}
				
				Object[] paramValues =   new Object[1];  
				paramValues[0] = pValue;

    			String methodName = "set" + pName.substring(0,1).toUpperCase() + pName.substring(1);
				    			
    			Method method = null;
				try {
					method = clz.getMethod(methodName, paramTypes);
					method.invoke(obj, paramValues);
				}
			}
		}
		从上面代码可以看出，我们是通过调用setProperty()这一类方法设置的属性值，要求bean的属性有Setter。
		目前也只支持String和integer类型。
	
	ClassPathXmlApplicationContext仍然是一个集成环境，通过外部的XML文件加载beandefinition。
	并不立刻创建bean instance，而是在getBean()的时候创建。
	public ClassPathXmlApplicationContext(String fileName){
    	Resource res = new ClassPathXmlResource(fileName);
    	SimpleBeanFactory bf = new SimpleBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(res);
        this.beanFactory = bf;
    }
    
    
6.
	到目前为止，ClassPathXmlApplicationContext初始化的时候只是加载了beandefinition，
	并没有立刻创建bean的instance。在此，提供一个容器的refresh()方法创建所有的bean instance，
	context初始化的时候可以调用beanfactory的refresh()。
	public ClassPathXmlApplicationContext(String fileName, boolean isRefresh){
    	Resource res = new ClassPathXmlResource(fileName);
    	SimpleBeanFactory bf = new SimpleBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(res);
        this.beanFactory = bf;
        
        if (isRefresh) {
        	this.beanFactory.refresh();
        }
    }
	看上面的代码，可以把isRefresh标志理解为Lazy模式的实现。
    
	而beanfactory里的refresh()很简单，就是对所有的bean调用了一次getBean()，利用getBean()方法
	中的创建bean instance把容器中所有的bean instance创建出来。
	public void refresh() {
    	for (String beanName : beanDefinitionNames) {
    		try {
				getBean(beanName);
			} catch (BeansException e) {
				e.printStackTrace();
			}
    	}
    }
	
	bean里面的Property的ref可以支持引用了，即可以是引用另一个bean：
	<bean id="basebaseservice" class="com.minis.test.BaseBaseService"> 
	    <property type="com.minis.test.AServiceImpl" name="as" ref="aservice"/>
	</bean>
	我们改造一下以前的createBean()方法，抽取出一个单独的处理属性的方法来：
	void handleProperties(BeanDefinition bd, Class<?> clz, Object obj);
	//is ref, create the dependent beans
    	try {
			paramTypes[0] = Class.forName(pType);
			paramValues[0] = getBean((String)pValue);
		}
	从上面代码可以看到实现的思路，就是对ref所指向的另一个bean再次调用getBean()方法。
	如果有多级引用，形成一个多级的getBean()调用链。
	因为getBean的时候,会判断容器中是否包含了bean instance，没有的话会现创建，所以XML
	中声明bean的先后次序是任意的。
	
	但是这样会引出一个循环引用的问题，比如A引用到B，B又引用到C，而C又引用到A。这种情况下，
	创建的过程会成为死结，我们必须想办法打破这个循环。如：
	<bean id="basebaseservice" class="com.minis.test.BaseBaseService"> 
	    <property type="com.minis.test.AServiceImpl" name="as" ref="aservice"/>
	</bean>
	<bean id="aservice" class="com.minis.test.AServiceImpl"> 
		<constructor-arg type="String" name="name" value="abc"/>
		<constructor-arg type="int" name="level" value="3"/>
        <property type="String" name="property1" value="Someone says"/>
        <property type="String" name="property2" value="Hello World!"/>
        <property type="com.minis.test.BaseService" name="ref1" ref="baseservice"/>
	</bean>
	<bean id="baseservice" class="com.minis.test.BaseService"> 
	        <property type="co
	        m.minis.test.BaseBaseService" name="bbs" ref="basebaseservice"/>
	</bean>
	
	我们的做法是给容器中引入一个临时的结构Map<String, Object> earlySingletonObjects：
	public class SimpleBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory,BeanDefinitionRegistry{
    	private Map<String,BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<>(256);
    	private List<String> beanDefinitionNames=new ArrayList<>();
		private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);
	这个earlySingletonObjects中保存的是毛胚状态的bean instance,所谓毛胚状态，就是bean已经创建了instance但是
	property都还没有设置，等着后续处理。
	我们从createBean()方法中单独抽取一个doCreateBean(bd)方法来专门负责毛胚bean的创建，创建好
	毛胚bean后放到临时的earlySingletonObjects结构中，然后再调用handleProperties补齐这些property的值：
	private Object createBean(BeanDefinition bd) {
		Class<?> clz = null;
		
		Object obj = doCreateBean(bd);
		this.earlySingletonObjects.put(bd.getId(), obj);
		clz = Class.forName(bd.getClassName());
		
		handleProperties(bd, clz, obj);
		
		return obj;
	} 
	
	在getBean()的时候，就要判断earlySingletonObjects有没有毛胚bean：
	public Object getBean(String beanName) throws BeansException{
        Object singleton = this.getSingleton(beanName);
        
        if (singleton == null) {
        	singleton = this.earlySingletonObjects.get(beanName);
        	if (singleton == null) {
        		BeanDefinition bd = beanDefinitionMap.get(beanName);
            	singleton=createBean(bd);
				this.registerBean(beanName, singleton);

        	}
        }
        
        return singleton;
    }
	
	
	
	
	
	