package ua.com.expert.validator.ui.usbvalidate

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView.OnEditorActionListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.client.android.Intents
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_scan.*
import kotlinx.android.synthetic.main.layout_confirmed.view.*
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.model.SelectedItem
import ua.com.expert.validator.model.ValidateEvent
import ua.com.expert.validator.model.dto.ValidateRequest
import ua.com.expert.validator.model.dto.ValidateResponse
import ua.com.expert.validator.ui.FragmentDispatchKeyEvent
import ua.com.expert.validator.ui.FragmentNFCScanListener
import ua.com.expert.validator.ui.FragmentStartScanListener
import ua.com.expert.validator.ui.basic.BasicActivity
import ua.com.expert.validator.ui.qrscanner.ScannerQrActivity
import ua.com.expert.validator.utils.ActivityUtils
import ua.com.expert.validator.utils.ScanCardUtils
import ua.com.expert.validator.utils.SharedStorage


class UsbValidateFragment : Fragment(), FragmentDispatchKeyEvent, FragmentStartScanListener, FragmentNFCScanListener {

    private lateinit var viewModel: UsbValidateViewModel
    private var animationScan: Animation? = null
    private val barcode = StringBuffer()
    private var usbConnected = false
    private lateinit var listScans: List<SelectedItem>
    companion object {
        private const val LAYOUT = R.layout.fragment_scan
        private const val REQ_QR_CODE = 205
//        private const val REQ_NFC_CODE = 206
        fun getInstance(): UsbValidateFragment {

            val args = Bundle()
            val fragment = UsbValidateFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private fun readBundle(bundle: Bundle?) {
        if (bundle != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view =  inflater.inflate(LAYOUT, container, false)
        readBundle(arguments)
        setHasOptionsMenu(true)
        animationScan = AnimationUtils.loadAnimation(activity, R.anim.scan)
        (activity as BasicActivity).selectedFragment = this

        val scanType = object : TypeToken<List<SelectedItem>>() {}.type
        listScans = Gson().fromJson<List<SelectedItem>>(SharedStorage.getString(activity!!, Consts.APP_SETTINGS_PREFS, Consts.TYPE_SCAN, ""), scanType)
        if(listScans.isNullOrEmpty()){
            var type = Consts.TYPE_SCAN_CAMERA
            type.selected = true
            listScans = listOf(type, Consts.TYPE_SCAN_NFC, Consts.TYPE_SCAN_USB)
            SharedStorage.setString(activity!!, Consts.APP_SETTINGS_PREFS, Consts.TYPE_SCAN, Gson().toJson(listScans))
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bar_scan.startAnimation(animationScan)

        (activity as AppCompatActivity).supportActionBar!!.title =
                activity!!.getString(R.string.mode_2)

        validateButton.setOnClickListener {
            viewModel.validate(ValidateRequest(enter_code.text.toString().replace("[^\\p{L}\\p{Z}]", "")))
            enter_code.text.clear()
        }

        enter_code.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.validate(ValidateRequest(enter_code.text.toString().replace("[^\\p{L}\\p{Z}]", "")))
                enter_code.text.clear()
                true
            } else false
        })


        viewModel = ViewModelProviders.of(this,
                UsbValidateViewModel.ViewModelFactory(activity as Context))
                .get(UsbValidateViewModel::class.java)

        viewModel.validateResponse.observe(this, Observer<ValidateEvent> { t ->
            if (t != null) {
                when (t.status) {
                    ValidateEvent.START -> (activity as BasicActivity).showDialogLoad(null, null)
                    ValidateEvent.ERROR -> {
                        if ((t.result as ValidateResponse).errorCode == 401) {
                            (activity as BasicActivity).cancelDialogLoad()
                            (activity as BasicActivity).inLogOut(true)
                        } else {
                            (activity as BasicActivity).cancelDialogLoad()
                            t.result.error?.let { ActivityUtils.showMessage(activity as Context, null, null, it) }
                        }
                    }
                    ValidateEvent.FINISH -> {
                        (activity as BasicActivity).cancelDialogLoad()
                        showDialogResult(t.result as ValidateResponse)
                    }
                }
            }
        })
        if (listScans.get(1).selected) {
            (activity as BasicActivity).initNFCScan()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (listScans.get(0).selected) {
            inflater.inflate(R.menu.menu_hall, menu)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when(item.itemId) {
            R.id.action_qr_scan -> {
                var selectionList = mutableListOf<String>()
                listScans.forEach {
                    if(it.id == 0 && it.selected){
                        selectionList.add(it.nameScan)
                    }
                }
                if(selectionList.size==1){
                    if(listScans[0].nameScan.equals(selectionList.get(0))) {
                        startActivityForResult(Intent(context, ScannerQrActivity::class.java), REQ_QR_CODE)
//                    }else if(listScans[1].nameScan.equals(selectionList.get(0))) {
//                        if(!NFCUtils.deviceHasNFC(getActivity())) {
//                            ActivityUtils.showMessage(activity as Context, null, null, getString(R.string.nfc_not_init))
//                            return true
//                        }
//                        startActivityForResult(Intent(context, NFCReadActivity::class.java), REQ_NFC_CODE)
                    }
                    return true
                }
//                ActivityUtils.showSelectionList(
//                        activity,
//                        activity!!.getString(R.string.select_mode),
//                        null,
//                        selectionList, object : ActivityUtils.ListItemClick {
//                    override fun onItemClik(item: Int, text: String?) {
//                        if(listScans[0].nameScan.equals(text)) {
//                            startActivityForResult(Intent(context, ScannerQrActivity::class.java), REQ_QR_CODE)
//                        }else if(listScans[1].nameScan.equals(text)) {
//                            if(!NFCUtils.deviceHasNFC(getActivity())) {
//                                ActivityUtils.showMessage(activity as Context, null, null, getString(R.string.nfc_not_init))
//                                return
//                            }
//                            startActivityForResult(Intent(context, NFCReadActivity::class.java), REQ_NFC_CODE)
//                        }
//                    }
//                })

                true
            }
            else ->
                super.onOptionsItemSelected(item)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_QR_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    var qrCode = data!!.getStringExtra(Intents.Scan.RESULT)
                    viewModel.validate(ValidateRequest(qrCode.replace("[^\\p{L}\\p{Z}]", "")))
                }
            }
//            REQ_NFC_CODE -> when (resultCode) {
//                Activity.RESULT_OK -> {
//                    val result = data!!.getStringExtra(ACTIVITY_NFC_RESULT)
//                    viewModel.validate(ValidateRequest(result.replace("[^\\p{L}\\p{Z}]","")))
//                }
//            }
        }
    }

    private fun showDialogResult(result: ValidateResponse){

        if (result==null) return

        val builder = AlertDialog.Builder(activity!!, R.style.WhiteDialogTheme)

        val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)

        titleView.text_title.text = resources.getString(R.string.questions_title_info)

        builder.setCustomTitle(titleView)

        val contentView = LayoutInflater.from(activity).inflate(R.layout.layout_confirmed, null)

        if(!TextUtils.isEmpty(result.error) || result.exists==false){
            contentView.image.setImageDrawable(resources.getDrawable(R.drawable.canceled))
            contentView.text.text = resources.getString(R.string.canceled)
            contentView.text.setTextAppearance(activity as Context, R.style.TextStyle_Large_Bold_Red)
            contentView.info.text = result.error
            contentView.info.visibility = View.VISIBLE
        }

        builder.setView(contentView)

        builder.setNeutralButton(activity!!.getString(R.string.questions_answer_ok)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            dialog.show()
        } catch (e: Exception) {
            Log.e(Consts.TAGLOG, e.toString())
        }

        val button1 = dialog.findViewById<View>(android.R.id.button1) as Button?
        button1!!.setTextColor(activity!!.resources.getColor(R.color.colorAccent))

    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!listScans.get(2).selected) {
            return true
        }
        if(SharedStorage.getBoolean(activity!!,
                Consts.APP_SETTINGS_PREFS, Consts.BLOCK_AUDIO_VALIDATE, false) || !usbConnected){
            return true
        }
        if (event.action == KeyEvent.ACTION_DOWN) {
            val pressedKey = event.unicodeChar.toChar()
            barcode.append(pressedKey)
        }
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
            var result = barcode.toString().replace("[^A-Za-z0-9]", "")
            viewModel.validate(ValidateRequest(result))
            enter_code.text.clear()
            Log.d(Consts.TAGLOG, result)
            barcode.delete(0, barcode.length)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        try {
            if (listScans.get(1).selected) {
                (activity as BasicActivity).startNFCScan()
            }
            startScan((activity as BasicActivity).usbConnected || listScans.get(1).selected)
        }catch (e: java.lang.Exception){}
    }

    override fun onPause() {
        super.onPause()
        if (listScans.get(1).selected) {
            (activity as BasicActivity).stopNFCScan()
        }
    }

    override fun startScan(show: Boolean) {
        if (!listScans.get(1).selected && !listScans.get(2).selected) {
            return
        }
        if(listScans.get(1).selected){
            nfc_scan.visibility = View.VISIBLE
        }else{
            nfc_scan.visibility = View.GONE
        }
        if(listScans.get(2).selected){
            qr_code.visibility = View.VISIBLE
        }else{
            qr_code.visibility = View.GONE
        }
        usbConnected = show
        val set = ConstraintSet()
        set.clone(main_container)
        if(show){
            scan_container.visibility = View.VISIBLE
            set.connect(
                    R.id.scan_layout,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM)
        } else {
            scan_container.visibility = View.GONE
            set.clear(R.id.scan_layout, ConstraintSet.BOTTOM)
        }

        TransitionManager.beginDelayedTransition(main_container)
        set.applyTo(main_container)
    }

    override fun onFound(code: String) {
        viewModel.validate(ValidateRequest(ScanCardUtils.getCardCode(activity, code.replace("[^\\p{L}\\p{Z}]", ""))))
    }

}