	
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
	
	
	
7.
	createBean()里面不直接handleProperties()了，而是扩充一下概念，调用一个新方法：
		populateBean(bd, clz, obj);
		目前阶段，populateBean()只做一件事情：调用handleProperties()
		private void populateBean(BeanDefinition bd, Class<?> clz, Object obj) {
			handleProperties(bd, clz, obj);
		}
	
	增强getBean()，在createBean()之后，支持beanpostprocessor和init-method:
		//beanpostprocessor
		//step 1 : postProcessBeforeInitialization
		applyBeanPostProcessorsBeforeInitialization(singleton, beanName);				

		//step 2 : init-method
		if (bd.getInitMethodName() != null && !bd.getInitMethodName().equals("")) {
			invokeInitMethod(bd, singleton);
		}

		//step 3 : postProcessAfterInitialization
		applyBeanPostProcessorsAfterInitialization(singleton, beanName);
		
	为了扩展性，把SimpleBeanFactory分成了AbstractBeanFactory和AutowireCapableBeanFactory。
	AbstractBeanFactory中applyBeanPostProcessorsBeforeInitialization和applyBeanPostProcessorsAfterInitialization
	是abstract的。需要在AutowireCapableBeanFactory中去实现，这个类实现了自动注入。(AutowireCapableBeanFactory通过BeanPostProcessor实现了Autowired)
	
	定义如下：
	public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory,BeanDefinitionRegistry;
	public class AutowireCapableBeanFactory extends AbstractBeanFactory{
		private final List<AutowiredAnnotationBeanPostProcessor> beanPostProcessors = new ArrayList<AutowiredAnnotationBeanPostProcessor>();
	}
	这个AutowireCapableBeanFactory扩展出来的功能是支持Autowired自动注入,所以里面有一个list存放了AutowiredAnnotationBeanPostProcessor。

	Autowired注解定义如下：
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Autowired {
	}
		
	BeanPostProcessor接口提供两个方法：
		Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;
		Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

	我们在postProcessBeforeInitialization()方法中利用Java的reflection机制进行属性设置，达到自动注入的目的。
	因此可以看出，在bean的initmethod被调用之前，这些该注入的属性都已经设置好了。
	
	实际的注入代码实现如下：
		String fieldName = field.getName();
		Object autowiredObj = this.getBeanFactory().getBean(fieldName);
		field.set(bean, autowiredObj);
	可以看出，目前我们支持的是按照名称匹配进行注入，这点与Spring的默认模式不一样。后期可以扩展为可配置项。
	
	到此，类已经比较多了，我们参照Spring的目录重新组织了一下bzuieans目录结构如下：
	com.minis
	com.minis.beans
	com.minis.beans.factory
	com.minis.beans.factory.annotation
	com.minis.beans.factory.config
	com.minis.beans.factory.support
	com.minis.beans.factory.xml
	
	最后，ClassPathXmlApplicationContext仍然是一个集成环境，给refresh()增加一个功能registerBeanPostProcessors():
	public void refresh() throws BeansException, IllegalStateException {
		// Register bean processors that intercept bean creation.
		registerBeanPostProcessors(this.beanFactory);

		// Initialize other special beans in specific context subclasses.
		onRefresh();
	}
	private void registerBeanPostProcessors(AutowireCapableBeanFactory bf) {
		//if (supportAutowire) {
			bf.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
		//}
	}
	private void onRefresh() {
		this.beanFactory.refresh();
	}
	我们暂时只有自动注入这么一个beanpostprocessor.
	
	我们还为ClassPathXmlApplicationContext提供了一个BeanFactoryPostProcessor：
		private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors =
			new ArrayList<BeanFactoryPostProcessor>();	
	别的程序可以利用这个结构来执行一些预处理工作。
	
	
8.
	为了扩展性，进一步提出几个interface：
	ListableBeanFactory接口扩展beanfactory，提供一些bean集合的方法。
	public interface ListableBeanFactory extends BeanFactory {
		boolean containsBeanDefinition(String beanName);
		int getBeanDefinitionCount();
		String[] getBeanDefinitionNames();
		String[] getBeanNamesForType(Class<?> type);
		<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;
	}
	ConfigurableBeanFactory接口扩展BeanFactory,SingletonBeanRegistry，提供BeanPostProcessor和dependent方法：
	public interface ConfigurableBeanFactory extends BeanFactory,SingletonBeanRegistry {
		String SCOPE_SINGLETON = "singleton";
		String SCOPE_PROTOTYPE = "prototype";
		void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
		int getBeanPostProcessorCount();
		void registerDependentBean(String beanName, String dependentBeanName);
		String[] getDependentBeans(String beanName);
		String[] getDependenciesForBean(String beanName);
	}
	AutowireCapableBeanFactory接口提供通过beanpostprocessor实现Autowired方法：
	public interface AutowireCapableBeanFactory  extends BeanFactory{
		int AUTOWIRE_NO = 0;
		int AUTOWIRE_BY_NAME = 1;
		int AUTOWIRE_BY_TYPE = 2;
		Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
				throws BeansException;
		Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
				throws BeansException;
	}
	再用一个interface ConfigurableListableBeanFactory把上面的三个interface集成在一起。
	public interface ConfigurableListableBeanFactory 
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {
	}
	
	这是设计的原则之一：接口隔离。 每个接口提供单一功能，可以组合选择实现那些接口。	

	最后实现了DefaultListableBeanFactory：
	public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory 
					implements ConfigurableListableBeanFactory
	这个类现在成了IoC的引擎。
	
	ClassPathXmlApplicationContext仍然是集成环境，里面现在包含了一个DefaultListableBeanFactory：
	public class ClassPathXmlApplicationContext implements ApplicationContext{
		DefaultListableBeanFactory beanFactory;
		private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors =
				new ArrayList<BeanFactoryPostProcessor>();	
	}
	
	为了扩展性，把ClassPathXmlApplicationContext做成一个真正的容器，具有上下文，提出下面的接口：
	public interface ApplicationContext 
		extends EnvironmentCapable, ListableBeanFactory, ConfigurableBeanFactory, ApplicationEventPublisher{
	}
	支持上下文环境，支持事件发布。	
	
	
9.
	丰富ApplicationContext接口，现在具有了很多容器的基本方法了：
	public interface ApplicationContext 
			extends EnvironmentCapable, ListableBeanFactory, ConfigurableBeanFactory, ApplicationEventPublisher{
		String getApplicationName();
		long getStartupDate();
		ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;
		void setEnvironment(Environment environment);
		Environment getEnvironment();
		void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);
		void refresh() throws BeansException, IllegalStateException;
		void close();
		boolean isActive();
	}
	
	提供ApplicationListener：
	public class ApplicationListener implements EventListener {
		void onApplicationEvent(ApplicationEvent event) {
			System.out.println(event.toString());
		}
	}
	使用到了ApplicationEvent.

	AbstractApplicationContext的refresh规范化成几步(注意步骤之间的先后次序)：
		postProcessBeanFactory(getBeanFactory());
		registerBeanPostProcessors(getBeanFactory());
		initApplicationEventPublisher();
		onRefresh();
		registerListeners();
		finishRefresh();
	并把这几步定义成abstract的：
		abstract void postProcessBeanFactory(ConfigurableListableBeanFactory bf);
		abstract void registerBeanPostProcessors(ConfigurableListableBeanFactory bf);
		abstract void initApplicationEventPublisher();
		abstract void onRefresh();
		abstract void registerListeners();
		abstract void finishRefresh();
	
	ClassPathXmlApplicationContext仍然是集成环境，不过现在简化了，继承了AbstractApplicationContext，
	实现了这些abstract方法。
	finishRefresh中会publishEvent(new ContextRefreshEvent("Context Refreshed..."));
	
	至此，我们的IoC就小有模样了。
		

		
-----------------------------------MVC--------------------------------------
MVC的基本思路是屏蔽servlet的概念，让应用程序员只需要了解很少的servlet，主要写业务逻辑。浏览器访问的
URL通过映射机制找到实际的业务逻辑方法。
按照servlet规范，可以通过Filter拦截，也可以通过Servlet拦截。实际的实现过程中，我通过DispatcherServlet
拦截所有请求，处理映射关系，调用业务逻辑代码，处理返回值回递给浏览器。
程序员写的业务逻辑程序，我们叫做bean。

1.
	写了一个DispatcherServlet，由它来处理所有请求，初始化的时候从外部xml文件读取mapping信息：
	  <servlet>
	    <servlet-name>minisMVC</servlet-name>
	    <servlet-class>com.minis.web.DispatcherServlet</servlet-class>
	    <init-param>
	      <param-name>contextConfigLocation</param-name>
	      <param-value> /WEB-INF/minisMVC-servlet.xml </param-value>
	    </init-param>
	    <load-on-startup>1</load-on-startup>
	  </servlet>
	  <servlet-mapping>
	    <servlet-name>minisMVC</servlet-name>
	    <url-pattern>/</url-pattern>
	  </servlet-mapping>
	
	定义外部minisMVC-servlet.xml文件格式：
	<bean id="/helloworld" class="com.minis.test.HelloWorldBean" value="doGet" />

	
	DispatcherServlet内部记录在三个Map中，记录了URL对应的类对象和方法
	    private Map<String,MappingValue> mappingValues;
    	private Map<String,Class<?>> mappingClz = new HashMap<>();
    	private Map<String,Object> mappingObjs = new HashMap<>();
    
    init()的核心代码：	
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	
        sContextConfigLocation = config.getInitParameter("contextConfigLocation");
        
        URL xmlPath = null;
		xmlPath = this.getServletContext().getResource(sContextConfigLocation);
		Resource rs = new ClassPathXmlResource(xmlPath);
        XmlConfigReader reader = new XmlConfigReader();
        mappingValues = reader.loadConfig(rs);
        Refresh();
    }
	从上述代码可以看出，这个DispatcherServlet初始化的时候，由config(通过容器如Tomcat传入)
	从外部的文件(由contextConfigLocation初始化参数定义)读取资源，把minisMVC-servlet.xml
	定义的bean定义转换成内存结构Map<String,MappingValue> mappingValues。
	读取外部文件，解析XML，生成内部结构，这些操作由ClassPathXmlResource和XmlConfigReader完成，
	这个实现接近于IoC中的相应功能。
	最后调用它Refresh()实际创建bean.
	
	protected void Refresh() {
    	for (Map.Entry<String,MappingValue> entry : mappingValues.entrySet()) {
    		String id = entry.getKey();
    		String className = entry.getValue().getClz();
    		Object obj = null;
    		Class<?> clz = null;
    		
			clz = Class.forName(className);
			obj = clz.newInstance();

			mappingClz.put(id, clz);
    		mappingObjs.put(id, obj);
    	}
    }
    Refresh()就是通过读取mappingValues中的bean定义，加载类，创建实例。
    
	这个servlet负责所有请求，我们先实现doGet:
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sPath = request.getServletPath();
		
		Class<?> clz = this.mappingClz.get(sPath);
		Object obj = this.mappingObjs.get(sPath);
		String methodName = this.mappingValues.get(sPath).getMethod();
		Object objResult = null;

		Method method = clz.getMethod(methodName);
		objResult = method.invoke(obj);
		
		response.getWriter().append(objResult.toString());
	}
	这是一个框架实现，它从mappingClz，mappingObjs和mappingValues获取与当前请求url关联的bean信息，
	然后调用实例的相关方法，返回结构通过response返回。
	
	这个实现很简陋，调用的方法没有参数，返回值只是String，回写直接通过response。
	

2.
	通过	定义外部minisMVC-servlet.xml文件格式：
	<bean id="/helloworld" class="com.minis.test.HelloWorldBean" value="doGet" />
	每个业务逻辑的方法都要定义一次，很麻烦。
	
	我们现在支持<component-scan base-package="com.minis.test"/>扫描所有的相关类
	并支持注解   @RequestMapping 来实现url和方法的映射。
	
	先修改minisMVC-servlet.xml文件格式：
	<components>
	<component-scan base-package="com.minis.test"/>
	</components>
	不再一个类一个类一个方法一个方法声明了，简单地写一个package就可以了。
	packages里面的类，我们不把它们叫bean，而是用一个特殊的名字controller.
	
	同时提供注解,将url与这个方法进行映射：
	@Target(value={ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface RequestMapping {
	    String value() default "";
	}
	目前，我们不提供类级别的RequestMapping.
	
	DispatcherServlet中用如下结构保存映射声明：
		private List<String> packageNames = new ArrayList<>();
	    private Map<String,Object> controllerObjs = new HashMap<>();
	    private List<String> controllerNames = new ArrayList<>();
	    private Map<String,Class<?>> controllerClasses = new HashMap<>();
	    private List<String> urlMappingNames = new ArrayList<>();
	    private Map<String,Object> mappingObjs = new HashMap<>();
	    private Map<String,Method> mappingMethods = new HashMap<>();
	packageNames是需要扫描的package列表；
	urlMappingNames是定义的 @RequestMapping名字（url名字）列表；
	mappingObjs保有的是url名字与对象的映射；
	mappingObjs保有的是url名字与方法的映射；
	controllerNames是controller的名字列表；
	controllerClasses是controller名字与类的映射；
	controllerObjs是controller名字与对象的映射；
	
	我们不再用ClassPathXmlResource和XmlConfigReader提供bean的注册，而是用一个
	XmlScanComponentHelper类扫描minisMVC-servlet.xml，拿到List<String> packages。
	private List<String> scanPackage(String packageName) {
    	List<String> tempControllerNames = new ArrayList<>();
        URL url  =this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
            	scanPackage(packageName+"."+file.getName());
            }else{
                String controllerName = packageName +"." +file.getName().replace(".class", "");
                tempControllerNames.add(controllerName);
            }
        }
        return tempControllerNames;
    }
	考虑目录下还有子目录，所以用到了递归。
	结果就是把package下所有的类的全名加到tempControllerNames列表中返回。
	
	Refresh()分成两步：
	protected void Refresh() {
    	initController();  //初始化controller
    	initMapping(); //初始化url映射
    }
    
	初始化controller对扫描到的每一个类进行加载和实例化，放到map中，以类名为key。
    protected void initController() {
    	this.controllerNames = scanPackages(this.packageNames);
    	
    	for (String controllerName : this.controllerNames) {
    		Object obj = null;
    		Class<?> clz = null;

			clz = Class.forName(controllerName);
			this.controllerClasses.put(controllerName,clz);

			obj = clz.newInstance();
			this.controllerObjs.put(controllerName, obj);
    	}
    }
	
	初始化url映射，找到定义了@RequestMapping的方法，url存放到urlMappingNames中，映射的对象
	存放到mappingObjs中，映射的方法存放到mappingMethods中。
    protected void initMapping() {
    	for (String controllerName : this.controllerNames) {
    		Class<?> clazz = this.controllerClasses.get(controllerName);
    		Object obj = this.controllerObjs.get(controllerName);
    		Method[] methods = clazz.getDeclaredMethods();
    		if(methods!=null){
    			for(Method method : methods){
    				boolean isRequestMapping = method.isAnnotationPresent(RequestMapping.class);
    				if (isRequestMapping){
    					String methodName = method.getName();
    					String urlmapping = method.getAnnotation(RequestMapping.class).value();
    					this.urlMappingNames.add(urlmapping);
    					this.mappingObjs.put(urlmapping, obj);
    					this.mappingMethods.put(urlmapping, method);
    				}
    			}
    		}
    	}
    }
	所以这一部分程序就取代了以前的bean解析出来的映射。
	
	doGet方法还是没有变化。
	
	
	
3.
	现在我们把IoC和MVC结合在一起。使得mvc controller这一层可以引用到内部的bean。
	
	以前我们IoC容器用了一个main()运行启动，现在不用了，我们可以用JavaEE服务器的启动机制，
	这里我用到了Listener机制。
		
	增加ContextLoaderListener，持有一个WebApplicationContext：
		public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
		private WebApplicationContext context;
	启动时注册WebApplicationContext，并设置到servletContext的Attribute中：
	public void contextInitialized(ServletContextEvent event) {
		initWebApplicationContext(event.getServletContext());
	}
	private void initWebApplicationContext(ServletContext servletContext) {
		String sContextLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		WebApplicationContext wac = new AnnotationConfigWebApplicationContext(sContextLocation);
		wac.setServletContext(servletContext);
		this.context = wac;
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);
	}
	最关键的是创建了一个AnnotationConfigWebApplicationContext wac。这个wac与容器的servletContext互相引用。
	
	WebApplicationContext定义如下：
	public interface WebApplicationContext extends ApplicationContext {
		String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";
		ServletContext getServletContext();
		void setServletContext(ServletContext servletContext);
	}
	public class AnnotationConfigWebApplicationContext 
					extends ClassPathXmlApplicationContext implements WebApplicationContext{
		private ServletContext servletContext;
	}
	我们可以看出，AnnotationConfigWebApplicationContext实际上就是一个IoC中的ClassPathXmlApplicationContext，并且
	加入了servletContext信息，构成一个适合web场景的上下文。这样Listener启动的时候IoC容器启动了，通过刷新装载了所有管理的beans.
	
	合在一起后，就有两个配置文件了，一个给DispatcherServlet, 一个给ContextLoaderListener：
	applicationContext.xml:
	<?xml version="1.0" encoding="UTF-8"?>
	<beans>
		<bean id="bbs" class="com.test.service.BaseBaseService"> 
		    <property type="com.test.service.AServiceImpl" name="as" ref="aservice"/>
		</bean>
		<bean id="aservice" class="com.test.service.AServiceImpl"> 
			<constructor-arg type="String" name="name" value="abc"/>
			<constructor-arg type="int" name="level" value="3"/>
	        <property type="String" name="property1" value="Someone says"/>
	        <property type="String" name="property2" value="Hello World!"/>
	        <property type="com.test.service.BaseService" name="ref1" ref="baseservice"/>
		</bean>
		<bean id="baseservice" class="com.test.service.BaseService"> 
		</bean>
	</beans>
	
	minisMVC-servlet.xml:
	<?xml version="1.0" encoding="UTF-8" ?>
	<components>
		<component-scan base-package="com.test"/>
	</components>
	
	Dispatcher初始化的时候获取上面注册的Context,这样也就可以从servlet中拿到listener时启用的WebApplicationContext了:
    	this.webApplicationContext = (WebApplicationContext) this.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    	
	然后再常规的扫描Controller
        sContextConfigLocation = config.getInitParameter("contextConfigLocation");
        this.packageNames = XmlScanComponentHelper.getNodeValue(xmlPath);
        Refresh();

	到此为止，我们的WebApplicationContext和Dispatcher都准备好了。并且从Dispatcher可以访问到WebApplicationContext，
	但是注意，反过来是拿不到的，这个单向的引用必须记住。
	
	
4.
	现在有了AnnotationConfigWebApplicationContext和DispatcherServlet，我们按照分工，整理一下代码。
	我们希望DispatcherServlet最后只负责解析request请求，解析，分发，处理返回。而ApplicationContext
	则负责业务逻辑beans。
	注意到由于web的引入，业务逻辑程序分层了两层：controller和service，原理上这两层是可以完全分开的。
	为了使得结构化更好，我们引入两个application context：一个针对controller层，一个针对service层。
	service层的由listener启动的application context容器负责，而controller层的由DispatcherServlet负责启动。
	按照时序，listener先启动，我们把它叫做parentApplicationContext。DispatcherServlet启动的webapplicationcontext
	持有对parentApplicationContext的引用。
	
	程序实现上，分别用的XmlWebApplicationContext和AnnotationConfigWebApplicationContext。
	
	DispatcherServlet类中使用两个变量：
	private WebApplicationContext webApplicationContext;
	private WebApplicationContext parentApplicationContext;
	
	初始化的时候先从servletcontext中拿属性WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE，
	获得listener存放在这里的parentApplicationContext；然后通过contextConfigLocation配置文件创建一个新的
	webApplicationContext。
	代码如下：
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	this.parentApplicationContext = 
    			(WebApplicationContext) this.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    	
        sContextConfigLocation = config.getInitParameter("contextConfigLocation");
    	this.webApplicationContext = new AnnotationConfigWebApplicationContext(sContextConfigLocation,this.parentApplicationContext);

        Refresh();
    }

	扩充AnnotationConfigWebApplicationContext，把DispatcherServlet中一部分与扫描包有关的代码挪到这里.
	public class AnnotationConfigWebApplicationContext 
						extends AbstractApplicationContext implements WebApplicationContext{
		private WebApplicationContext parentApplicationContext;
		private ServletContext servletContext;
		DefaultListableBeanFactory beanFactory;
		private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors =
				new ArrayList<BeanFactoryPostProcessor>();	
	}

	public WebApplicationContext(String fileName, WebApplicationContext parentApplicationContext) {
		this.parentApplicationContext = parentApplicationContext;
		this.servletContext = this.parentApplicationContext.getServletContext();
        URL xmlPath = null;

		xmlPath = this.getServletContext().getResource(fileName);
        List<String> packageNames = XmlScanComponentHelper.getNodeValue(xmlPath);
        List<String> controllerNames = scanPackages(packageNames);

    	DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        this.beanFactory = bf;
        this.beanFactory.setParent(this.parentApplicationContext.getBeanFactory());
        loadBeanDefinitions(controllerNames);
        
		refresh();
	}
	
	至此，web环境下的两个applicationcontext都构建好了，WebApplicationContext持有对parentApplicationContext的引用，
	反过来并不持有，单向的引用。
	所以会在WebApplicationContext和parentApplicationContext分别包含beans，这样getBean()的时候要考虑这一层关系，
	先从WebApplicationContext中拿，拿不到的话，再从parentApplicationContext拿。



5.
	我们接下来继续扩展dispatcher。
	以前是在doGet()中进行的如下实现：
		Method method = this.mappingMethods.get(sPath);
		obj = this.mappingObjs.get(sPath);
		objResult = method.invoke(obj);
		response.getWriter().append(objResult.toString());
	简单地根据uri找对应的method和object，然后调用，最后把返回值写到response里。	
	增加了RequestMappingHandlerMapping和RequestMappingHandlerAdapter，分别负责
	包装请求对应的映射和具体的处理过程。
	
	以前在dispatcher中存放的映射数据如下：
	private List<String> urlMappingNames = new ArrayList<>();
    private Map<String,Object> mappingObjs = new HashMap<>();
    private Map<String,Method> mappingMethods = new HashMap<>();
    
	现在不再放在dispatcher中了，取而代之的是在dispatcher中存放：
	private HandlerMapping handlerMapping;
	private HandlerAdapter handlerAdapter;

	在	handlerMapping中通过MappingRegistry存放映射数据：
	public class RequestMappingHandlerMapping implements HandlerMapping {
		WebApplicationContext wac;
		private final MappingRegistry mappingRegistry = new MappingRegistry();
	}
	public class MappingRegistry {
	    private List<String> urlMappingNames = new ArrayList<>();
	    private Map<String,Object> mappingObjs = new HashMap<>();
	    private Map<String,Method> mappingMethods = new HashMap<>();
    }
	
	初始化过程：
	DispatcherServlet中refresh()	{
    	initController();
    	
		initHandlerMappings(this.webApplicationContext);
		initHandlerAdapters(this.webApplicationContext);
    }
    protected void initHandlerMappings(WebApplicationContext wac) {
    	this.handlerMapping = new RequestMappingHandlerMapping(wac);
    }
    protected void initHandlerAdapters(WebApplicationContext wac) {
    	this.handlerAdapter = new RequestMappingHandlerAdapter(wac);
    }
	
	类RequestMappingHandlerMapping对外提供一个getHandler，通过uri拿到method调用。
    public HandlerMethod getHandler(HttpServletRequest request) throws Exception {
		String sPath = request.getServletPath();
	
		Method method = this.mappingRegistry.getMappingMethods().get(sPath);
		Object obj = this.mappingRegistry.getMappingObjs().get(sPath);
		HandlerMethod handlerMethod = new HandlerMethod(method, obj);
		
		return handlerMethod;
	}
    
	具体的调用方法包装成
	public HandlerMethod(Method method, Object obj) {
		this.setMethod(method);
		this.setBean(obj);	
	}
	
	dispatcher在分发的时候变成通过：
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerMethod handlerMethod = this.handlerMapping.getHandler(processedRequest);
		HandlerAdapter ha = this.handlerAdapter;

		ha.handle(processedRequest, response, handlerMethod);
	}
	即通过handlerMapping拿到对应的handlerMethod，然后通过HandlerAdapter进行处理。
	
	HandlerAdapter通过反射invoke具体的方法并处理返回数据(现在仍然只是简单地写到response)：
		Method method = handler.getMethod();
		Object obj = handler.getBean();
		Object objResult = null;
		objResult = method.invoke(obj);
		response.getWriter().append(objResult.toString());
	
	

6.
	提供WebDataBinder,代表的是内部的一个目标对象:
	public WebDataBinder(Object target, String targetName) {
		this.target = target;
		this.objectName = targetName;
		this.clz = this.target.getClass();
	}
	
	WebDataBinder通过doBind()把Request里面的参数自动转成一个对象，过程如下：
	先从request中把参数转成内部的PropertyValues，经过整理(addBindValues())：
	public void bind(HttpServletRequest request) {
		PropertyValues mpvs = assignParameters(request);
		addBindValues(mpvs, request);
		doBind(mpvs);
	}
	注1：PropertyValues是复用了IoC中的。
	注2：assignParameters()简单地拿到request中所有参数，所以这里对多个对象处理不好
	
	然后调用doBind():
	private void doBind(PropertyValues mpvs) {
		applyPropertyValues(mpvs);	
	}
	protected void applyPropertyValues(PropertyValues mpvs) {
		getPropertyAccessor().setPropertyValues(mpvs);
	}
	protected BeanWrapperImpl getPropertyAccessor() {
		return new BeanWrapperImpl(this.target);
	}
	
	BeanWrapperImpl实现了PropertyEditorRegistrySupport，这个里面事先注册了CustomNumberEditor
	和StringEditor。
		this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
		this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
		this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
		this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
		this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
		this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
		this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
		this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
		this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
		this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

		this.defaultEditors.put(String.class, new StringEditor(String.class, true));
	这些Editor关键是实现如下方法：
		void setAsText(String text);
		void setValue(Object value);
		Object getValue();
		String getAsText();
	把格式字符串与Object进行转换，这样支持不同的数字时间格式，把一个串写进去，读一个object出来。
	
	BeanWrapperImpl用这些Editor这么实现值的写入：
	public void setPropertyValue(PropertyValue pv) {
		//拿到合适的getter和setter handler
		BeanPropertyHandler propertyHandler = new BeanPropertyHandler(pv.getName());
		//拿到一个相应类型的Editor
		PropertyEditor pe = this.getDefaultEditor(propertyHandler.getPropertyClz());
		pe.setAsText((String) pv.getValue());
		propertyHandler.setValue(pe.getValue());
	}
	
	
	有了这个WebDataBinder,我们再提供一个WebDataBinderFactory包装一下：
	public class WebDataBinderFactory {
		public WebDataBinder createBinder(HttpServletRequest request, Object target, String objectName) {
			WebDataBinder wbd= new WebDataBinder(target,objectName);
			initBinder(wbd, request);
			return wbd;
		}
		protected void initBinder(WebDataBinder dataBinder, HttpServletRequest request){
		}
	}

	在RequestMappingHandlerAdapter中handleInternal改写方法：
	protected void invokeHandlerMethod(HttpServletRequest request,
						HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
		WebDataBinderFactory binderFactory = new WebDataBinderFactory();
		
		//获取调用方法的所有参数
		Parameter[] methodParameters = handlerMethod.getMethod().getParameters();
		Object[] methodParamObjs = new Object[methodParameters.length];
		int i = 0;
		for (Parameter methodParameter : methodParameters) {
			//对某个参数，新创建一个空的对象，这个就是要bind的目标
			Object methodParamObj = methodParameter.getType().newInstance();
			//对这个参数实现bind
			WebDataBinder wdb = binderFactory.createBinder(request, methodParamObj, methodParameter.getName());
			wdb.bind(request);
			methodParamObjs[i] = methodParamObj;
			i++;
		}
		//至此bind了方法中的所有参数，可以调用了
		Method invocableMethod = handlerMethod.getMethod();
		Object returnobj = invocableMethod.invoke(handlerMethod.getBean(), methodParamObjs);
		
		response.getWriter().append(returnobj.toString());
	}
	
	至此，我们就通过RequestMappingHandlerAdapter中的invokeHandlerMethod把request中的参数自动
	转成了内部的对象。
	但是我们的实现不好，对多个参数处理不好，另外如果方法中有HttpRequest参数也没有跳过。
	
	

7.
	支持自定义的CustomEditor，实现PropertyEditor接口：
	public interface PropertyEditor {
		void setAsText(String text);
		void setValue(Object value);
		Object getValue();
		String getAsText();
	}
	把格式字符串与Object进行转换，这样支持不同的数字时间或者别的类型的格式，把一个串写进去，读一个object出来，
	如CustomDateEditor。
	
	系统提供一个WebBindingInitializer接口，用它注册自定义的CustomEditor:
	public interface WebBindingInitializer {
		void initBinder(WebDataBinder binder);
	}
	
	应用程序员自己实现一个WebBindingInitializer，在initBinder中注册自定义Editor，如自定义日期格式：
	public class DateInitializer implements WebBindingInitializer{
	@Override
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(Date.class,"yyyy-MM-dd", false));
	}
	这个CustomDateEditor类的构造方法：
		public CustomDateEditor(Class<Date> dateClass,
				String pattern, boolean allowEmpty) throws IllegalArgumentException {
			this.dateClass = dateClass;
			this.datetimeFormatter = DateTimeFormatter.ofPattern(pattern);
			this.allowEmpty = allowEmpty;
		}
		
	RequestMappingHandlerAdapter中增加一个属性：
		WebBindingInitializer webBindingInitializer
	构造方法中从getBean("webBindingInitializer")
	public RequestMappingHandlerAdapter(WebApplicationContext wac) {
		this.wac = wac;
		this.webBindingInitializer = (WebBindingInitializer) this.wac.getBean("webBindingInitializer");
	}
	也就是说用户通过applicationContext.xml配置webBindingInitializer：
		<bean id="webBindingInitializer" class="com.test.DateInitializer"> 
		</bean>

	然后HandlerAdapter修改为：
		protected void invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		for (Parameter methodParameter : methodParameters) {
			//对某个参数，新创建一个空的对象，这个就是要bind的目标
			Object methodParamObj = methodParameter.getType().newInstance();
			//对这个参数实现bind
			WebDataBinder wdb = binderFactory.createBinder(request, methodParamObj, methodParameter.getName());
			webBindingInitializer.initBinder(wdb); //注册binder中的editor
			wdb.bind(request);
			methodParamObjs[i] = methodParamObj;
			i++;
		}

	BeanWrapperImpl所继承的类 PropertyEditorRegistrySupport 增加customEditors属性：
		private Map<Class<?>, PropertyEditor> defaultEditors;
		private Map<Class<?>, PropertyEditor> customEditors;
	
	最后BeanWrapperImpl修改为先取CustomEditor再取DefaultEditor：
	public void setPropertyValue(PropertyValue pv) {
		BeanPropertyHandler propertyHandler = new BeanPropertyHandler(pv.getName());
		PropertyEditor pe = this.getCustomEditor(propertyHandler.getPropertyClz());
		if (pe == null) {
			pe = this.getDefaultEditor(propertyHandler.getPropertyClz());
			
		}
		if (pe != null) {
			pe.setAsText((String) pv.getValue());
			propertyHandler.setValue(pe.getValue());
		}
		else {
			propertyHandler.setValue(pv.getValue());			
		}
	}
	
	这样，就支持了用户自定义的CustomEditor.



8.
	支持@ResponseBody
	增加从controller返回给前端的字符流数据格式转换支持。
	public interface HttpMessageConverter {
		void write(Object obj, HttpServletResponse response) throws IOException;
	}
	给了一个默认的实现：DefaultHttpMessageConverter，把object转成Json串。
		String defaultContentType = "text/json;charset=UTF-8";
		String defaultCharacterEncoding = "UTF-8";
		ObjectMapper objectMapper;
		
		public void write(Object obj, HttpServletResponse response) throws IOException {
	        response.setContentType(defaultContentType);
	        response.setCharacterEncoding(defaultCharacterEncoding);
	        writeInternal(obj, response);
	        response.flushBuffer();
		}
		private void writeInternal(Object obj, HttpServletResponse response) throws IOException{
			String sJsonStr = this.objectMapper.writeValuesAsString(obj);
			PrintWriter pw = response.getWriter();
			pw.write(sJsonStr);
		}
		
	
	定义一个ObjectMapper:
		public interface ObjectMapper {
			void setDateFormat(String dateFormat);
			void setDecimalFormat(String decimalFormat);
			String writeValuesAsString(Object obj);
		}
	给了一个默认的实现：DefaultObjectMapper，在	writeValuesAsString中拼JSon串。
			if (value instanceof Date) {
				LocalDate localDate = ((Date)value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				strValue = localDate.format(this.datetimeFormatter);
			}
			else if (value instanceof BigDecimal || value instanceof Double || value instanceof Float){
				strValue = this.decimalFormatter.format(value);
			}
			else {
				strValue = value.toString();
			}
	目前为止，我们也只支持Date, Number和String三种类型。
	
	RequestMappingHandlerAdapter增加两个属性：
	public class RequestMappingHandlerAdapter implements HandlerAdapter {
		private WebBindingInitializer webBindingInitializer = null;
		private HttpMessageConverter messageConverter = null;

	方法invokeHandlerMethod()增加	messageConverter 处理:	
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
			
			... ...
			
			if (invocableMethod.isAnnotationPresent(ResponseBody.class)){ //ResponseBody
		        this.messageConverter.write(returnObj, response);
			}
			
			... ...
	}
		
	
	这些webBindingInitializer和messageConverter都通过配置注入：
	<bean id="handlerAdapter" class="com.minis.web.servlet.RequestMappingHandlerAdapter"> 
	 <property type="com.minis.web.HttpMessageConverter" name="messageConverter" ref="messageConverter"/>
	 <property type="com.minis.web.WebBindingInitializer" name="webBindingInitializer" ref="webBindingInitializer"/>
	</bean>
	
	<bean id="webBindingInitializer" class="com.test.DateInitializer" /> 
	
	<bean id="messageConverter" class="com.minis.web.DefaultHttpMessageConverter"> 
	 <property type="com.minis.web.ObjectMapper" name="objectMapper" ref="objectMapper"/>
	</bean>
	<bean id="objectMapper" class="com.minis.web.DefaultObjectMapper" >
	 <property type="String" name="dateFormat" value="yyyy/MM/dd"/>
	 <property type="String" name="decimalFormat" value="###.##"/>
	</bean>
	
	DispatcherServlet中通过getBean获取handlerAdapter（约定一个名字）。
	protected void initHandlerAdapters(WebApplicationContext wac) {
 		this.handlerAdapter = (HandlerAdapter) wac.getBean(HANDLER_ADAPTER_BEAN_NAME);
    }
    
	客户程序HelloWorldBean:
		@RequestMapping("/test7")
		@ResponseBody
		public User doTest7(User user) {
			user.setName(user.getName() + "---");
			user.setBirthday(new Date());
			return user;
		}	
    
	完成数据的转换之后，我们接着实现ModelAndView：
	public class ModelAndView {
		private Object view;
		private Map<String, Object> model = new HashMap<>();
	}
	
	类RequestMappingHandlerAdapter的	方法invokeHandlerMethod() 返回值改为ModelAndView:	
		protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

			... ...
					
			ModelAndView mav = null;
			if (invocableMethod.isAnnotationPresent(ResponseBody.class)){ //ResponseBody
		        this.messageConverter.write(returnObj, response);
			}
			else {
				if (returnObj instanceof ModelAndView) {
					mav = (ModelAndView)returnObj;
				}
				else if(returnObj instanceof String) {
					String sTarget = (String)returnObj;
					mav = new ModelAndView();
					mav.setViewName(sTarget);
				}
			}
			
			return mav;
		}
	
	DispatcherServlet修改：
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerMethod handlerMethod = null;
		ModelAndView mv = null;
		
		handlerMethod = this.handlerMapping.getHandler(processedRequest);
		mv = ha.handle(processedRequest, response, handlerMethod);

		render(processedRequest, response, mv);
	}
	
	//用jsp 进行render
	protected void render( HttpServletRequest request, HttpServletResponse response,ModelAndView mv) throws Exception {
		if (mv == null) { //@ResponseBody
			response.getWriter().flush();
			response.getWriter().close();
			return;
		}
		//获取model，写到request的Attribute中：
		Map<String, Object> modelMap = mv.getModel();
		for (Map.Entry<String, Object> e : modelMap.entrySet()) {
			request.setAttribute(e.getKey(),e.getValue());
		}
		
		String sTarget = mv.getViewName();
		String sPath = "/" + sTarget + ".jsp";
		request.getRequestDispatcher(sPath).forward(request, response);
	}
	
	客户程序HelloWorldBean:
		@RequestMapping("/test5")
		public ModelAndView doTest5(User user) {
			ModelAndView mav = new ModelAndView("test","msg",user.getName());
			return mav;
		}

	

9.
	支持前端View，核心是render().
	public interface View {
		void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response);
	}
	Controller返回值增加ModelAndView：
	public class ModelAndView {
		private Object view;
		private Map<String, Object> model = new HashMap<>();
	}

	DispatcherServlet增加属性：
		private ViewResolver viewResolver;
		
	DispatcherServlet初始化后刷新：	
		protected void Refresh() {
			initHandlerMappings(this.webApplicationContext);
			initHandlerAdapters(this.webApplicationContext);
			initViewResolvers(this.webApplicationContext);
    	}

	RequestMappingHandlerAdapter调用invokeHandlerMethod拿到返回值后进行如下处理：
				if (returnObj instanceof ModelAndView) {
					mav = (ModelAndView)returnObj;
				}
				else if(returnObj instanceof String) {
					String sTarget = (String)returnObj;
					mav = new ModelAndView();
					mav.setViewName(sTarget);
				}
				
	DispatcherServlet调用RequestMappingHandlerAdapter的invokeHandlerMethod后处理范式的ModelAndView，
		mv = ha.handle(processedRequest, response, handlerMethod);
		render(processedRequest, response, mv);
	
	render的实现，就是先按照规则找到具体的view(某个jdp页面)，拿到model数据，然后再render：	
	protected void render( HttpServletRequest request, HttpServletResponse response,ModelAndView mv) throws Exception {
		String sTarget = mv.getViewName();
		Map<String, Object> modelMap = mv.getModel();
		View view = resolveViewName(sTarget, modelMap, request);
		view.render(modelMap, request, response);
	}
	
	提供一个默认的JstlView实现：
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		for (Entry<String, ?> e : model.entrySet()) {
			request.setAttribute(e.getKey(),e.getValue());
		}
		
		request.getRequestDispatcher(getUrl()).forward(request, response);
	}
	
	这些也是通过配置注入的：
	<bean id="viewResolver" class="com.minis.web.servlet.view.InternalResourceViewResolver" >
	 <property type="String" name="viewClassName" value="com.minis.web.servlet.view.JstlView" />
	 <property type="String" name="prefix" value="/jsp/" />
	 <property type="String" name="suffix" value=".jsp" />
    </bean>
		
    另外，我们把容器的listener, beanfactorypostprocessor还有beanpostprocessor都配置化了。
    	<bean id="contextListener" class="com.test.MyListener" />

    	<bean id="beanFactoryPostProcessor" class="com.test.MyBeanFactoryPostProcessor" />

    	<bean id="autowiredAnnotationBeanPostProcessor" class="com.minis.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor" />
    	<bean id="logBeanPostProcessor" class="com.test.LogBeanPostProcessor" />
    名字不重要，是通过类型匹配进行注入的。

    至此，有了完整的MVC实现。
		


-----------------------------------JDBC--------------------------------------
1.
	提供一个JdbcTemplate抽象类，实现基本JDBC的访问框架代码：
	现在只实现数据查询。
	public Object query(String sql) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Object rtnObj = null;
		
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			stmt = con.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			rtnObj = doInStatement(rs);
		
		return rtnObj;
	}
	
	真正的业务代码留给用户自己实现doInStatement(),如实现User类：
	public class UserJdbcImpl extends JdbcTemplate {
	@Override
	protected Object doInStatement(ResultSet rs) {
		User rtnUser = null;
			if (rs.next()) {
				rtnUser = new User();
				rtnUser.setId(rs.getInt("id"));
				rtnUser.setName(rs.getString("name"));
				rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
			}
		
		return rtnUser;
	}
	
	原有的UserService改成：
	public class UserService {
		public User getUserInfo(int userid) {
			String sql = "select id, name,birthday from users where id="+userid;
			JdbcTemplate jdbcTemplate = new UserJdbcImpl();
			User rtnUser = (User)jdbcTemplate.query(sql);
			
			return rtnUser;
		}
	}

	这一步仅仅只做到了把JDBC查询语句中的格式化的语句抽离出来，让应用程序员只需要管sql语句并返回数据对象。
	



2.
	为了不让每个实体类都对应一个UserJdbcImpl类，写成callback模式，让用户自己动态写入：
	public Object query(StatementCallback stmtcallback) {
		Connection con = null;
		Statement stmt = null;
		
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			stmt = con.createStatement();
			
			return stmtcallback.doInStatement(stmt);
	}
	
	同时支持PreparedStatement：
	public Object query(String sql, Object[] args, PreparedStatementCallback pstmtcallback) {
		Connection con = null;
		PreparedStatement pstmt = null;
		
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			pstmt = con.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					Object arg = args[i];
					if (arg instanceof String) {
						pstmt.setString(i+1, (String)arg);
					}
					else if (arg instanceof Integer) {
						pstmt.setInt(i+1, (int)arg);
					}
					else if (arg instanceof java.util.Date) {
						pstmt.setDate(i+1, new java.sql.Date(((java.util.Date)arg).getTime()));
						
					}
				}
			}
			
			return pstmtcallback.doInPreparedStatement(pstmt);
	}

	两个Callback接口如下：
	public interface StatementCallback {
		Object doInStatement(Statement stmt) throws SQLException;
	}
	public interface PreparedStatementCallback {
		Object doInPreparedStatement(PreparedStatement stmt) throws SQLException;
	}
	
	这样应用程序员就只需要用一个JdbcTemplate类就可以了，不用为每一个业务类单独再做一个.

	用户类改成使用Callback动态匿名类：
	public User getUserInfo(int userid) {
		final String sql = "select id, name,birthday from users where id=?";
		return (User)jdbcTemplate.query(sql, new Object[]{new Integer(userid)},
				(pstmt)->{			
					ResultSet rs = pstmt.executeQuery();
					User rtnUser = null;
					if (rs.next()) {
						rtnUser = new User();
						rtnUser.setId(userid);
						rtnUser.setName(rs.getString("name"));
						rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
					} else {
					}
					return rtnUser;
				}
		);
	}
	
	由于只需要一个JdbcTemplate，我们就可以事先在IoC容器中声明这个bean，然后自动注入进来。
	<bean id="userService" class="com.test.service.UserService" /> 
	<bean id="jdbcTemplate" class="com.minis.jdbc.core.JdbcTemplate" /> 
		
	public class UserService {
		@Autowired
		JdbcTemplate jdbcTemplate;
	}
	这里采用的是按照名字匹配注入。
	
	controller再注入service：
	public class HelloWorldBean {
		@Autowired
		UserService userService;
		
		@RequestMapping("/test8")
		@ResponseBody
		public User doTest8(HttpServletRequest request, HttpServletResponse response) {
			int userid = Integer.parseInt(request.getParameter("id"));
			User user = userService.getUserInfo(userid);		
			return user;
		}	
	}
		
	
3.
	我们看到，JdbcTemplate中获取数据库连接信息仍然是hard coded：
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");
	我们把这一部分代码包装成DataSource：
			con = dataSource.getConnection();

	并通过属性注入：
	public class JdbcTemplate {
		private DataSource dataSource;
	}
	
	配置文件：
	<bean id="dataSource" class="com.minis.jdbc.datasource.SingleConnectionDataSource">
	<property type="String" name="driverClassName" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>
	<property type="String" name="url" value="jdbc:sqlserver://localhost:1433;databasename=DEMO;"/>
	<property type="String" name="username" value="sa"/>
	<property type="String" name="password" value="Sql2016"/>
    </bean>
                
	<bean id="jdbcTemplate" class="com.minis.jdbc.core.JdbcTemplate" >
	<property type="javax.sql.DataSource" name="dataSource" ref="dataSource"/>
	</bean> 

	DataSource定义如下：
	public class SingleConnectionDataSource implements DataSource {
		private String driverClassName;
		private String url;
		private String username;
		private String password;
		private Properties connectionProperties;	
		private Connection connection;
		
		@Override
		public Connection getConnection() throws SQLException {
			return getConnectionFromDriver(getUsername(), getPassword());
		}
	}
	
	这样在bean初始化的时候，设置Property的时候load相应的JDBC Driver，然后注入JdbcTemplate来使用。
	在应用程序dataSource.getConnection()的时候才实际生成数据库连接。


4.
	现在往PreparedStatement中传参数是这么实现的：
				for (int i = 0; i < args.length; i++) {
					Object arg = args[i];
					if (arg instanceof String) {
						pstmt.setString(i+1, (String)arg);
					}
					else if (arg instanceof Integer) {
						pstmt.setInt(i+1, (int)arg);
					}
					else if (arg instanceof java.util.Date) {
						pstmt.setDate(i+1, new java.sql.Date(((java.util.Date)arg).getTime()));
					}
				}
	我们现在修改一下，把jdbc中传参数的代码进行包装：ArgumentPreparedStatementSetter
	通过setValues()把参数传进PreparedStatement.
		public class ArgumentPreparedStatementSetter {
			private final Object[] args;
		
			public ArgumentPreparedStatementSetter(Object[] args) {
				this.args = args;
			}
		
			public void setValues(PreparedStatement pstmt) throws SQLException {
				for (int i = 0; i < this.args.length; i++) {
					Object arg = this.args[i];
					doSetValue(pstmt, i + 1, arg);
				}
			}
		
			protected void doSetValue(PreparedStatement pstmt, int parameterPosition, Object argValue) throws SQLException {
				Object arg = argValue;
				if (arg instanceof String) {
					pstmt.setString(parameterPosition, (String)arg);
				}
				else if (arg instanceof Integer) {
					pstmt.setInt(parameterPosition, (int)arg);
				}
				else if (arg instanceof java.util.Date) {
					pstmt.setDate(parameterPosition, new java.sql.Date(((java.util.Date)arg).getTime()));	
				}
			}
		}
	
	所以，Query()就修改成这个样子：
		public Object query(String sql, Object[] args, PreparedStatementCallback pstmtcallback) {
			Connection con = null;
			PreparedStatement pstmt = null;
			
			con = dataSource.getConnection();
			pstmt = con.prepareStatement(sql);
			ArgumentPreparedStatementSetter argumentSetter = new ArgumentPreparedStatementSetter(args);	
			argumentSetter.setValues(pstmt);
			
			return pstmtcallback.doInPreparedStatement(pstmt);
		}

	接下来，我们再把接受返回值的代码进行包装：RowMapperResultSetExtractor。
	先提供一个接口RowMapper，把JDBC resultset的某一行数据映射成为一个对象：
	public interface RowMapper<T> {
		T mapRow(ResultSet rs, int rowNum) throws SQLException;
	}
	再提供一个接口ResultSetExtractor，把JDBC ResultSet数据映射为一个集合对象：
	public interface ResultSetExtractor<T> {
		T extractData(ResultSet rs) throws SQLException;
	}
	利用上面的两个接口，我们事先RowMapperResultSetExtractor：
	public class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
		private final RowMapper<T> rowMapper;
	
		public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
			this.rowMapper = rowMapper;
		}
	
		public List<T> extractData(ResultSet rs) throws SQLException {
			List<T> results = new ArrayList<>();
			int rowNum = 0;
			while (rs.next()) {
				results.add(this.rowMapper.mapRow(rs, rowNum++));
			}
			return results;
		}
	}

	有了传入和返回数据的包装，query()修改如下：
	public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
		RowMapperResultSetExtractor<T> resultExtractor = new RowMapperResultSetExtractor<>(rowMapper);
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		con = dataSource.getConnection();

		pstmt = con.prepareStatement(sql);
		ArgumentPreparedStatementSetter argumentSetter = new ArgumentPreparedStatementSetter(args);	
		argumentSetter.setValues(pstmt);
		rs = pstmt.executeQuery();
		
		return resultExtractor.extractData(rs);
	}
	
	那么应用程序的service层改成这样：
	public List<User> getUsers(int userid) {
		final String sql = "select id, name,birthday from users where id>?";
		return (List<User>)jdbcTemplate.query(sql, new Object[]{new Integer(userid)},
						new RowMapper<User>(){
							public User mapRow(ResultSet rs, int i) throws SQLException {
								User rtnUser = new User();
								rtnUser.setId(rs.getInt("id"));
								rtnUser.setName(rs.getString("name"));
								rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
		
								return rtnUser;
							}
						}
		);
	}
		
	到此为止，JdbcTemplate就提供三种query()了：
	public Object query(StatementCallback stmtcallback) {}
	public Object query(String sql, Object[] args, PreparedStatementCallback pstmtcallback) {}
	public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper){}
		

		

5.
	支持PooledConnection，用Active表示是否忙着，实际上永不close：
	public PooledConnection(Connection connection, boolean active) {
		this.connection = connection;
		this.active = active;

		public void setActive(boolean active) {
			this.active = active;
		}
		public void close() throws SQLException {
			this.active = false;
		}
		
	把DataSource改成PooledDataSource，初始化的时候激活所有的数据库链接：
	public class PooledDataSource implements DataSource{
		private List<PooledConnection> connections = null;
		private String driverClassName;
		private String url;
		private String username;
		private String password;
		private int initialSize = 2;
		private Properties connectionProperties;	
		
		private void initPool() {
			this.connections = new ArrayList<>(initialSize);
			for(int i = 0; i < initialSize; i++){
				Connection connect = DriverManager.getConnection(url, username, password);
				PooledConnection pooledConnection = new PooledConnection(connect, false);
				this.connections.add(pooledConnection);
			}
		}
		
		获取数据库连接的代码如下：
		PooledConnection pooledConnection= getAvailableConnection();
		while(pooledConnection == null){
			pooledConnection = getAvailableConnection();
			if(pooledConnection == null){
				try {
					TimeUnit.MILLISECONDS.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}		
		return pooledConnection;
	可以看出，策略是死等这一个有效的连接。

	获取有效的连接的代码：
		private PooledConnection getAvailableConnection() throws SQLException{
			for(PooledConnection pooledConnection : this.connections){
				if (!pooledConnection.isActive()){
					pooledConnection.setActive(true);
					return pooledConnection;
				}
			}
	
			return null;
		}
	可以看出，其实是拿一个空闲标志的数据库连接。	
	
	通过配置注入这个datasource：
	<bean id="dataSource" class="com.minis.jdbc.pool.PooledDataSource">  
                <property name="url" value="jdbc:sqlserver://localhost:1433;databasename=DEMO"/>  
                <property name="driverClassName" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>  
                <property name="username" value="sa"/>  
                <property name="password" value="Sql2016"/>  
                <property type="int" name="initialSize" value="3"/>  
    </bean>
		
	别的程序没有任何变化。
	
	
		
-----------------------------------AOP--------------------------------------
1. 
	第一步先直接用Java 动态代理编程实现Aop：
	代理interface上的doAction()方法。
	public class DynamicProxy {
		private Object subject = null; 
		
		public DynamicProxy(Object subject) {
				this.subject = subject;
		}
		
		public Object getProxy() {
			return Proxy.newProxyInstance(DynamicProxy.class
					.getClassLoader(), subject.getClass().getInterfaces(),
					new InvocationHandler() {
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if (method.getName().equals("doAction")) {
						 System.out.println("before call real object........");
						 return method.invoke(subject, args); 
					}
					return null;
				}
			});
		}
	}
	
	Java的机制是在interface上代理，所以我们定义一个接口：
	public interface IAction {
		void doAction();
	}

	应用程序利用代理进行包装，实际的工作类注入。
	<bean id="action" class="com.test.service.Action1" /> 
	
	应用程序：
	@Autowired
	IAction action;
	
	@RequestMapping("/testaop")
	public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
		DynamicProxy proxy = new DynamicProxy(action);
		IAction p = (IAction)proxy.getProxy();
		p.doAction();
	}

		
2. 	
	直接写Proxy，对应用程序有介入，不是好方案，要考虑非介入式的。
	
	我们通过配置一个ProxyFactoryBean实现aop
	<bean id="action" class="com.minis.aop.ProxyFactoryBean" >
        <property type="java.lang.Object" name="target" ref="realaction"/>	
	</bean>
	factorybean的特点是它里面包含有一个target，指向真正工作的对象，在容器里面放入的是这个 FactoryBean,
	而不是实际上工作的那个bean。当getBean()的时候，对于factorybean进行特殊处理，返回指向的这个真正工作的对象。
	FactoryBean接口定义如下：
	public interface FactoryBean<T> {
		T getObject() throws Exception;
		Class<?> getObjectType();
	}
	
	改写AbstractBeanFactory：	AbstractBeanFactory extends FactoryBeanRegistrySupport 
	在AbstractBeanFactory的getBean()的时候特殊处理FactoryBean，不是返回这个FactoryBean本身，而是返回它里面生成的object：
	    if (singleton instanceof FactoryBean) {
        	return this.getObjectForBeanInstance(singleton, beanName);
        }
        
	getObjectForBeanInstance()进一步调用到  FactoryBeanRegistrySupport里面的    getObjectFromFactoryBean
    private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) {
		return factory.getObject();
	}
	通过这个手段，拿到factorybean指向的那个object。
	
	实际上这个object也并不是真正工作的那个object，如果那样就不能插入额外的逻辑了，所以其实是这个object的一个代理。
	ProxyFactoryBean在getObject()中生成了一个代理getProxy(createAopProxy())，所以实际上BeanFactory
	对于这个ProxyFactoryBean,它拿到的是一个动态代理类代理了target。
	public Object getObject() throws Exception {
		return getSingletonInstance();
	}
	private synchronized Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			this.singletonInstance = getProxy(createAopProxy());
		}
		return this.singletonInstance;
	}
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy();
	}
	protected AopProxy createAopProxy() {
		return getAopProxyFactory().createAopProxy(target);
	}
	
	程序中默认了一个代理工厂，直接创建JdkDynamicAopProxy（预留了CglibAopProxy的支持）：
	public class DefaultAopProxyFactory implements AopProxyFactory {
		public AopProxy createAopProxy(Object target) {
			//if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(target);
			//}
			//return new CglibAopProxy(config);
		}
	}
	
	JdkDynamicAopProxy利用Java的动态代理技术代理了target：
	public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
		Object target;
		
		public JdkDynamicAopProxy(Object target) {
			this.target = target;
		}
	
		@Override
		public Object getProxy() {
			Object obj = Proxy.newProxyInstance(JdkDynamicAopProxy.class.getClassLoader(), target.getClass().getInterfaces(), this);
			return obj;
		}
	
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("doAction")) {
				 System.out.println("-----before call real object, dynamic proxy........");
				 return method.invoke(target, args); 
			}
			return null;
		}
	}
	
	应用程序，就不需要写死Proxy代码了：
	@Autowired
	IAction action;
	
	@RequestMapping("/testaop")
	public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
		action.doAction();
	}
	
	配置文件：
	<bean id="realaction" class="com.test.service.Action1" /> 
	<bean id="action" class="com.minis.aop.ProxyFactoryBean" >
        <property type="java.lang.Object" name="target" ref="realaction"/>	
	</bean> 
 