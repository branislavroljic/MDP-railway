package bane.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtil {

	public static String digest(byte[] input, String salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt.getBytes());
			byte[] messageDigest = md.digest(input);
			BigInteger no = new BigInteger(1, messageDigest);
			String hashtext = no.toString(16);
			// Dopunim nulama do 32 bita
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
