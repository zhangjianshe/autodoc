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
package cn.mapway.document.meta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.print.DocFlavor.READER;

import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.POST;
import org.nutz.resource.Scans;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.mapway.document.annotation.Doc;
import cn.mapway.document.gen.module.GenContext;
import cn.mapway.document.meta.module.ApiDocument;
import cn.mapway.document.meta.module.ApiEntry;
import cn.mapway.document.meta.module.ApiGroup;
import cn.mapway.document.meta.module.ParameterInfo;

/**
 * 处理Spring注解 生成 ApiDocument对象
 * 
 * @author zhangjianshe@navinfo.com
 * 
 */
public class SpringParser extends DocAnotationBase {
	/**
	 * 处理包里和子包中拥有Doc注释的Class
	 * 
	 * @param packageName
	 * @return
	 */
	public ApiDocument parsePackage(String packageName, GenContext context) {
		List<Class<?>> clzs = Scans.me().scanPackage(packageName);

		System.out.println("find resource " + clzs.size());
		ApiDocument doc = new ApiDocument();
		doc.author = context.getAuthor();
		doc.basePath = context.getBasepath();
		doc.title = context.getDocTitle();

		for (Class<?> clz : clzs) {
			if (clz.getAnnotation(Controller.class) != null
					|| clz.getAnnotation(RestController.class) != null) {
				parseClass(doc, clz);
			}
		}
		return doc;

	}

	/**
	 * 解析类
	 * 
	 * @param apiDoc
	 * @param clz
	 */
	private void parseClass(ApiDocument apiDoc, Class<?> clz) {
		Doc doc = clz.getAnnotation(Doc.class);
		if (doc == null) {
			System.out.println("Document " + clz.getName()
					+ " is not annotated with Doc");
			return;
		}
		String path = doc.group();
		ApiGroup apigroup = apiDoc.findGroup(path);
		populateGroup(apigroup, clz);

	}

	/**
	 * 填充APiGroup信息
	 * 
	 * @param apiGroup
	 * @param c
	 */
	private void populateGroup(ApiGroup apiGroup, Class<?> c) {
		String basepath = "";
		RequestMapping rm = c.getAnnotation(RequestMapping.class);
		if (rm != null) {
			String[] paths = rm.value();
			if (paths == null || paths.length == 0) {

			} else {
				basepath = paths[0];
			}
		}

		Method[] methods = c.getDeclaredMethods();
		List<Method> list = new ArrayList<Method>();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			RequestMapping rm1 = m.getAnnotation(RequestMapping.class);
			if (rm1 != null) {
				list.add(m);
			}
		}

		Collections.sort(list, new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());

			}
		});

		for (int i = 0; i < list.size(); i++) {
			Method m = list.get(i);

			ApiEntry entry = handleMethod(m);
			if (entry != null) {
				entry.relativePath = basepath + entry.relativePath;
				apiGroup.entries.add(entry);
			}
		}
	}

	/**
	 * @param c
	 * @return
	 */
	public ApiDocument toApiDocument(Class<?> c) {
		ApiDocument api = new ApiDocument();

		api.clsName = c.getSimpleName();
		At rm = c.getAnnotation(At.class);
		if (rm != null) {
			String[] paths = rm.value();
			if (paths == null || paths.length == 0) {

			} else {
				api.basePath = paths[0];
			}
		}

		Doc summary = c.getAnnotation(Doc.class);
		if (summary != null) {
			api.title = summary.value();
			api.author = summary.author();

		}

		Method[] methods = c.getDeclaredMethods();
		List<Method> list = new ArrayList<Method>();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			At rm1 = m.getAnnotation(At.class);
			if (rm1 != null) {
				list.add(m);
			}
		}

		Collections.sort(list, new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());

			}
		});

		for (int i = 0; i < list.size(); i++) {
			Method m = list.get(i);
			if (m.getName().equals("index")) {
				continue;
			}
			ApiEntry entry = handleMethod(m);
			if (entry != null) {
				// api.entries.add(entry);
			}
		}

		return api;
	}

	/**
	 * @param m
	 * @return
	 */
	private ApiEntry handleMethod(Method m) {

		ApiEntry e = new ApiEntry();

		RequestMapping rm = m.getAnnotation(RequestMapping.class);
		if (rm != null) {
			String[] paths = rm.value();
			if (paths == null || paths.length == 0) {

			} else {
				e.relativePath = paths[0];
			}

			e.invokeMethod = "GET";
			RequestMethod[] ms = rm.method();
			if (ms != null) {
				for (int i = 0; i < ms.length; i++) {
					RequestMethod rm0 = ms[i];
					if (rm0.equals(RequestMethod.POST)) {

						e.invokeMethod = "POST";
						break;
					}
				}
			}
		}

		if (e.relativePath.length() == 0) {
			return null;
		}

		e.methodName = m.getName();

		Doc summary = m.getAnnotation(Doc.class);
		if (summary != null) {
			e.name = summary.value();
			e.summary = summary.desc();

		}

		Class<?>[] ps = m.getParameterTypes();
		Class<?> out = m.getReturnType();

		for (Class<?> clz : ps) {
			String name = clz.getSimpleName();
			if (name.startsWith("Http")) {
				continue;
			} else {
				ParameterInfo p = handleParameter(clz);
				if (p != null) {
					e.input.add(p);
				}
			}
		}
		e.output = handleParameter(out);
		return e;
	}

}
