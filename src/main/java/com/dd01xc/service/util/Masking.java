package com.dd01xc.service.util;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//hides personal data in public analytics responses
public final class Masking {

    private static final Pattern IPV4 = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern IPV6 = Pattern.compile("^[0-9a-fA-F:]+$");
    private static final String HIDDEN = "hidden";
    private static final String STARS = "***";

    private Masking() {}

    public static String ip(String ip) {
        if (ip == null || ip.isBlank()) {return null;}
        if (IPV4.matcher(ip).matches()) {
            return ip.substring(0, ip.lastIndexOf('.')) + ".x";
        }
        if (ip.contains(":") && IPV6.matcher(ip).matches()) {
            //keep network prefix (first 3 groups)
            String prefix = Arrays.stream(ip.split(":"))
                .filter(g -> !g.isEmpty())
                .limit(3)
                .collect(Collectors.joining(":"));
            return prefix.isEmpty() ? "::x" : prefix + "::x";
        }
        return HIDDEN;
    }

    public static String account(String account) {
        if (account == null || account.isBlank()) {return account;}
        int at = account.lastIndexOf('@');
        if (at < 0) {return maskPart(account);}
        return maskPart(account.substring(0, at)) + account.substring(at);
    }

    private static String maskPart(String part) {
        if (part.isEmpty()) {return STARS;}
        int keep = part.length() >= 4 ? 2 : 1;
        return part.substring(0, keep) + STARS;
    }
}
