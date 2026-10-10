package co.obraplan;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class Password {
    private Password() {}

    static String hash(String plain) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] key = derive(plain, salt, 120_000);
            return "pbkdf2$120000$"
                    + Base64.getEncoder().encodeToString(salt)
                    + "$"
                    + Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo proteger la contraseña", e);
        }
    }

    static boolean verify(String plain, String encoded) {
        try {
            String[] p = encoded.split("\\$");
            if (p.length != 4 || !p[0].equals("pbkdf2")) return false;
            byte[] key = derive(plain, Base64.getDecoder().decode(p[2]), Integer.parseInt(p[1]));
            return java.security.MessageDigest.isEqual(key, Base64.getDecoder().decode(p[3]));
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(String p, byte[] salt, int rounds) throws Exception {
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(new PBEKeySpec(p.toCharArray(), salt, rounds, 256))
                .getEncoded();
    }
}
