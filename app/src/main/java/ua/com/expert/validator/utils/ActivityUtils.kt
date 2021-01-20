package ua.com.expert.validator.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import ua.com.expert.validator.R
import ua.com.expert.validator.common.Consts.TAGLOG
import ua.com.expert.validator.model.SelectedItem


object ActivityUtils{

    fun showMessage(mContext: Context, textTitle: String?, drawableIconTitle: Drawable?,
                    textMessage: String): AlertDialog? {
        if (mContext == null || textMessage.isEmpty()) return null

        val builder = AlertDialog.Builder(mContext, R.style.WhiteDialogTheme)

        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
        if (drawableIconTitle != null) {
            titleView.image_title.setImageDrawable(drawableIconTitle)
        }
        titleView.text_title.text = if (TextUtils.isEmpty(textTitle)) {
            mContext.getString(R.string.questions_title_error)
        }else{
            textTitle
        }
        builder.setCustomTitle(titleView)
        builder.setMessage(textMessage)

        builder.setNeutralButton(mContext.getString(R.string.questions_answer_ok)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAGLOG, e.toString())
        }

        val textView = dialog.findViewById<View>(android.R.id.message) as TextView?
        textView!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
        val button1 = dialog.findViewById<View>(android.R.id.button1) as Button?
        button1!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
        val button2 = dialog.findViewById<View>(android.R.id.button2) as Button?
        button2!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
        val button3 = dialog.findViewById<View>(android.R.id.button3) as Button?
        button3!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))

        return dialog
    }

    fun showShortToast(mContext: Context, message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(mContext: Context, message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
    }

     fun showQuestion(mContext: Context, textTitle: String?, drawableIconTitle: Drawable?,
                      textMessage: String,
                      nameButton1: String?, nameButton2: String?, nameButton3: String?,
                      questionAnswer: QuestionAnswer) : AlertDialog? {

         if (mContext == null || textMessage.isEmpty()) return null

         val builder = AlertDialog.Builder(mContext, R.style.WhiteDialogTheme)

         val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
         val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
         if (drawableIconTitle == null) {
             titleView.image_title.visibility = View.GONE
         } else {
             titleView.image_title.setImageDrawable(drawableIconTitle)
         }

         titleView.text_title.text = if (textTitle != null && !textTitle.isEmpty())
             textTitle
         else
             mContext.getString(R.string.questions_title_question)

         builder.setCustomTitle(titleView)
         builder.setMessage(textMessage)

         builder.setPositiveButton(if (TextUtils.isEmpty(nameButton1))
             mContext.getString(R.string.questions_answer_yes)
         else
             nameButton1) { dialog, _ ->
             dialog.dismiss()
             questionAnswer.onPositiveAnswer()
         }

         builder.setNegativeButton(if (TextUtils.isEmpty(nameButton2))
             mContext.getString(R.string.questions_answer_no)
         else
             nameButton2) { dialog, _ ->
             dialog.dismiss()
             questionAnswer.onNegativeAnswer()
         }

         if (!TextUtils.isEmpty(nameButton3)) {
             builder.setNeutralButton(nameButton3
             ) { dialog, which ->
                 dialog.dismiss()
                 questionAnswer.onNeutralAnswer()
             }
         }

         builder.setOnCancelListener { questionAnswer.onNegativeAnswer() }
         val dialog = builder.create()
         dialog.setCanceledOnTouchOutside(false)
         dialog.setCancelable(false)
         try {
             dialog.show()
         } catch (e: Exception) {
             Log.e(TAGLOG, e.toString())
             return null
         }

         val textView = dialog.findViewById<View>(android.R.id.message) as TextView?
         textView!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
         val button1 = dialog.findViewById<View>(android.R.id.button1) as Button?
         button1!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
         val button2 = dialog.findViewById<View>(android.R.id.button2) as Button?
         button2!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
         val button3 = dialog.findViewById<View>(android.R.id.button3) as Button?
         button3!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))

         return dialog
     }

    interface QuestionAnswer {

        fun onPositiveAnswer()

        fun onNegativeAnswer()

        fun onNeutralAnswer()

    }

    fun showMessageWihtCallBack(mContext: Context, textTitle: String?, drawableIconTitle: Drawable?,
                                textMessage: String, messageCallBack: MessageCallBack) : AlertDialog? {

        if (mContext == null || textMessage.isEmpty()) return null

        val builder = AlertDialog.Builder(mContext, R.style.WhiteDialogTheme)

        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
        if (drawableIconTitle != null) {
            titleView.image_title.setImageDrawable(drawableIconTitle)
        }
        titleView.text_title.text = if (TextUtils.isEmpty(textTitle))
            mContext.getString(R.string.questions_title_error)
        else
            textTitle
        builder.setCustomTitle(titleView)
        builder.setMessage(textMessage)

        builder.setNeutralButton(mContext.getString(R.string.questions_answer_ok)) { dialog, which ->
            dialog.dismiss()
            messageCallBack.onPressOk()
        }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAGLOG, e.toString())
            return null
        }

        val textView = dialog.findViewById<View>(android.R.id.message) as TextView?
        textView!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
        val button1 = dialog.findViewById<View>(android.R.id.button1) as Button?
        button1!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
        val button2 = dialog.findViewById<View>(android.R.id.button2) as Button?
        button2!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))
        val button3 = dialog.findViewById<View>(android.R.id.button3) as Button?
        button3!!.setTextColor(mContext.resources.getColor(R.color.colorAccent))

        return dialog
    }

    interface MessageCallBack {

        fun onPressOk()

    }

    fun showSnackBar(mContext: Context, viewParent: View, colorBackground: Int,
                     textMessage: String, snackBarCallBack: SnackBarCallBack): Snackbar {
        val custom = LayoutInflater.from(mContext).inflate(R.layout.layout_new_version, null)
        val snackbar = Snackbar.make(viewParent, "", Snackbar.LENGTH_INDEFINITE)
        snackbar.view.setPadding(0, 0, 0, 0)
        (snackbar.view as ViewGroup).removeAllViews()
        (snackbar.view as ViewGroup).addView(custom)
        if (colorBackground > 0) {
            val container = custom.findViewById<View>(R.id.container)
            container.setBackgroundResource(colorBackground)
        }
        val updateNow = custom.findViewById<View>(R.id.update_now) as TextView?
        updateNow!!.text = textMessage
        updateNow!!.setOnClickListener { v -> snackBarCallBack.onCallBack() }
        val updateClose = custom.findViewById<View>(R.id.close_update)
        updateClose!!.setOnClickListener { v -> snackbar.dismiss() }
        try {
            snackbar.show()
        } catch (e: Exception) {
            Log.e(TAGLOG, e.toString())
        }

        return snackbar
    }

    interface SnackBarCallBack {

        fun onCallBack()

    }

    fun showSelectionList(mContext: Context?, textTitle: String?, drawableIconTitle: Drawable?,
                          listString: List<String>?, listItemClick: ListItemClick) {
        if (mContext == null) return
        if (listString == null) {
            return
        }
        val builder = AlertDialog.Builder(mContext, R.style.WhiteDialogTheme)
        builder.setTitle(if (textTitle != null && !textTitle.isEmpty()) textTitle else mContext.getString(R.string.questions_title_info))
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
        val imageTitle = titleView.findViewById<ImageView>(R.id.image_title)
        if (drawableIconTitle == null) {
            imageTitle.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_playlist_add_check_white))
        } else {
            imageTitle.setImageDrawable(drawableIconTitle)
        }
        val titleTV = titleView.findViewById<TextView>(R.id.text_title)
        titleTV.text = if (textTitle != null && !textTitle.isEmpty()) textTitle else mContext.getString(R.string.questions_select_from_list)
        builder.setCustomTitle(titleView)
        builder.setAdapter(ArrayAdapter(mContext,
                R.layout.row_sevice_item, R.id.textItem, listString)
        ) { dialog, which -> listItemClick.onItemClik(which, listString[which]) }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        try {
            dialog.show()
        } catch (e: java.lang.Exception) {
            Log.e(TAGLOG, e.toString())
        }
    }

    fun showMultiSelectionList(mContext: Context?, textTitle: String?, drawableIconTitle: Drawable?,
                               list: List<SelectedItem>, listItemMultiClick: ListItemMultiClick) {
        if (mContext == null) return
        if (list == null) {
            return
        }
        val builder = AlertDialog.Builder(mContext, R.style.WhiteDialogTheme)
        builder.setTitle(if (textTitle != null && !textTitle.isEmpty()) textTitle else mContext.getString(R.string.questions_title_info))
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val titleView = layoutInflater.inflate(R.layout.dialog_title, null)
        val imageTitle = titleView.findViewById<ImageView>(R.id.image_title)
        if (drawableIconTitle == null) {
            imageTitle.setImageDrawable(mContext.resources.getDrawable(R.drawable.ic_playlist_add_check_white))
        } else {
            imageTitle.setImageDrawable(drawableIconTitle)
        }
        val titleTV = titleView.findViewById<TextView>(R.id.text_title)
        titleTV.text = if (textTitle != null && !textTitle.isEmpty()) textTitle else mContext.getString(R.string.questions_select_from_list)
        builder.setCustomTitle(titleView)

        var listNameScans = mutableListOf<String>()
        var listSelectedScans = mutableListOf<Boolean>()
        list.forEach {
            listNameScans.add(it.nameScan)
            listSelectedScans.add(it.selected)
        }

        val listItems = listNameScans.toTypedArray()

        val checkedItems = BooleanArray(listSelectedScans.size)
        var index = 0
        for (`object` in listSelectedScans) {
            checkedItems[index++] = `object`
        }

        builder.setMultiChoiceItems(listItems, checkedItems) { dialog, which, isChecked ->
            list.forEach() { it ->
                if(it.id == which) {
                    it.selected = isChecked
                }
            }
        }
        builder.setPositiveButton( mContext.getString(R.string.questions_answer_ok)) { dialog, _ ->
            dialog.dismiss()
            listItemMultiClick.onItemClick(list)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        try {
            dialog.show()
        } catch (e: java.lang.Exception) {
            Log.e(TAGLOG, e.toString())
        }

    }

    interface ListItemClick {
        fun onItemClik(item: Int, text: String?)
    }

    interface ListItemMultiClick {
        fun onItemClick(list: List<SelectedItem>)
    }
}