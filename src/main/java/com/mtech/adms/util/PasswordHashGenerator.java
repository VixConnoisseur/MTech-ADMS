package com.mtech.adms.util;

/**
 * One-off utility to generate a BCrypt hash for seeding the first
 * admin account. Not part of the running application - run this
 * once, copy the output into an INSERT statement, then this class
 * can be deleted (or kept around for creating future test accounts).
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        String plainPassword = "admin123"; // change this to your desired password

        String hash = PasswordUtil.hash(plainPassword);

        System.out.println("Plain password:admin123" + plainPassword);
        System.out.println("BCrypt hash:$2a$10$SAT3M7jpi59RaqectN.uteo0UMbuGfacMFUEU94VPuebZsOHNZzyO" + hash);
        System.out.println();
        System.out.println("Copy the hash above into your INSERT statement.");
    }
}