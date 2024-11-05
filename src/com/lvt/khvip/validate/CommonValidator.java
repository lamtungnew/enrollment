package com.lvt.khvip.validate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonValidator {

    public static boolean isValidTaxCode(String mst) {
        if (mst.length() == 14 && mst.indexOf("-") != 10)
            return false;
        if (mst.length() == 13 && mst.contains("-"))
            return false;
        if (mst.length() != 10 && mst.length() != 13 && mst.length() != 14)
            return false;
        mst = mst.replace("-", "");
        int[] temp = {31, 29, 23, 19, 17, 13, 7, 5, 3};
        int total = 0;
        for (int i = 0; i < 9; i++)
            total += ((mst.charAt(i) - '0')) * temp[i];
        total = total % 11;
        total = 10 - total;
        if (total != (mst.charAt(9) - '0'))
            return false;
        if (mst.length() == 13
                && (Integer.parseInt(mst.substring(10, 13)) > 999 || Integer.parseInt(mst.substring(10, 13)) < 1))
            return false;
        return true;
    }

    public static boolean isValidMobiNumber(String number) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(number);
        if (!matcher.matches()) {
            return false;
        } else if (number.length() == 10 || number.length() == 11) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static boolean isValidDateStr(String date) {
        String regex = "(^(((0[1-9]|1[0-9]|2[0-8])[\\/](0[1-9]|1[012]))|((29|30|31)[\\/](0[13578]|1[02]))|((29|30)[\\/](0[4,6,9]|11)))[\\/](19|[2-9][0-9])\\d\\d$)|(^29[\\/]02[\\/](19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)$)";

        Pattern pat = Pattern.compile(date);
        if (date == null)
            return false;
        return pat.matcher(date).matches();
    }
}
