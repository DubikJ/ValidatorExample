package ua.com.expert.validator.ui.halls

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.client.android.Intents
import kotlinx.android.synthetic.main.fragment_halls.*
import ua.com.expert.validator.R
import ua.com.expert.validator.adapter.HallsListAdapter
import ua.com.expert.validator.common.Consts
import ua.com.expert.validator.model.SelectedItem
import ua.com.expert.validator.model.dto.CinemaPlace
import ua.com.expert.validator.model.dto.CinemaPlaceResponse
import ua.com.expert.validator.ui.NFCReadActivity
import ua.com.expert.validator.ui.basic.BasicActivity
import ua.com.expert.validator.ui.qrscanner.ScannerQrActivity
import ua.com.expert.validator.ui.validate.ValidateFragment
import ua.com.expert.validator.utils.ActivityUtils
import ua.com.expert.validator.utils.NFCUtils
import ua.com.expert.validator.utils.SharedStorage

class HallsFragment : Fragment() {

    private lateinit var viewModel: HallsViewModel
    private lateinit var adapterResult : HallsListAdapter
    private lateinit var listScans: List<SelectedItem>
    companion object {

        private const val REQ_QR_CODE = 200
        private const val REQ_NFC_CODE = 201
        private val LAYOUT = R.layout.fragment_halls
        fun getInstance(): HallsFragment {

            val args = Bundle()
            val fragment = HallsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        (activity as BasicActivity).selectedFragment = this
        val scanType = object : TypeToken<List<SelectedItem>>() {}.type
        listScans = Gson().fromJson<List<SelectedItem>>(SharedStorage.getString(activity!!, Consts.APP_SETTINGS_PREFS, Consts.TYPE_SCAN, ""), scanType)
        if(listScans.isNullOrEmpty()){
            var type = Consts.TYPE_SCAN_CAMERA
            type.selected = true
            listScans = listOf(type, Consts.TYPE_SCAN_NFC, Consts.TYPE_SCAN_USB)
            SharedStorage.setString(activity!!, Consts.APP_SETTINGS_PREFS, Consts.TYPE_SCAN, Gson().toJson(listScans))
        }
        return inflater.inflate(LAYOUT, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as BasicActivity).enableNavigationView(true, false)

        swipe_list.setOnRefreshListener {
            viewModel.getCinemaPlaces()
        }
        viewModel = ViewModelProviders.of(this,
                HallsViewModel.ViewModelFactory(activity as Context))
                .get(HallsViewModel::class.java)

        viewModel.cinemaPlaceResponse.observe(this, Observer<CinemaPlaceResponse> { t ->
            swipe_list.isRefreshing = false
            if(t!=null) {
                if (TextUtils.isEmpty(t.error)) {
                    initDataToList(t.cinemaPlaceGroups)
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

        viewModel.getCinemaPlaces()

        swipe_list.isRefreshing = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (listScans.get(0).selected || listScans.get(1).selected) {
            inflater.inflate(R.menu.menu_hall, menu)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when(item.itemId) {
            R.id.action_qr_scan -> {
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
                            return true
                        }
                        startActivityForResult(Intent(context, NFCReadActivity::class.java), REQ_NFC_CODE)
                    }
                    return true
                }
                ActivityUtils.showSelectionList(
                        activity,
                        activity!!.getString(R.string.select_mode),
                        null,
                        selectionList, object : ActivityUtils.ListItemClick {
                    override fun onItemClik(item: Int, text: String?) {
                        if(listScans[0].nameScan.equals(text)) {
                            startActivityForResult(Intent(context, ScannerQrActivity::class.java), REQ_QR_CODE)
                        }else if(listScans[1].nameScan.equals(text)) {
                            if(!NFCUtils.deviceHasNFC(getActivity())) {
                                ActivityUtils.showMessage(activity as Context, null, null, getString(R.string.nfc_not_init))
                                return
                            }
                            startActivityForResult(Intent(context, NFCReadActivity::class.java), REQ_NFC_CODE)
                        }
                    }
                })

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
                    var qrCode = data!!.getStringExtra(Intents.Scan.RESULT).replace("[^\\p{L}\\p{Z}]","")
                    for (item in adapterResult.list) {
                        if (item.id == qrCode.toInt()) {
                            (activity as BasicActivity).showNextFragment(ValidateFragment.getInstance(item))
                            return
                        }
                    }
                    ActivityUtils.showShortToast(activity as Context, resources.getString(R.string.hall_not_found))
                }
            }
            REQ_NFC_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val qrCode = data!!.getStringExtra(NFCReadActivity.ACTIVITY_NFC_RESULT).replace("[^\\p{L}\\p{Z}]","")
                    for (item in adapterResult.list) {
                        if (item.id == qrCode.toInt()) {
                            (activity as BasicActivity).showNextFragment(ValidateFragment.getInstance(item))
                            return
                        }
                    }
                    ActivityUtils.showShortToast(activity as Context, resources.getString(R.string.hall_not_found))
                }
            }
        }
    }


    private fun initStartData(){

        (activity as AppCompatActivity).supportActionBar!!.title =
                activity!!.getString(R.string.action_halls)

        adapterResult = HallsListAdapter(
                activity as Context,
                object : HallsListAdapter.ClickListener {
                    override fun onItemClick(position: Int) {
                        var item = adapterResult.getSelectedItem(position)
                        if (item == null) {
                            ActivityUtils.showShortToast(activity as Context, resources.getString(R.string.error_retrieving_data))
                        }
                        (activity as BasicActivity).showNextFragment(ValidateFragment.getInstance(item!!))
                    }

                })
        list_halls.layoutManager = LinearLayoutManager(activity as Context)
        list_halls.adapter = adapterResult

        val c1 = ContextCompat.getColor(activity as Context, R.color.colorPrimaryDark)
        val c2 = ContextCompat.getColor(activity as Context, R.color.colorPrimaryLight)
        val c3 = ContextCompat.getColor(activity as Context, R.color.colorGreen)

        swipe_list.setColorSchemeColors(c1, c2, c3)
    }

    private fun initDataToList(list : List<CinemaPlace>?){
        if(list!=null) {
            adapterResult.list = list
            adapterResult.notifyDataSetChanged()
        }
    }

}