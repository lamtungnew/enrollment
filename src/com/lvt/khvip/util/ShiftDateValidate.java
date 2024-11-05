package com.lvt.khvip.util;

import com.lvt.khvip.client.dto.ShiftConfigDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class ShiftDateValidate {
    public static void main(String[] a) {
        try {
            List<ShiftConfigDto> lst = new ArrayList<>();
            lst.add(ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/01/2021"))
                    .expireDate(Utils.convertStringToDate("30/01/2021")).build());

            lst.add(ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/02/2021"))
                    .expireDate(Utils.convertStringToDate("28/02/2021")).build());

            lst.add(ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/04/2021"))
                    .expireDate(Utils.convertStringToDate("28/04/2021")).build());

            ShiftConfigDto in = ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("22/05/2021"))
                    .expireDate(Utils.convertStringToDate("31/5/2021")).build();

            boolean rs = checkRange(in, lst);
            log.info("{}", rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkRange(ShiftConfigDto shiftConfigDto, List<ShiftConfigDto> lstShift) {
        boolean valid = true;
        try {
            for (ShiftConfigDto item : lstShift) {
                if(shiftConfigDto.getAutoid() != null && item.getAutoid() != null && shiftConfigDto.getAutoid().equals(item.getAutoid())){
                    continue;
                }
                boolean betweenStart = betweenEq(shiftConfigDto.getStartDate(), item.getStartDate(), item.getExpireDate());
                boolean betweenEnd = betweenEq(shiftConfigDto.getExpireDate(), item.getStartDate(), item.getExpireDate());
                log.info(":    [{} <= {} >= {}] startDate = {}, [{} <= {} >= {}] expireDate {}, rs: {}",
                        Utils.convertDateToString(item.getStartDate()), Utils.convertDateToString(shiftConfigDto.getStartDate()), Utils.convertDateToString(item.getExpireDate()),
                        betweenStart,
                        Utils.convertDateToString(item.getStartDate()), Utils.convertDateToString(shiftConfigDto.getExpireDate()), Utils.convertDateToString(item.getExpireDate()),
                        betweenEnd,
                        (!betweenStart && !betweenEnd));
                if (betweenStart || betweenEnd) {
                    valid = false;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            valid = false;
        }
        return valid;
    }

    private static boolean between(Date date, Date dateStart, Date dateEnd) {
        if (date != null && dateStart != null && dateEnd != null) {
            if (date.after(dateStart) && date.before(dateEnd)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private static boolean betweenEq(Date date, Date dateStart, Date dateEnd) {
        if (date != null && dateStart != null && dateEnd != null) {
            if ((date.compareTo(dateStart) == 0 || date.after(dateStart)) && (date.compareTo(dateStart) == 0 || date.before(dateEnd))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
