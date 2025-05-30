package io.github.hqqich.csv.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * excelparse注解
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvProperty {

	/**
	 * Excel列的的标题
	 *
	 * @return
	 */
	String value() default "";

	int index() default -1;
}
