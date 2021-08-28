# Minis

A mini Spring platform for learning. We plan to implement IoC,MVC,JDBCTemplate,AOP and ThreadPool from scratch.

IoC is the core of Minis. We will use a bean factory to manage all required beans.

mvc,integrate with IoC. mapping request uri to controller method,
use handleadapter to invocate method and process response,
and use view resolver to render return message (JSP). 

Use jdbctemplate to access database, and connectionpool supported.
PooledConnection supported.

FactoryBean supported. Use JDKDynamicsProxy technology.

methodinterceptor，methodbeforeadvice, afterreturningadvice supported.

 implements Pointcut and AutoProxyCreator.

Using annotation + aop implements @Async
 