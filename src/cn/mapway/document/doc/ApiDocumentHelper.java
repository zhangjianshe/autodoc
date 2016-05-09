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
package cn.mapway.document.doc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Times;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.gen.GenClassInfo;
import cn.mapway.document.gen.module.GenContext;
import cn.mapway.document.meta.DocAnotationBase;
import cn.mapway.document.meta.module.ApiDocument;
import cn.mapway.document.meta.module.ApiEntry;
import cn.mapway.document.meta.module.ApiGroup;
import cn.mapway.document.meta.module.FieldInfo;
import cn.mapway.document.meta.module.ParameterInfo;
import cn.mapway.document.util.Template;

/**
 * @author zhangjianshe@navinfo.com
 * 
 */
public class ApiDocumentHelper extends DocAnotationBase {
	private ArrayList<GenClassInfo> objects = new ArrayList<GenClassInfo>();

	private void addGenClass(ParameterInfo t) {
		boolean find = false;
		for (int i = 0; i < objects.size(); i++) {
			GenClassInfo info = objects.get(i);
			String n1 = info.cls.clz.getName();
			String n2 = t.clz.getName();

			if (n1.equals(n2)) {
				find = true;
				break;
			}
		}
		if (find == false) {
			GenClassInfo info = new GenClassInfo();
			info.cls = t;
			info.gen = false;
			objects.add(info);
		}
	}

	/**
	 * @param api
	 * @return
	 */
	public String gendoc(ApiDocument api, GenContext context) {

		if (context.getDocTitle() != null && context.getDocTitle().length() > 0) {
			api.title = context.getDocTitle();
		}
		if (context.getAuthor() != null && context.getAuthor().length() > 0) {
			api.author = context.getAuthor();
		}

		StringBuilder sb = new StringBuilder();
		StringBuilder catalog = new StringBuilder();
		StringBuilder apiIndex = new StringBuilder();

		// Catalog is tree
		catalog.append("<div>");
		sb.append("");
		String author = api.author;

		int indent = 1;
		for (ApiGroup g : api.root.getChildGroups()) {
			handlerGroup(indent, g, catalog, sb, apiIndex);
		}
		catalog.append("</div>");

		// output field type
		StringBuilder sb1 = new StringBuilder();

		ArrayList<GenClassInfo> objs = new ArrayList<GenClassInfo>();
		for (GenClassInfo info : objects) {
			if (info.gen == false) {
				objs.add(info);
			}
		}

		while (objs.size() > 0) {
			for (int i = 0; i < objs.size(); i++) {
				GenClassInfo info = objs.get(i);
				sb1.append("<div class='m_block'>");
				sb1.append("<a class='bookmark' style='height:60px;display:block;' id='cls_"
						+ info.cls.clz.getSimpleName() + "'></a>");

				sb1.append("<div class='m_title'>"
						+ info.cls.clz.getSimpleName() + "</div>");
				sb1.append(descriptObject(info.cls));
				sb1.append("</div>");
				info.gen = true;
			}
			objs = new ArrayList<GenClassInfo>();
			for (GenClassInfo info : objects) {
				if (info.gen == false) {
					objs.add(info);
				}
			}
		}
		sb.append(sb1.toString());

		// output list
		sb.append(apiIndex.toString());

		String template;
		try {
			template = Template
					.readTemplate("/cn/mapway/document/util/resource/doctemplate.html");
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		template = template.replaceAll("\\$\\{page_title\\}", api.title);

		template = template.replaceAll("\\$\\{page_catalog\\}",
				catalog.toString());

		template = template.replaceAll("\\$\\{page_content\\}", sb.toString());

		template = template.replaceAll(
				"\\$\\{page_footer\\}",
				"&copy;" + (Times.now().getYear() + 1900) + "&nbsp; "
						+ context.getDomain() + " &nbsp;&nbsp;&nbsp;联系:"
						+ author);
		return template;
	}

	private void handlerEntry(int indent, ApiEntry e, StringBuilder catalog,
			StringBuilder sb) {

		String id = e.relativePath.replace("/", "");

		String cls = "entry";
		catalog.append("<li class='" + cls + "'>");
		catalog.append("<a  href='#" + id + "'>");
		catalog.append(e.name);
		catalog.append("</a></li>");

		sb.append("<a class='bookmark' style='height:60px;display:block;' id='"
				+ id + "'></a>");
		sb.append("<div class='m_block'><table width='100%' cellpadding='5px'>");
		sb.append("<tr><td colspan='2' class='m_title' >" + e.name
				+ "<div class='m_subtitle'>" + e.summary + "</div>"
				+ "</td></tr>");

		sb.append("<tr><td colspan='2' class='m_path' ><a href='"
				+ e.relativePath + e.relativePath + "' target='_blank'>"
				+ e.relativePath + "</a></td></tr>");

		sb.append("<tr><td colspan='2' >调用方法:" + e.invokeMethod + "</td></tr>");
		sb.append("</table>");

		StringBuilder inputstbl = new StringBuilder();
		for (ParameterInfo i : e.input) {

			inputstbl.append("<div class='objdesc'>传入参数：" + i.title + "</div>");

			inputstbl.append(descriptObject(i));
		}

		sb.append(inputstbl.toString());
		sb.append("<div class='objdesc'>传出参数：" + e.output.title + "</div>");
		sb.append(descriptObject(e.output));
		sb.append("</div>");

	}

	private void handlerGroup(int indent, ApiGroup g, StringBuilder catalog,
			StringBuilder detail, StringBuilder apiIndex) {

		String cls = "tree" + (indent++);

		catalog.append("<div subgroup='" + g.getChildGroups().size()
				+ "'  class='" + cls + "'>");
		String path = g.getPath();
		path = path.replace("/", "_");

		String groupName = "group" + path;

		catalog.append("<div><a href='#" + groupName + "'>");
		catalog.append(g.name);
		catalog.append("</a></div>");

		apiIndex.append("<a name='" + groupName + "'></a>");
		apiIndex.append("<div class='indexGroup'>" + g.getPath() + "/" + g.name
				+ "</div>");

		if (g.entries.size() > 0) {
			apiIndex.append("<table width='100%' class='indexTable'>");
			int index = 1;
			for (ApiEntry e : g.entries) {
				apiIndex.append("<tr>");
				apiIndex.append("<td width='50px' align='right'>" + (index++)
						+ "</td>");
				apiIndex.append("<td width='250px'>" + e.name + "</td>");
				apiIndex.append("<td>" + e.relativePath + "</td>");
				apiIndex.append("</tr>");
			}
			apiIndex.append("</table>");
		}

		// handler subgroup
		for (ApiGroup subg : g.getChildGroups()) {
			handlerGroup(indent, subg, catalog, detail, apiIndex);
		}

		// handle entry

		if (g.entries.size() > 0) {
			catalog.append("<ol>");
			for (ApiEntry e : g.entries) {
				handlerEntry(indent, e, catalog, detail);
			}
			catalog.append("</ol>");
		}
		catalog.append("</div>");
	}

	/**
	 * 描述对象信息
	 * 
	 * @param c
	 * @return
	 */
	private String descriptObject(ParameterInfo info) {
		StringBuilder sb = new StringBuilder();

		sb.append("<table width='100%' border='1' class='tbl_param' cellpadding='5px'>");
		sb.append("<tr><td class='m_subtitle' colspan=\"5\">" + info.title
				+ "<br/>" + info.summary + "</td></tr>");
		sb.append("<tr class='tbheader'><th>名称</th><th>类型</th><th>长度</th><th>选项</th><th>解释</th></tr>");
		for (FieldInfo fi : info.flds) {
			Field f = fi.fld;
			ApiField wf = f.getAnnotation(ApiField.class);
			if (wf != null) {
				sb.append("<tr>");
				if (Modifier.isFinal(f.getModifiers())) {

					try {
						sb.append("<td class='m_const' width='300px'>"
								+ f.getName() + "("
								+ f.get(info.clz).toString() + ")</td>");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

				} else {
					sb.append("<td class='key' width='300px'>" + f.getName()
							+ "</td>");

				}
				if (isPrimitive(f.getType())) {
					sb.append("<td width='150px'>"
							+ f.getType().getSimpleName() + "</td>");

				} else if (f.getType().isAssignableFrom(List.class)) {
					// list
					Type fc = f.getGenericType();
					if (fc == null) {
						sb.append("<td width='150px'>"
								+ f.getType().getSimpleName() + "</td>");
						continue;
					}
					if (fc instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) fc;
						Class<?> t = (Class<?>) pt.getActualTypeArguments()[0];

						ParameterInfo pi = handleParameter(t);
						if (isPrimitive(pi.clz)) {

						} else {
							addGenClass(pi);
						}
						sb.append("<td width='150px'><a href='#cls_"
								+ t.getSimpleName() + "'>" + "List&lt;"
								+ t.getSimpleName() + "&gt;</a></td>");
					}

				} else {

					ParameterInfo pi = handleParameter(f.getType());
					if (isPrimitive(pi.clz)) {

					} else {
						addGenClass(pi);
					}
					sb.append("<td width='150px'><a href='#cls_"
							+ f.getType().getSimpleName() + "'>"
							+ f.getType().getSimpleName() + "</a></td>");

				}
				sb.append("<td width='50px'>"
						+ (wf.length() == 0 ? "" : wf.length()) + "</td>");
				sb.append("<td width='60px'>" + (wf.mandidate() ? "必填" : "可选")
						+ "</td>");
				sb.append("<td class='doc'>" + wf.value() + "</td>");

				sb.append("</tr>");
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

}
