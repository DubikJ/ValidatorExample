package ua.com.expert.validator.ui.settings

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_settings.*
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.APP_CASH_TOKEN_PREFS
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.BLOCK_AUDIO_VALIDATE
import ua.com.expert.validator.common.Consts.LENGTH_SCAN_CARD_CODE
import ua.com.expert.validator.common.Consts.MODE
import ua.com.expert.validator.common.Consts.POSTFIX_SCAN_CARD_CODE
import ua.com.expert.validator.common.Consts.PREFIX_SCAN_CARD_CODE
import ua.com.expert.validator.common.Consts.SERVER
import ua.com.expert.validator.common.Consts.TOKEN
import ua.com.expert.validator.common.Consts.TYPE_SCAN
import ua.com.expert.validator.common.Consts.TYPE_SCAN_CAMERA
import ua.com.expert.validator.common.Consts.TYPE_SCAN_LIST
import ua.com.expert.validator.common.Consts.TYPE_SCAN_NFC
import ua.com.expert.validator.common.Consts.TYPE_SCAN_USB
import ua.com.expert.validator.common.Consts.USE_FINGEPRINT
import ua.com.expert.validator.common.Consts.VALID_DATE
import ua.com.expert.validator.model.SelectedItem
import ua.com.expert.validator.repo.Repository
import ua.com.expert.validator.ui.FragmentNavigationClickListener
import ua.com.expert.validator.ui.FragmentOnBackPressed
import ua.com.expert.validator.ui.basic.BasicActivity
import ua.com.expert.validator.utils.ActivityUtils
import ua.com.expert.validator.utils.FingerprintUtils
import ua.com.expert.validator.utils.SharedStorage
import java.util.*


class SettingsFragment : Fragment(), FragmentNavigationClickListener, FragmentOnBackPressed {

    private var isLogin: Boolean = false
    private var serverUrl: String? = null
    private var modeList: List<String>? = null
    private var lengthCodeNFC = 0
    private var prefixCardCodeNFC: String? = null
    private var postfixCardCodeNFC: String? = null


    companion object {
        private val LAYOUT = R.layout.fragment_settings
        private val FRAGMENT_SETTINGS_PARENT = "fragment_settings_parent"
        fun getInstance(isLogin: Boolean?): SettingsFragment {

            val args = Bundle()
            args.putBoolean(FRAGMENT_SETTINGS_PARENT, isLogin!!)
            val fragment = SettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun readBundle(bundle: Bundle?) {
        if (bundle != null) {
            isLogin = bundle.getBoolean(FRAGMENT_SETTINGS_PARENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(LAYOUT, container, false)

        readBundle(arguments)
        setHasOptionsMenu(true)
        if(activity is BasicActivity){
            (activity as BasicActivity).selectedFragment = this
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isLogin) {
            app_bar_layout_settings.visibility = View.VISIBLE
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            (activity as AppCompatActivity).supportActionBar!!.title = activity!!.getString(R.string.action_settings)
            (activity as AppCompatActivity).supportActionBar!!
                    .setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar!!
                    .setHomeAsUpIndicator(R.drawable.ic_close_white)
        } else {
            (activity as AppCompatActivity).supportActionBar!!.title =
                    activity!!.getString(R.string.action_settings)
        }

        serverUrl = SharedStorage.getString(activity as Context, APP_SETTINGS_PREFS, SERVER, "")
        server.setText(serverUrl)
        server.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                serverUrl = p0.toString()
            }
        })

        id_device.text = Settings.Secure.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        switch_finger_print_container.visibility =
                if (FingerprintUtils.checkFingerprintCompatibility(activity as Context)) View.VISIBLE else View.GONE

        switch_finger_print.isChecked = SharedStorage.getBoolean(activity as Context, APP_SETTINGS_PREFS, USE_FINGEPRINT, true)
        switch_finger_print.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                SharedStorage.setString(activity as Context, APP_SETTINGS_PREFS, Consts.USER_PASS, "")
            }
            SharedStorage.setBoolean(activity as Context, APP_SETTINGS_PREFS, USE_FINGEPRINT, isChecked)
        }

        clear_finger_print_pass.setOnClickListener { _ ->
            ActivityUtils.showQuestion(activity as Context, getString(R.string.finger), null,
                    getString(R.string.clear_finger_print_pass), null, null, null,
                    object : ActivityUtils.QuestionAnswer {
                        override fun onNegativeAnswer() {}

                        override fun onNeutralAnswer() {}

                        override fun onPositiveAnswer() {
                            SharedStorage.setString(activity as Context, APP_SETTINGS_PREFS, Consts.USER_PASS, "")
                        }

                    })
        }

        modeList = getCameraList()
        initSelectionMode(SharedStorage.getInteger(activity!!, APP_SETTINGS_PREFS, MODE, 0))
        select_mode.setOnClickListener { showDialogSelectMode() }

        block_auto_validate.isChecked = SharedStorage.getBoolean(activity!!,
                APP_SETTINGS_PREFS, BLOCK_AUDIO_VALIDATE, false)
        block_auto_validate.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            SharedStorage.setBoolean(activity!!, APP_SETTINGS_PREFS, BLOCK_AUDIO_VALIDATE, isChecked)
        }

        val scanType = object : TypeToken<List<SelectedItem>>() {}.type
        var listScans = Gson().fromJson<List<SelectedItem>>(SharedStorage.getString(activity!!, APP_SETTINGS_PREFS, TYPE_SCAN, ""), scanType)
        initSelectionTypeScan(listScans)
        select_scan_type.setOnClickListener { showDialogSelectScanner() }

        lengthCodeNFC = SharedStorage.getInteger(activity!!, APP_SETTINGS_PREFS, LENGTH_SCAN_CARD_CODE, 0)
        card_code_length.setText(java.lang.String.valueOf(lengthCodeNFC))
        card_code_length.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                lengthCodeNFC = 0
                try {
                    lengthCodeNFC = Integer.valueOf(p0.toString())
                } catch (e: Exception) {
                }
            }
        })
        prefixCardCodeNFC = SharedStorage.getString(activity!!, APP_SETTINGS_PREFS, PREFIX_SCAN_CARD_CODE, "")
        prefix_card_code.setText(prefixCardCodeNFC)
        prefix_card_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                prefixCardCodeNFC = p0.toString()
            }
        })

        postfixCardCodeNFC = SharedStorage.getString(activity!!, APP_SETTINGS_PREFS, POSTFIX_SCAN_CARD_CODE, "")
        postfix_card_code.setText(postfixCardCodeNFC)
        postfix_card_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                postfixCardCodeNFC = p0.toString()
            }
        })

    }

    override fun onNavigationClick(v: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        super.onDestroy()

        SharedStorage.setInteger(activity!!,
                APP_SETTINGS_PREFS, LENGTH_SCAN_CARD_CODE, lengthCodeNFC)
        SharedStorage.setString(activity!!,
                APP_SETTINGS_PREFS, PREFIX_SCAN_CARD_CODE, prefixCardCodeNFC)
        SharedStorage.setString(activity!!,
                APP_SETTINGS_PREFS, POSTFIX_SCAN_CARD_CODE, postfixCardCodeNFC)
        val oldServer = SharedStorage.getString(activity as Context, APP_SETTINGS_PREFS, SERVER, "")
        SharedStorage.setString(activity as Context, APP_SETTINGS_PREFS, SERVER, serverUrl)
        Repository.getInstance(activity as Context).buildSyncService()
        if (!TextUtils.isEmpty(oldServer) && !oldServer.equals(serverUrl, ignoreCase = true)) {
            logout()
        }
    }

    private fun logout() {
        SharedStorage.setLong(activity as Context, APP_CASH_TOKEN_PREFS, VALID_DATE, 0)
        SharedStorage.setString(activity as Context, APP_CASH_TOKEN_PREFS, TOKEN, "")
    }

    private fun showDialogSelectMode() {
        ActivityUtils.showSelectionList(
                activity,
                activity!!.getString(R.string.select_mode),
                null,
                modeList, object : ActivityUtils.ListItemClick {
            override fun onItemClik(item: Int, text: String?) {
                SharedStorage.setInteger(activity!!,
                        APP_SETTINGS_PREFS, MODE, item)
                if(activity is BasicActivity) {
                    (activity as BasicActivity?)!!.setVisibilityNavigationViewItems()
                }
                initSelectionMode(item)
            }
        })
    }

    private fun showDialogSelectScanner() {

        val scanType = object : TypeToken<List<SelectedItem>>() {}.type
        var listScans = Gson().fromJson<List<SelectedItem>>(SharedStorage.getString(activity!!, APP_SETTINGS_PREFS, TYPE_SCAN, ""), scanType)
        if(listScans.isNullOrEmpty()){
            listScans = TYPE_SCAN_LIST
        }
        var listNameScans = mutableListOf<String>()
        var listSelectedScans = mutableListOf<Boolean>()
        listScans.forEach {
            listNameScans.add(it.nameScan)
            listSelectedScans.add(it.selected)
        }

        ActivityUtils.showMultiSelectionList(
                activity,
                activity!!.getString(R.string.type_scan),
                null, listScans, object : ActivityUtils.ListItemMultiClick {
            override fun onItemClick(list: List<SelectedItem>) {
                SharedStorage.setString(activity!!, APP_SETTINGS_PREFS, TYPE_SCAN, Gson().toJson(listScans))
                initSelectionTypeScan(listScans)
            }
        })
    }

    private fun getCameraList(): List<String>? {
        val selectionList: MutableList<String> = ArrayList()
        selectionList.add(activity!!.getString(R.string.mode_1))
        selectionList.add(activity!!.getString(R.string.mode_2))
        return selectionList
    }

    private fun initSelectionMode(item: Int) {
        select_mode.setText(if (modeList != null && modeList!!.isNotEmpty()) modeList!![item] else activity!!.getString(R.string.mode_1))
        if(item == 1) {
            block_auto_validate.visibility = View.VISIBLE
        }else{
            block_auto_validate.visibility = View.GONE
        }
    }

    private fun initSelectionTypeScan(list: List<SelectedItem>?) {
        var listScans = list
        if(listScans.isNullOrEmpty()){
            var type = TYPE_SCAN_CAMERA
            type.selected = true
            listScans = listOf(type, TYPE_SCAN_NFC, TYPE_SCAN_USB)
            SharedStorage.setString(activity!!, APP_SETTINGS_PREFS, TYPE_SCAN, Gson().toJson(listScans))
        }
        var text = ""
        var useNFC = false
        listScans.forEach {
            if(it.selected) {
                text = text.plus(if (text.isNullOrEmpty()) {
                    ""
                } else {
                    ", "
                }).plus(it.nameScan)
                if(it.id == 1){
                    useNFC = true
                }
            }
        }
        select_scan_type.setText(text)
        if(useNFC){
            use_nfc_content.visibility = View.VISIBLE
        }else{
            use_nfc_content.visibility = View.GONE
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }
}
