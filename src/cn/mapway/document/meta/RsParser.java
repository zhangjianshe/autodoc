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

import org.nutz.mvc.annotation.POST;

import cn.mapway.document.annotation.Doc;
import cn.mapway.document.meta.module.ApiDocument;
import cn.mapway.document.meta.module.ApiEntry;
import cn.mapway.document.meta.module.ParameterInfo;

/**
 * 处理Nutz注解 生成 ApiDocument对象
 * 
 * @author zhangjianshe@navinfo.com
 * 
 */
public class RsParser extends DocAnotationBase {
	/**
	 * @param c
	 * @return
	 */
	public ApiDocument toApiDocument(Class<?> c) {
		ApiDocument api = new ApiDocument();

//		api.clsName = c.getSimpleName();
//		Path rm = c.getAnnotation(Path.class);
//		if (rm != null) {
//			api.basePath = rm.value();
//		}
//		if (!api.basePath.endsWith("/")) {
//			api.basePath += "/";
//		}
//
//		Summary summary = c.getAnnotation(Summary.class);
//		if (summary != null) {
//			api.title = summary.value();
//			api.author = summary.author();
//		}
//
//		Method[] methods = c.getDeclaredMethods();
//		List<Method> list = new ArrayList<Method>();
//		for (int i = 0; i < methods.length; i++) {
//			Method m = methods[i];
//			Path rm1 = m.getAnnotation(Path.class);
//			if (rm1 != null) {
//				list.add(m);
//			}
//		}
//
//		Collections.sort(list, new Comparator<Method>() {
//			@Override
//			public int compare(Method o1, Method o2) {
//				return o1.getName().compareTo(o2.getName());
//
//			}
//		});
//
//		System.out.println(Json.toJson(api, JsonFormat.forLook()));
//		for (int i = 0; i < list.size(); i++) {
//			Method m = list.get(i);
//			if (m.getName().equals("index")) {
//				continue;
//			}
//			ApiEntry entry = handleMethod(m);
//			if (entry != null) {
//				api.entries.add(entry);
//			}
//		}
		return api;
	}

	/**
	 * @param m
	 * @return
	 */
	private ApiEntry handleMethod(Method m) {

		ApiEntry e = new ApiEntry();

//		Path rm = m.getAnnotation(Path.class);
//		if (rm != null) {
//			e.relativePath = rm.value();
//
//		}

		if (e.relativePath.length() == 0) {
			return null;
		}

		Doc summary = m.getAnnotation(Doc.class);
		if (summary != null) {
			e.title = summary.title();
		}

		POST post = m.getAnnotation(POST.class);
		if (post == null) {
			e.invokeMethod = "GET";
		} else {
			e.invokeMethod = "POST";
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
