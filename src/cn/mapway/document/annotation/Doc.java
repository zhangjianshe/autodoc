/******************************************************************************
<pre>

           =============================================================
           -   ____ _  _ ____ _  _ ____  _ _ ____ _  _ ____ _  _ ____  -
           -    __] |__| |__| |\ | | __  | | |__| |\ | [__  |__| |___  -
           -   [___ |  | |  | | \| |__| _| | |  | | \| ___] |  | |___  -
           -           http://hi.baidu.com/zhangjianshe                -
           =============================================================

</pre>
 *******************************************************************************/
package cn.mapway.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解类
 * 
 * @author zhangjianshe@navinfo.com
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE,
		ElementType.FIELD })
public @interface Doc {
	/**
	 * 描述信息,对要描述的类和方法做出解释和说明/title
	 * 
	 * @return
	 */
	public String value();

	/**
	 * 描述信息
	 * 
	 * @return
	 */
	public String desc() default "";

	/**
	 * 作者
	 * 
	 * @return
	 */
	public String author() default "zhangjianshe@gmail.com";
	
	/**
	 * 接口分组信息 ,以 /为分隔符 进行树形分组，缺省归属为根节点
	 * @return
	 */
	public String group() default "/";
	
	
}
