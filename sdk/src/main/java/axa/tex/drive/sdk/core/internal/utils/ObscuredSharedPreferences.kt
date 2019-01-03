package axa.tex.drive.sdk.core.internal.utils

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Base64
import android.util.Log
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec


/**
 * Thanks to emmby at http://stackoverflow.com/questions/785973/what-is-the-most-appropriate-way-to-store-user-settings-in-android-application/6393502#6393502
 *
 * Documentation: http://right-handed-monkey.blogspot.com/2014/04/obscured-shared-preferences-for-android.html
 * This class has the following additions over the original:
 * additional logic for handling the case for when the preferences were not originally encrypted, but now are.
 * The secret key is no longer hard coded, but defined at runtime based on the individual device.
 * The benefit is that if one device is compromised, it now only affects that device.
 *
 * Simply replace your own SharedPreferences object in this one, and any data you read/write will be automatically encrypted and decrypted.
 *
 * Updated usage:
 * ObscuredSharedPreferences prefs = ObscuredSharedPreferences.getPrefs(this, MY_APP_NAME, Context.MODE_PRIVATE);
 * //to get data
 * prefs.getString("foo", null);
 * //to store data
 * prefs.edit().putString("foo","bar").commit();
 */
class ObscuredSharedPreferences
/**
 * Constructor
 * @param context
 * @param delegate - SharedPreferences object from the system
 */
(protected var context: Context, protected var delegate: SharedPreferences) : SharedPreferences {

    init {
        //updated thanks to help from bkhall on github
        ObscuredSharedPreferences.setNewKey(Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
        ObscuredSharedPreferences.setNewSalt(Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
    }

    inner class Editor : SharedPreferences.Editor {
        protected var delegate: SharedPreferences.Editor

        init {
            this.delegate = this@ObscuredSharedPreferences.delegate.edit()
        }

        override fun putBoolean(key: String, value: Boolean): Editor {
            delegate.putString(key, encrypt(java.lang.Boolean.toString(value)))
            return this
        }

        override fun putFloat(key: String, value: Float): Editor {
            delegate.putString(key, encrypt(java.lang.Float.toString(value)))
            return this
        }

        override fun putInt(key: String, value: Int): Editor {
            delegate.putString(key, encrypt(Integer.toString(value)))
            return this
        }

        override fun putLong(key: String, value: Long): Editor {
            delegate.putString(key, encrypt(java.lang.Long.toString(value)))
            return this
        }

        override fun putString(key: String, value: String?): Editor {
            delegate.putString(key, encrypt(value))
            return this
        }

        override fun apply() {
            //to maintain compatibility with android level 7
            delegate.commit()
        }

        override fun clear(): Editor {
            delegate.clear()
            return this
        }

        override fun commit(): Boolean {
            return delegate.commit()
        }

        override fun remove(s: String): Editor {
            delegate.remove(s)
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): android.content.SharedPreferences.Editor {
            throw RuntimeException("This class does not work with String Sets.")
        }
    }

    override fun edit(): Editor {
        return Editor()
    }


    override fun getAll(): Map<String, *> {
        throw UnsupportedOperationException() // left as an exercise to the reader
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        //if these weren't encrypted, then it won't be a string
        val v: String?
        try {
            v = delegate.getString(key, null)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return delegate.getBoolean(key, defValue)
        }

        //Boolean string values should be 'true' or 'false'
        //Boolean.parseBoolean does not throw a format exception, so check manually
        val parsed = decrypt(v)
        if (!checkBooleanString(parsed)) {
            //could not decrypt the Boolean.  Maybe the wrong key was used.
            decryptionErrorFlag = true
            Log.e(this.javaClass.name, "Warning, could not decrypt the value.  Possible incorrect key used.")
        }
        return if (v != null) java.lang.Boolean.parseBoolean(parsed) else defValue
    }

    /**
     * This function checks if a valid string is received on a request for a Boolean object
     * @param str
     * @return
     */
    private fun checkBooleanString(str: String?): Boolean {
        return "true".equals(str!!, ignoreCase = true) || "false".equals(str, ignoreCase = true)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        val v: String?
        try {
            v = delegate.getString(key, null)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return delegate.getFloat(key, defValue)
        }

        try {
            return java.lang.Float.parseFloat(decrypt(v)!!)
        } catch (e: NumberFormatException) {
            e.printStackTrace()//could not decrypt the number.  Maybe we are using the wrong key?
            decryptionErrorFlag = true
            Log.e(this.javaClass.name, "Warning, could not decrypt the value.  Possible incorrect key.  " + e.message)
        }

        return defValue
    }

    override fun getInt(key: String, defValue: Int): Int {
        val v: String?
        try {
            v = delegate.getString(key, null)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return delegate.getInt(key, defValue)
        }

        try {
            return Integer.parseInt(decrypt(v)!!)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            //could not decrypt the number.  Maybe we are using the wrong key?
            decryptionErrorFlag = true
            Log.e(this.javaClass.name, "Warning, could not decrypt the value.  Possible incorrect key.  " + e.message)
        }

        return defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        val v: String?
        try {
            v = delegate.getString(key, null)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            return delegate.getLong(key, defValue)
        }

        try {
            return java.lang.Long.parseLong(decrypt(v)!!)
        } catch (e: NumberFormatException) {
            //could not decrypt the number.  Maybe we are using the wrong key?
            e.printStackTrace()
            decryptionErrorFlag = true
            Log.e(this.javaClass.name, "Warning, could not decrypt the value.  Possible incorrect key.  " + e.message)
        }

        return defValue
    }

    override fun getString(key: String, defValue: String?): String? {
        val v = delegate.getString(key, null)
        return if (v != null) decrypt(v) else defValue
    }

    override fun contains(s: String): Boolean {
        return delegate.contains(s)
    }

    override fun registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
        delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
        delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        throw RuntimeException("This class does not work with String Sets.")
    }

    protected fun encrypt(value: String?): String {

        try {
            val bytes = value?.toByteArray(charset(UTF8)) ?: ByteArray(0)
            val keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
            val key = keyFactory.generateSecret(PBEKeySpec(SEKRIT))
            val pbeCipher = Cipher.getInstance("PBEWithMD5AndDES")
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, PBEParameterSpec(SALT!!, 20))

            return String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), Charset.forName(UTF8));
            //return String(Base64Support.encode(pbeCipher.doFinal(bytes), Base64Support.NO_WRAP), UTF8)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    protected fun decrypt(value: String?): String? {
        try {
            //val bytes = if (value != null) Base64Support.decode(value, Base64Support.DEFAULT) else ByteArray(0)
            val bytes = if (value != null) Base64.decode(value, Base64.DEFAULT) else ByteArray(0)
            val keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
            val key = keyFactory.generateSecret(PBEKeySpec(SEKRIT))
            val pbeCipher = Cipher.getInstance("PBEWithMD5AndDES")
            pbeCipher.init(Cipher.DECRYPT_MODE, key, PBEParameterSpec(SALT!!, 20))
            return String(pbeCipher.doFinal(bytes), Charset.forName(UTF8))
        } catch (e: Exception) {
            Log.e(this.javaClass.name, "Warning, could not decrypt the value.  It may be stored in plaintext.  " + e.message)
            return value
        }

    }

    companion object {
        protected val UTF8 = "UTF-8"
        //this key is defined at runtime based on ANDROID_ID which is supposed to last the life of the device
        private var SEKRIT: CharArray? = null
        private var SALT: ByteArray? = null

        private var backup_secret: CharArray? = null
        private var backup_salt: ByteArray? = null

        //Set to true if a decryption error was detected
        //in the case of float, int, and long we can tell if there was a parse error
        //this does not detect an error in strings or boolean - that requires more sophisticated checks
        var decryptionErrorFlag = false

        /**
         * Only used to change to a new key during runtime.
         * If you don't want to use the default per-device key for example
         * @param key
         */
        fun setNewKey(key: String) {
            SEKRIT = key.toCharArray()
        }

        /**
         * Only used to change to a new salt during runtime.
         * If you don't want to use the default per-device code for example
         * @param salt - this must be a string in UT
         */
        fun setNewSalt(salt: String) {
            try {
                SALT = salt.toByteArray(charset(UTF8))
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException(e)
            }

        }

        /**
         * Accessor to grab the preferences object
         * To improve performance for multiple accesses, please store the returned object in a variable for reuse
         * @param c - the context used to access the preferences.
         * @param domainName - domain the shared preferences should be stored under
         * @param contextMode - Typically Context.MODE_PRIVATE
         * @return
         */
        @Synchronized
        fun getPrefs(c: Context, domainName: String, contextMode: Int): ObscuredSharedPreferences {
            //make sure to use application context since preferences live outside an Activity
            //use for objects that have global scope like: prefs or starting services
            return ObscuredSharedPreferences(
                    c.applicationContext, c.applicationContext.getSharedPreferences(domainName, contextMode))
        }

        /**
         * Push key allows you to hold the current key being used into a holding location so that it can be retrieved later
         * The use case is for when you need to load a new key, but still want to restore the old one.
         */
        fun pushKey() {
            backup_secret = SEKRIT
        }

        /**
         * This takes the key previously saved by pushKey() and activates it as the current decryption key
         */
        fun popKey() {
            SEKRIT = backup_secret
        }

        /**
         * pushSalt() allows you to hold the current salt being used into a holding location so that it can be retrieved later
         * The use case is for when you need to load a new salt, but still want to restore the old one.
         */
        fun pushSalt() {
            backup_salt = SALT
        }

        /**
         * This takes the value previously saved by pushSalt() and activates it as the current salt
         */
        fun popSalt() {
            SALT = backup_salt
        }
    }
}