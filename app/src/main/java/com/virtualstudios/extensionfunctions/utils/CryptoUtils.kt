package com.virtualstudios.extensionfunctions.utils

import android.util.Base64
import io.opencensus.internal.StringUtils
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Generates SHA256 hash of the password which is used as key
 *
 * @param password used to generated key
 * @return SHA256 of the password
 */
@Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
private fun generateKey(password: String): SecretKeySpec {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = password.toByteArray(StandardCharsets.UTF_8)
    digest.update(bytes, 0, bytes.size)
    val key = digest.digest()
    return SecretKeySpec(key, "AES")
}


/**
 * Encrypt and encode message using 256-bit AES with key generated from password.
 *
 * @param password used to generated key
 * @param message  the thing you want to encrypt assumed String UTF-8
 * @return Base64 encoded CipherText
 * @throws GeneralSecurityException if problems occur during encryption
 */
@Throws(GeneralSecurityException::class)
private fun _encrypts(password: String, message: String): String {
    val ivBytes = byteArrayOf(
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )
    try {
        val key = generateKey(password)
        val cipherText = encrypt(key, ivBytes, message.toByteArray(StandardCharsets.UTF_8))
        //NO_WRAP is important as was getting \n at the end
        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    } catch (e: UnsupportedEncodingException) {
        throw GeneralSecurityException(e)
    }
}


/**
 * More flexible AES encrypt that doesn't encode
 *
 * @param key     AES key typically 128, 192 or 256 bit
 * @param iv      Initiation Vector
 * @param message in bytes (assumed it's already been decoded)
 * @return Encrypted cipher text (not encoded)
 * @throws GeneralSecurityException if something goes wrong during encryption
 */
@Throws(GeneralSecurityException::class)
private fun encrypt(key: SecretKeySpec, iv: ByteArray, message: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    val ivSpec = IvParameterSpec(iv)
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
    return cipher.doFinal(message)
}

/**
 * Decrypt and decode ciphertext using 256-bit AES with key generated from password
 *
 * @param password                used to generated key
 * @param base64EncodedCipherText the encrpyted message encoded with base64
 * @return message in Plain text (String UTF-8)
 * @throws GeneralSecurityException if there's an issue decrypting
 */
@Throws(GeneralSecurityException::class)
private fun _decrypt(password: String, base64EncodedCipherText: String): String {
    val ivBytes = byteArrayOf(
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )
    try {
        val key = generateKey(password)
        val decodedCipherText = Base64.decode(base64EncodedCipherText, Base64.NO_WRAP)
        val decryptedBytes = decrypt(key, ivBytes, decodedCipherText)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    } catch (e: UnsupportedEncodingException) {
        throw GeneralSecurityException(e)
    }
}

/**
 * More flexible AES decrypt that doesn't encode
 *
 * @param key               AES key typically 128, 192 or 256 bit
 * @param iv                Initiation Vector
 * @param decodedCipherText in bytes (assumed it's already been decoded)
 * @return Decrypted message cipher text (not encoded)
 * @throws GeneralSecurityException if something goes wrong during encryption
 */
@Throws(GeneralSecurityException::class)
private fun decrypt(key: SecretKeySpec, iv: ByteArray, decodedCipherText: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    val ivSpec = IvParameterSpec(iv)
    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

    return cipher.doFinal(decodedCipherText)
}

fun encrypt(content: String, password: String): String {
    try {
        return nonNull(
            _encrypts(
                password,
                content
            )
        )
    } catch (e: GeneralSecurityException) {
        e.printStackTrace()
    }
    return ""
}

fun decrypt(content: String, password: String): String {
    try {
        return nonNull(
            _decrypt(
                password,
                content
            )
        )
    } catch (e: GeneralSecurityException) {
        e.printStackTrace()
    }
    return ""
}