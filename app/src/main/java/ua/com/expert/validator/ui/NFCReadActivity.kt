package ua.com.expert.validator.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import ua.com.expert.validator.R
import ua.com.expert.validator.utils.ActivityUtils.MessageCallBack
import ua.com.expert.validator.utils.ActivityUtils.showMessageWihtCallBack
import ua.com.expert.validator.utils.NFCUtils

class NFCReadActivity : AppCompatActivity() {

    private var mAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!NFCUtils.enabledNFC(this)) {
            showMessageWihtCallBack(this, null, null,
                    getString(R.string.nfc_disabled), object : MessageCallBack {
                override fun onPressOk() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                        startActivity(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(intent)
                    }
                    finish()
                }
            })
        }
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mAdapter == null) {
            showMessageWihtCallBack(this, null, null,
                    getString(R.string.nfc_start_error), object : MessageCallBack {
                override fun onPressOk() {
                    finish()
                }
            })
            return
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, Intent(this,
                javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    }

    override fun onResume() {
        super.onResume()
        mAdapter!!.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        if (mAdapter != null) {
            mAdapter!!.disableForegroundDispatch(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        resolveIntent(intent)
    }

    private fun resolveIntent(intent: Intent) {
        val msgs: Array<NdefMessage?>
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMsgs != null) {
                msgs = arrayOfNulls(rawMsgs.size)
                for (i in rawMsgs.indices) {
                    msgs[i] = rawMsgs[i] as NdefMessage
                }
            } else {
                val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag
                        ?: return
                if (onFoundTagListener != null) {
                    onFoundTagListener!!.onFoundTag(NFCUtils.toDec(tag.id).toString())
                    onFoundTagListener = null
                } else {
                    val intentResult = Intent()
                    intentResult.putExtra(ACTIVITY_NFC_RESULT, NFCUtils.toDec(tag.id).toString())
                    setResult(Activity.RESULT_OK, intentResult)
                }
                finish()

//                String dumpData = NFCUtils.dumpTagData(tag);
//                msgs = new NdefMessage[]{new NdefMessage(
//                new NdefRecord[]{
//                new NdefRecord((short) 5, new byte[0], intent.getByteArrayExtra("android.nfc.extra.ID"),
//                dumpData.getBytes())})};

//                this.mTags.add(tag);
            }
        }
    }

    interface OnFoundTagListener {
        fun onFoundTag(tag: String?)
    }

    companion object {
        const val ACTIVITY_NFC_RESULT = "activity_result_key"
        private var onFoundTagListener: OnFoundTagListener? = null
        fun setFoundTagListener(listener: OnFoundTagListener?) {
            onFoundTagListener = listener
        }
    }
}