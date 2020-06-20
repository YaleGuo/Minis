# Minis

A mini Spring platform for learning. We plan to implement IoC,MVC,JDBCTemplate and AOP from scratch.

IoC is the core of Minis. We will use a bean factory to manage all required beans.

mvc,integrate with IoC. mapping request uri to controller method,
use handleadapter to invocate method and process response,
and use view resolver to render return message (JSP). 

Use jdbctemplate to access database, and connectionpool supported.