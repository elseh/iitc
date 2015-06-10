package iitc.triangulation;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by epavlova on 6/10/2015.
 */
public class Test {
    public static void main(String[] args) {
        String oos = "Бумага &#x95; креповая стандартная &#x96; упаковочный материал для стерилизациибумага креповая";
        String doc = "Бумага "+(new String(Character.toChars(8226)))+" креповая стандартная "+(new String(Character.toChars(8211)))+" упаковочный материал для стерилизациибумага креповая";
        System.out.println("doc:  " + doc);
        System.out.println("oos:  " + oos);

        //System.out.println(StringEscapeUtils.unescapeHtml3(oos).getBytes()).stream().forEach((c) -> System.out.println(c + " " + new String(new char[]{c}))));

        String s = StringEscapeUtils.unescapeHtml3(oos);
        try {
            String l1 = new String(oos.getBytes("utf-8"), "koi8");
            System.out.println(l1);
            l1 = StringEscapeUtils.unescapeHtml3(oos);
            String ut = new String(l1.getBytes("koi8"), "utf-8");
            System.out.println(ut);

            /*String x = StringEscapeUtils.unescapeHtml3(new String(oos.getBytes("utf-8"), "windows-1251"));
            System.out.println(new String(x.getBytes("windows-1251"), "utf-8"));*/

            /*System.out.println(new String(s.getBytes("utf-8"), "windows-1251"));*/
            System.out.println(new String(s.getBytes("ISO-8859-1")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /*for (int i = 0; i < s.length(); i ++) {
            System.out.println(s.charAt(i) + " " + s.codePointAt(i));
        }*/

        System.out.println(doc.compareTo(StringEscapeUtils.unescapeHtml4((oos))));
    }
}
