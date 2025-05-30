# csv




## 说明：

1. 实体类需要有无参构造，用来反射对象
2. Get-Set一定要用，用来反射



## 读取


```java
List<TestCsv> csvEntities = new EasyCsv().readAll("D://test2.csv", TestCsv.class);
csvEntities.forEach(System.out::println);
```
