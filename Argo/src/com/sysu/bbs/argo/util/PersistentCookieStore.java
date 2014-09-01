package com.sysu.bbs.argo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class PersistentCookieStore implements CookieStore {
	private CookieStore store;
	private static final String COOKIE_PREFS = "CookePrefsFile";
	private static final String COOKIE_NAME_STORE = "names";
	private static final String COOKIE_NAME_PREFIX = "cookies_";

	private SharedPreferences cookiePref;

	public PersistentCookieStore(Context con) {
		// get the default in memory cookie store
		store = new CookieManager().getCookieStore();

		cookiePref = con.getSharedPreferences(COOKIE_PREFS, 0);
		String[] cookieNames = cookiePref.getString(COOKIE_NAME_STORE, "")
				.split(",");
		for (String name : cookieNames) {
			if (name.equals(""))
				continue;
			String encodedCookie = cookiePref.getString(COOKIE_NAME_PREFIX + name, null);
			if (encodedCookie != null) {
				HttpCookie decodedCookie = decodeCookie(encodedCookie);
				if (decodedCookie != null)
					store.add(URI.create(name), decodedCookie);
			}
			
			
		}

	}

	public void add(URI uri, HttpCookie cookie) {
		store.add(uri, cookie);
	}

	public List<HttpCookie> get(URI uri) {
		return store.get(uri);
	}

	public List<HttpCookie> getCookies() {
		return store.getCookies();
	}

	public List<URI> getURIs() {
		return store.getURIs();
	}

	public boolean remove(URI uri, HttpCookie cookie) {

		return store.remove(uri, cookie);
	}

	public boolean removeAll() {

		return store.removeAll();
	}

	public void persist() {
		SharedPreferences.Editor editor = cookiePref.edit();
		for (URI uri: store.getURIs()) {
			List<HttpCookie> cookies = store.get(uri);
			for (HttpCookie cookie: cookies) {
				editor.putString(COOKIE_NAME_PREFIX + uri, 
					encodeCookie(new SerializableHttpCookie(cookie)));
			}
		}
		editor.putString(COOKIE_NAME_STORE, TextUtils.join(",", store.getURIs()));
		editor.commit();

	}

	/**
	 * Serializes HttpCookie object into String
	 * 
	 * @param cookie
	 *            cookie to be encoded, can be null
	 * @return cookie encoded as String
	 */
	protected String encodeCookie(SerializableHttpCookie cookie) {
		if (cookie == null)
			return null;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(os);
			outputStream.writeObject(cookie);
		} catch (IOException e) {
			
			return null;
		}

		return byteArrayToHexString(os.toByteArray());
	}

	/**
	 * Returns HttpCookie decoded from cookie string
	 * 
	 * @param cookieString
	 *            string of cookie as returned from http request
	 * @return decoded cookie or null if exception occurred
	 */
	protected HttpCookie decodeCookie(String cookieString) {
		byte[] bytes = hexStringToByteArray(cookieString);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				bytes);

		HttpCookie cookie = null;
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(
					byteArrayInputStream);
			cookie = ((SerializableHttpCookie) objectInputStream.readObject())
					.getCookie();
		} catch (IOException e) {
			
		} catch (ClassNotFoundException e) {
			
		}

		return cookie;
	}

	/**
	 * Using some super basic byte array &lt;-&gt; hex conversions so we don't
	 * have to rely on any large Base64 libraries. Can be overridden if you
	 * like!
	 * 
	 * @param bytes
	 *            byte array to be converted
	 * @return string containing hex values
	 */
	protected String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte element : bytes) {
			int v = element & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase(Locale.US);
	}

	/**
	 * Converts hex values from strings to byte arra
	 * 
	 * @param hexString
	 *            string of hex-encoded values
	 * @return decoded byte array
	 */
	protected byte[] hexStringToByteArray(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
					.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

}
