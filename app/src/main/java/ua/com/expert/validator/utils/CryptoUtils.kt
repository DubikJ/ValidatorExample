package ua.com.expert.validator.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.util.Base64
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import kotlin.experimental.and
import kotlin.experimental.xor

@TargetApi(Build.VERSION_CODES.M)
object CryptoUtils {
    private val TAG = CryptoUtils::class.java.simpleName
    private val KEY_ALIAS = "key_for_pin"
    private val KEY_STORE = "AndroidKeyStore"
    private val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

    private var sKeyStore: KeyStore? = null
    private var sKeyPairGenerator: KeyPairGenerator? = null
    private var sCipher: Cipher? = null

    private val keyStore: Boolean
        get() {
            try {
                sKeyStore = KeyStore.getInstance(KEY_STORE)
                sKeyStore!!.load(null)
                return true
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            }
            return false
        }

    private val keyPairGenerator: Boolean
        get() {
            try {
                sKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE)
                return true
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchProviderException) {
                e.printStackTrace()
            }

            return false
        }


    private val cipher: Boolean
        @SuppressLint("GetInstance")
        get() {
            try {
                sCipher = Cipher.getInstance(TRANSFORMATION)
                return true
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            }

            return false
        }

    private val key: Boolean
        get() {
            try {
                return sKeyStore!!.containsAlias(KEY_ALIAS) || generateNewKey()
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            }

            return false

        }

    val cryptoObject: FingerprintManagerCompat.CryptoObject?
        get() = if (prepare() && initCipher(Cipher.DECRYPT_MODE)) {
            FingerprintManagerCompat.CryptoObject(sCipher!!)
        } else null

    fun encode(inputString: String): String? {
        try {
            if (prepare() && initCipher(Cipher.ENCRYPT_MODE)) {
                val bytes = sCipher!!.doFinal(inputString.toByteArray())
                return Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (exception: IllegalBlockSizeException) {
            exception.printStackTrace()
        } catch (exception: BadPaddingException) {
            exception.printStackTrace()
        }

        return null
    }


    fun decode(encodedString: String, cipher: Cipher): String? {
        try {
            val bytes = Base64.decode(encodedString, Base64.NO_WRAP)
            return String(cipher.doFinal(bytes))
        } catch (exception: IllegalBlockSizeException) {
            exception.printStackTrace()
        } catch (exception: BadPaddingException) {
            exception.printStackTrace()
        }

        return null
    }

    private fun prepare(): Boolean {
        return keyStore && cipher && key
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun generateNewKey(): Boolean {

        if (keyPairGenerator) {

            try {
                sKeyPairGenerator!!.initialize(
                        KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                                .setUserAuthenticationRequired(true)
                                .build())
                sKeyPairGenerator!!.generateKeyPair()
                return true
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }

        }
        return false
    }


    private fun initCipher(mode: Int): Boolean {
        try {
            sKeyStore!!.load(null)

            when (mode) {
                Cipher.ENCRYPT_MODE -> initEncodeCipher(mode)

                Cipher.DECRYPT_MODE -> initDecodeCipher(mode)
                else -> return false //this cipher is only for encode\decode
            }
            return true

        } catch (exception: KeyPermanentlyInvalidatedException) {
            deleteInvalidKey()

        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: UnrecoverableKeyException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }

        return false
    }

    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, UnrecoverableKeyException::class, InvalidKeyException::class)
    private fun initDecodeCipher(mode: Int) {
        val key = sKeyStore!!.getKey(KEY_ALIAS, null) as PrivateKey
        sCipher!!.init(mode, key)
    }

    @Throws(KeyStoreException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class)
    private fun initEncodeCipher(mode: Int) {
        val key = sKeyStore!!.getCertificate(KEY_ALIAS).publicKey

        // workaround for using public key
        // from https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html
        val unrestricted = KeyFactory.getInstance(key.algorithm).generatePublic(X509EncodedKeySpec(key.encoded))
        // from https://code.google.com/p/android/issues/detail?id=197719
        val spec = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)

        sCipher!!.init(mode, unrestricted, spec)
    }

    fun deleteInvalidKey() {
        if (keyStore) {
            try {
                sKeyStore!!.deleteEntry(KEY_ALIAS)
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            }

        }
    }

    fun toHexString(array: ByteArray?): String {

        var bufferString = ""

        if (array != null) {
            for (i in array.indices) {
                var hexChar = Integer.toHexString((array[i] and 0xFF.toByte()).toInt())
                if (hexChar.length == 1) {
                    hexChar = "0$hexChar"
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " "
            }
        }
        return bufferString
    }

    fun stringToHexBytes(rawdata: String?): ByteArray? {

        if (rawdata == null || rawdata.isEmpty()) {
            return null
        }

        val command = rawdata.replace(" ", "").replace("\n", "")

        return if ((command.isEmpty() || command.length % 2 != 0
                        || isHexNumber(command) == false)) {
            null
        } else hexString2Bytes(command)

    }

    fun isHexNumber(string: String?): Boolean {
        if (string == null)
            throw NullPointerException("string was null")

        var flag = true

        for (i in 0 until string.length) {
            val cc = string!![i]
            if (!isHexNumber(cc.toByte())) {
                flag = false
                break
            }
        }
        return flag
    }

    private fun isHexNumber(value: Byte): Boolean {
        return if ((!(value >= '0'.toByte() && value <= '9'.toByte()) && !(value >= 'A'.toByte() && value <= 'F'.toByte())
                        && !(value >= 'a'.toByte() && value <= 'f'.toByte()))) {
            false
        } else true
    }

    fun hexString2Bytes(string: String?): ByteArray {
        if (string == null)
            throw NullPointerException("string was null")

        val len = string.length

        if (len == 0)
            return ByteArray(0)
        if (len % 2 == 1)
            throw IllegalArgumentException(
                    "string length should be an even number")

        val ret = ByteArray(len / 2)
        val tmp = string.toByteArray()

        var i = 0
        while (i < len) {
            if (!isHexNumber(tmp[i]) || !isHexNumber(tmp[i + 1])) {
                throw NumberFormatException(
                        "string contained invalid value")
            }
            ret[i / 2] = uniteBytes(tmp[i], tmp[i + 1])
            i += 2
        }
        return ret
    }

    private fun uniteBytes(src0: Byte, src1: Byte): Byte {
        var _b0 = java.lang.Byte.decode("0x" + String(byteArrayOf(src0)))
                .toByte()
        _b0 = (_b0 shl 4).toByte()
        val _b1 = java.lang.Byte.decode("0x" + String(byteArrayOf(src1)))
                .toByte()
        return (_b0 xor _b1).toByte()
    }

    infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
}


