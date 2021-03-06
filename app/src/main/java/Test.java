import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * +----------------------------------------------------------------------
 * |  说明     ：
 * +----------------------------------------------------------------------
 * | 创建者   :  kim_tony
 * +----------------------------------------------------------------------
 * | 时　　间 ：2017/12/14 14:40
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 **/

public class Test {
    public static void main(String[] arg){
        try {
            System.out.println(dateToStamp("2018-06-10"));
            System.out.println(dateToStamp("2018-08-31"));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /*
   * 将时间转换为时间戳
   */
    public static String dateToStamp(String s) throws  ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }
}
