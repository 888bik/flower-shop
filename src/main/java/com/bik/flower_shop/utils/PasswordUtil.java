package com.bik.flower_shop.utils;


import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtil {
    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verify(String raw, String encoded) {
        return BCrypt.checkpw(raw, encoded);
    }
}
