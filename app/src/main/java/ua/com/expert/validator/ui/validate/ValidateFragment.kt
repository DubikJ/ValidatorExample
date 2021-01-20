package ua.com.expert.validator.ui.validate

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import com.alien.barcode.BarcodeReader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.client.android.Intents
import kotlinx.android.synthetic.main.fragment_halls.swipe_list
import kotlinx.android.synthetic.main.fragment_validate.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_halls.swipe_list
import kotlinx.android.synthetic.main.fragment_validate.*
import kotlinx.android.synthetic.main.layout_confirmed.view.*
import kotlinx.android.synthetic.main.layout_confirmed.view.image
import kotlinx.android.synthetic.main.layout_scan.view.*
import ua.com.expert.validator.R
import ua.com.expert.validator.adapter.SessionsListAdapter
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.common.Consts.NAME_DEVICE_ALIEN
import ua.com.expert.validator.model.SelectedItem
import ua.com.expert.validator.model.dto.*
import ua.com.expert.validator.ui.FragmentBarcodeListener
import ua.com.expert.validator.ui.NFCReadActivity
import ua.com.expert.validator.ui.basic.BasicActivity
import ua.com.expert.validator.ui.qrscanner.ScannerQrActivity
import ua.com.expert.validator.utils.ActivityUtils
import ua.com.expert.validator.utils.DeviceUtils
import ua.com.expert.validator.utils.NFCUtils
import ua.com.expert.validator.utils.SharedStorage


class ValidateFragment : Fragment(), ValidMVPContract.View, FragmentBarcodeListener {

    private lateinit var viewModel: ValidateViewModel
    private lateinit var adapter : SessionsListAdapter
    private var placeId  : Int = 0
    private var placeName: String? = null
    private var placeCount  : Int = 0
    private lateinit var validPresenter: ValidPresenter
    private var barcodeReader: BarcodeReader? = null
    private var dialogScan: AlertDialog? = null
    private lateinit var listScans: List<SelectedItem>
    companion object {

        private const val LAYOUT = R.layout.fragment_validate
        private const val FRAGMENT_HALLS_PLACE_ID = "fragment_halls_place_id"
        private const val FRAGMENT_HALLS_PLACE_NAME = "fragment_halls_place_name"
        private const val FRAGMENT_HALLS_PLACE_COUNT = "fragment_halls_place_count"
        private const val REQ_QR_CODE = 100
        private const val REQ_NFC_CODE = 101

        fun getInstance(cinemaPlace: CinemaPlace): ValidateFragment {

            val args = Bundle()
            val fragment = ValidateFragment()
            args.putInt(FRAGMENT_HALLS_PLACE_ID, cinemaPlace.id)
            args.putString(FRAGMENT_HALLS_PLACE_NAME, cinemaPlace.name)
            args.putInt(FRAGMENT_HALLS_PLACE_COUNT, cinemaPlace.placeCount)
            fragment.arguments = args
            return fragment
        }
    }

    private fun readBundle(bundle: Bundle?) {
        if (bundle != null) {
            placeId = bundle.getInt(FRAGMENT_HALLS_PLACE_ID)
            placeName = bundle.getString(FRAGMENT_HALLS_PLACE_NAME)
            placeCount = bundle.getInt(FRAGMENT_HALLS_PLACE_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view =  inflater.inflate(LAYOUT, container, false)

        readBundle(arguments)
        setHasOptionsMenu(true)
        validPresenter = ValidPresenter(activity as Context, this)
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

        (activity as BasicActivity).enableNavigationView(false, false)

        if(placeId==0){
            (activity as BasicActivity).onBackPressed()
        }

        swipe_list.setOnRefreshListener {
            viewModel.getCinemaSessions(CinemaSessionRequest(placeId))
        }
        viewModel = ViewModelProviders.of(this,
                ValidateViewModel.ViewModelFactory(activity as Context))
                .get(ValidateViewModel::class.java)

        viewModel.cinemaSessionResponse.observe(this, Observer<CinemaSessionResponse> { t ->
            swipe_list.isRefreshing = false
            if(t!=null) {
                if (TextUtils.isEmpty(t.error)) {
                    initDataToList(t.cinemaSessions)
                } else {
                    if(t.errorCode == 401){
                        (activity as BasicActivity).inLogOut(true)
                    }else {
                        ActivityUtils.showMessage(activity as Context, null, null, t.error!!)
                    }
                }
            }else{
                ActivityUtils.showMessage(activity as Context, null, null, resources.getString(R.string.error_retrieving_data))
            }})

        initStartData()

        viewModel.getCinemaSessions(CinemaSessionRequest(placeId))

        swipe_list.isRefreshing = true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }

    private fun initStartData(){

        (activity as AppCompatActivity).supportActionBar!!.title = placeName

        adapter = SessionsListAdapter(
                activity as Context,
                object : SessionsListAdapter.ClickListener {
                    override fun onItemClick(position: Int) {

                    }

                })
        list_sessions.layoutManager = LinearLayoutManager(activity as Context)
        list_sessions.adapter = adapter

        camera_scan.setOnClickListener {
            var selectionList = mutableListOf<String>()
            listScans.forEach(){
                if((it.id == 0 || it.id == 1) && it.selected){
                    selectionList.add(it.nameScan)
                }
            }
            if(selectionList.size==1){
                if(listScans[0].nameScan.equals(selectionList.get(0))) {
                    startActivityForResult(Intent(context, ScannerQrActivity::class.java), REQ_QR_CODE)
                }else if(listScans[1].nameScan.equals(selectionList.get(0))) {
                    if(!NFCUtils.deviceHasNFC(getActivity())) {
                        ActivityUtils.showMessage(activity as Context, null, null, getString(R.string.nfc_not_init))
                    }else {
                        startActivityForResult(Intent(context, NFCReadActivity::class.java), REQ_NFC_CODE)
                    }
                }
            }else {
                ActivityUtils.showSelectionList(
                        activity,
                        activity!!.getString(R.string.select_mode),
                        null,
                        selectionList, object : ActivityUtils.ListItemClick {
                    override fun onItemClik(item: Int, text: String?) {
                        if (listScans[0].nameScan.equals(text)) {
                            startActivityForResult(Intent(context, ScannerQrActivity::class.java), REQ_QR_CODE)
                        } else if (listScans[1].nameScan.equals(text)) {
                            if (!NFCUtils.deviceHasNFC(getActivity())) {
                                ActivityUtils.showMessage(activity as Context, null, null, getString(R.string.nfc_not_init))
                                return
                            }
                            startActivityForResult(Intent(context, NFCReadActivity::class.java), REQ_NFC_CODE)
                        }
                    }
                })
            }
        }

        qr_code_scan.setOnClickListener {
            startScan()
        }
        if(!DeviceUtils.getDeviceName()?.equals(NAME_DEVICE_ALIEN, ignoreCase = true)!!){
            qr_code_scan.visibility = View.GONE
        }

        val c1 = ContextCompat.getColor(activity as Context, R.color.colorPrimaryDark)
        val c2 = ContextCompat.getColor(activity as Context, R.color.colorPrimaryLight)
        val c3 = ContextCompat.getColor(activity as Context, R.color.colorGreen)

        swipe_list.setColorSchemeColors(c1, c2, c3)
    }

    private fun initDataToList(list : List<CinemaSession>?){
        adapter.setList(list)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_QR_CODE -> when (resultCode) {
                RESULT_OK -> {
                    var qrCode = data!!.getStringExtra(Intents.Scan.RESULT)

                    validPresenter.checkTicket(CheckTiketRequest(qrCode,
                            placeId, 0))
                }
            }
            REQ_NFC_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val qrCode = data!!.getStringExtra(NFCReadActivity.ACTIVITY_NFC_RESULT)

                    validPresenter.checkTicket(CheckTiketRequest(qrCode,
                            placeId, 0))
                }
            }
        }
    }

    override fun onStartLoad(title: String?, message: String?) {
        (activity as BasicActivity).showDialogLoad(null, null)
    }

    override fun onError(error: String) {
        (activity as BasicActivity).cancelDialogLoad()
        ActivityUtils.showMessage(activity as Context, null, null, error)
    }

    override fun result(result: DownloadResponse) {
        (activity as BasicActivity).cancelDialogLoad()
        showDialogResult(result)
    }

    override fun onLogOut() {
        (activity as BasicActivity).inLogOut(true)
    }

    private fun showDialogResult(result: DownloadResponse){

        if (result==null) return

        val builder = AlertDialog.Builder(activity!!, R.style.WhiteDialogTheme)

        val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)

        titleView.text_title.text = resources.getString(R.string.questions_title_info)

        builder.setCustomTitle(titleView)

        val contentView = LayoutInflater.from(activity).inflate(R.layout.layout_confirmed, null)

        if(!TextUtils.isEmpty(result.error)){
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

    override fun startScan() {
        showDialogScan()

        if (barcodeReader == null)
            barcodeReader = BarcodeReader(context as Context)

        barcodeReader?.start { barcode ->
            validPresenter.checkTicket(CheckTiketRequest(barcode, placeId, 0))
            dialogScan?.dismiss()}
        Handler().postDelayed({ dialogScan?.dismiss()}, 10000)
    }

    override fun stopScan() {
        barcodeReader?.stop()
        dialogScan?.dismiss()

    }

    private fun showDialogScan(){

        val builder = AlertDialog.Builder(activity!!, R.style.WhiteDialogTheme)

        val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)

        titleView.text_title.text = resources.getString(R.string.scanning)

        builder.setCustomTitle(titleView)

        val contentView = LayoutInflater.from(activity).inflate(R.layout.layout_scan, null)


       // val imageView = contentView.image
        val bar = contentView.bar as View

        val animation = AnimationUtils.loadAnimation(activity, R.anim.scan)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                bar.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        bar.visibility = View.VISIBLE
        bar.startAnimation(animation)

        builder.setView(contentView)

        builder.setNeutralButton(activity!!.getString(R.string.questions_answer_cancel)) { dialog, _ ->
            dialog.dismiss()
            barcodeReader?.stop()
        }

        dialogScan = builder.create()
        dialogScan?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            dialogScan?.show()
        } catch (e: Exception) {
            Log.e(Consts.TAGLOG, e.toString())
        }

        val button1 = dialogScan?.findViewById<View>(android.R.id.button1) as Button?
        button1!!.setTextColor(activity!!.resources.getColor(R.color.colorAccent))

    }
}