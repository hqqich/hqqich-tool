package com.tsinglink;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
public class Main {

	public static void main(String[] args) {

        System.out.println(System.getProperty("file.encoding"));
        //
		//List<TestCsv> csvEntities = new EasyCsv(Rule.builder().split(",").build()).readAll(
		//		"D://test2.csv", TestCsv.class
		//);
        //
		//csvEntities.forEach(System.out::println);
        //
        //
		//csvEntities.add(new TestCsv("5", "5", "5", "5"));
        //
		//new EasyCsv().write("D://test2.csv", csvEntities);

	}
}
