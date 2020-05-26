package com.minis.beans;

public class BeanDefinition {
	String SCOPE_SINGLETON = "singleton";
	String SCOPE_PROTOTYPE = "prototype";
	
	private boolean lazyInit = true;
	private String[] dependsOn;
	private ArgumentValues constructorArgumentValues;

	private PropertyValues propertyValues;
	private String initMethodName;
	
	private volatile Object beanClass;
    private String id;
    private String className;
    private String scope=SCOPE_SINGLETON;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    
    public BeanDefinition(String id, String className) {
        this.id = id;
        this.className = className;
    }
    
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}
	
	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}
	
	public Class<?> getBeanClass(){

		return (Class<?>) this.beanClass;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getScope() {
		return this.scope;
	}
	
	public boolean isSingleton() {
		return SCOPE_SINGLETON.equals(scope);
	}

	public boolean isPrototype() {
		return SCOPE_PROTOTYPE.equals(scope);
	}
	
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public boolean isLazyInit() {
		return this.lazyInit;
	}
	
	public void setDependsOn(String... dependsOn) {
		this.dependsOn = dependsOn;
	}

	public String[] getDependsOn() {
		return this.dependsOn;
	}
	
	public void setConstructorArgumentValues(ArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues =
				(constructorArgumentValues != null ? constructorArgumentValues : new ArgumentValues());
	}

	public ArgumentValues getConstructorArgumentValues() {
		return this.constructorArgumentValues;
	}

	public boolean hasConstructorArgumentValues() {
		return !this.constructorArgumentValues.isEmpty();
	}
	public void setPropertyValues(PropertyValues propertyValues) {
		this.propertyValues = (propertyValues != null ? propertyValues : new PropertyValues());
	}

	public PropertyValues getPropertyValues() {
		return this.propertyValues;
	}
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	public String getInitMethodName() {
		return this.initMethodName;
	}
    
}
