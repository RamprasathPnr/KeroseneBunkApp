package com.omneAgate.printer;

import android.util.Log;

import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.UserDto.BillItemProductDto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;


public class PrintProcess {

    @Getter
    @Setter
    private String template;

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    public static String align(String name, int maxlen, boolean left) {

        if (left) {
            name = padLeft(name, maxlen);
            name = name.substring(name.length() - maxlen);
        } else {
            name = padRight(name, maxlen);
            name = name.substring(0, maxlen);
        }
        System.out.println("Aligned Name[" + name + "]");
        return name;
    }

    public static int getPaddingValue(String name) {

        String pattern = "<.*?>";
        Pattern regx = Pattern.compile(pattern);
        Matcher match = regx.matcher(name);
        String label = new String();
        while (match.find()) {
            label = match.group();
            System.out.println("Label :[" + label + "]");
        }
        name = name.replace(label, "");
        label = label.replace("<LP:", "");
        label = label.replace("<RP:", "");
        label = label.replace(">", "");
        int retval = 0;
        try {
            retval = Integer.parseInt(label);
        } catch (Exception e) {
            System.out.println("Error");
        }
        System.out.println("Value [" + retval + "]" + name);
        return retval;
    }

    public static String removePadding(String name) {

        String pattern = "<.*?>";
        Pattern regx = Pattern.compile(pattern);
        Matcher match = regx.matcher(name);
        String label = new String();
        while (match.find()) {
            label = match.group();
            System.out.println("Label :[" + label + "]");
        }
        name = name.replace(label, "");

        return name;
    }

    public String ProcessBill(String template, BillDto bill) {

        this.template = template;
        List<String> matchlist = new ArrayList<String>();
        String pattern = "\\$#.*?#\\$";
        Pattern regx = Pattern.compile(pattern);
        Matcher match = regx.matcher(template);
        while (match.find()) {
            String label = match.group();
            label = label.replace("$#", "");
            label = label.replace("#$", "");
            matchlist.add(label);
        }
        template = replacedata(template, matchlist, bill);
        Log.e("Bill", "Final data is \n" + template);
        return template;

    }

    public String replacedata(String content, List<String> matchlist,
                              BillDto bill) {

        for (String label : matchlist) {
            try {
                if (label.startsWith("bill.items")
                        || label.startsWith("bill.tax")) {
                    // ignore it
                } else {
                    String fieldname = label.replace("bill.", "");
                    boolean lpad = isLeftPad(fieldname);
                    int padval = getPaddingValue(fieldname);
                    fieldname = removePadding(fieldname);
                    Field field = BillDto.class
                            .getDeclaredField(fieldname);
                    Log.e("Bill Field:", "" + field);
                    String fieldValue = getStringValue(field, bill);
                    fieldValue = align(fieldValue, padval, lpad);
                    System.out.println(field.getName() + " Field Value "
                            + fieldValue);
                    content = content.replace("$#" + label + "#$", fieldValue);
                }
            } catch (Exception e) {

                Log.e("Error", e.toString());

            }
        }
        content = processItems(content, matchlist, bill);

        return content;
    }

    private String getStringValue(Field field, Object bill) {

        String methodName = "get"
                + field.getName().substring(0, 1).toUpperCase()
                + field.getName().substring(1);
        Method method;
        System.out.println("method name :" + methodName);
        try {
            System.out.println("bill :" + bill);
            System.out.println("class :" + bill.getClass());
            method = bill.getClass().getDeclaredMethod(methodName);
            System.out.println("method :" + method.getName());
            Object retval = method.invoke(bill);

            String value = retval.toString();
            if (method.getName().equals("getDescription") && value.length() > 0) {
                value = "(" + value + ")";
            }
            //for empty description
            if (method.getName().equals("getDescription") && value.length() == 0) {
                value = " ";
            }
            if (field.getType().toString().equals("class java.lang.Double")) {
                value = new MessageFormat("{0,number," + "#0.00" + "}")
                        .format(new Object[]{method.invoke(bill)});
            }
            return value;
        } catch (Exception e) {
            Log.e("Error in print ", e.toString(), e);
        }
        return null;
    }

    private String processItems(String content, List<String> matchlist,
                                BillDto bill) {

        List<BillItemProductDto> items = new ArrayList<>(bill.getBillItemDto());
        if (items == null) {
            return content;
        }
        String itemsContent = new String();
        itemsContent += "\n";
        int counter = 0;
        for (BillItemProductDto billItem : items) {
            counter++;
            itemsContent += align(Integer.toString(counter), 3, false) + " ";

            for (String label : matchlist) {
                try {

                    if (label.startsWith("bill.items.")) {
                        String fieldname = label.replace("bill.items.", "");
                        boolean lpad = isLeftPad(fieldname);
                        int padval = getPaddingValue(fieldname);
                        fieldname = removePadding(fieldname);
                        Field field = BillItemDto.class
                                .getDeclaredField(fieldname);
                        String fieldValue = getStringValue(field, billItem);

                        System.out.println(field.getName() + " Field Value "
                                + fieldValue + "Field Type is "
                                + field.getType());
                        System.out.println("Content:" + content);
                        System.out.println("label:" + label);
                        if (label.equals("bill.items.description<RP:35>")
                                && fieldValue.length() == 0) {
                            System.out.println("Lable value:" + label);
                            label = "bill.items.description<RP:1>";

                        }
                        content = content.replace("$#" + label + "#$", "");

                        itemsContent += align(fieldValue, padval, lpad);

                    }

                } catch (Exception e) {
                    Log.e("Error", e.toString(), e);
                }
            }
            itemsContent += "\r";
        }
        System.out.println("Item Content is \n" + itemsContent);
        content = content.replace("$#bill.items#$", itemsContent);
        System.out.println("Item After Content is \n" + itemsContent);

        return content;
    }

    private boolean isLeftPad(String name) {

        return name.contains("<LP:");

    }
}