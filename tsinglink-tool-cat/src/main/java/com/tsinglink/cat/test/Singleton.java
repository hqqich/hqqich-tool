package com.tsinglink.cat.test;

public class Singleton {

	//私有化构造函数
	private Singleton() {
	}

	//对外暴露一个获取Singleton对象的静态方法
	public static Singleton getInstance() {
		return SingletonEnum.INSTANCE.getInstnce();
	}

	public static void main(String[] args) {
		System.out.println(Singleton.getInstance());
		System.out.println(Singleton.getInstance());
		System.out.println(Singleton.getInstance() == Singleton.getInstance());
	}

	//定义一个静态枚举类
	static enum SingletonEnum {
		//创建一个枚举对象，该对象天生为单例
		INSTANCE;
		private Singleton singletion;

		//私有化枚举的构造函数
		private SingletonEnum() {
			singletion = new Singleton();
		}

		public Singleton getInstnce() {
			return singletion;
		}
	}

}
