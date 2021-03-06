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
package cn.mapway.document.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 模板类
 * 
 * @author zhangjianshe@navinfo.com
 * 
 */
public class Template {

	/**
	 * 读取相对资源路径
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public final static String readTemplate(String fileName) throws IOException {
		
		InputStream in = Template.class.getResourceAsStream(fileName);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int count = 0;
		byte[] bytes = new byte[1024];
		count = in.read(bytes);
		while (count > 0) {
			out.write(bytes, 0, count);
			count = in.read(bytes);
		}

		String txt = new String(out.toByteArray(), "UTF-8");
		return txt;
	}
}
