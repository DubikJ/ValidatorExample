package ua.com.expert.validator.ui.validate

import ua.com.expert.validator.model.dto.CheckTiketRequest
import ua.com.expert.validator.model.dto.DownloadResponse

interface ValidMVPContract{

    interface View {

        fun onStartLoad(title: String?, message: String?)

        fun onError(error: String)

        fun result(result: DownloadResponse)

        fun onLogOut()

    }

    interface Repository {

        fun onDestroy()

        fun checkTicket(request: CheckTiketRequest)

    }
}