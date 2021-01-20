package ua.com.expert.validator.common

import ua.com.expert.validator.model.SelectedItem
import java.text.SimpleDateFormat

object Consts {

    const val TAGLOG = "ServioValid"
    const val TAGLOG_FINGERPRINT = "POSApp_FingerPrint"

    const val APP_SETTINGS_PREFS = "servio_valid_settings_prefs"
    const val APP_CASH_SETTINGS_PREFS = "servio_valid_cash_settings_prefs"
    const val APP_CASH_TOKEN_PREFS = "servio_valid_token_cash"

    // Settings
    const val CONNECT_TIMEOUT_SECONDS_RETROFIT = 20L
    const val CHECK_SERVER_TIMEOUT_SECONDS_RETROFIT = 20L
    const val UPLOAD_TIMEOUT_SECONDS_RETROFIT = 60
    const val CHECK_SERVER_SECONDS = 10
    const val TYPE_CONNECTION = "http://"
    const val CONNECT_SERVER_URL = "localhost"

    const val AUTHENTICATE_PATTERN_URL = "POSExternal/Authenticate"
    const val GET_CINEMA_PLACE_PATTERN_URL = "POSExternal/GetCinemaPlaceGroup"
    const val GET_CINEMA_SESSIONS_PATTERN_URL = "POSExternal/GetCinemaSessions"
    const val CHECK_TICKET_PATTERN_URL = "POSExternal/CheckTicket"
    const val VALIDATE_LICENSE_URL = "POSExternal/ValidateLicense"
    const val LOGOUT_PATTERN_URL = "POSExternal/Logout"
    const val GET_IMAGE_PATTERN_URL = "POSExternal/GetImage"
    const val QR_CODE_VALIDATE_PATTERN_URL = "POSExternal/QrCodeValidate"

    const val TOKEN_HEADER = "AccessToken"
    val DATE_SYNC_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val DATE_VALID_FORMAT = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
    val DATE_TIME_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
    val DATE_PRICE_FORMAT = SimpleDateFormat("dd MMM HH:mm")


    const val SYSTEM_CODE = "4503"
    const val CHECK_PASSWORD = "5370168"

    const val MAX_PASS_LENGTH = 10

    //Cash
    const val SERVER = "m_server"
    const val USE_FINGEPRINT = "use_fingerprint"
    const val DONT_ASK_USING_FINGEPRINT = "dont_ask_use_fingerprint"
    const val USE_PHOTO_TARIF_ITEMS = "use_photo_tarif_items"
    const val USER_PASS = "user_pass"

    const val USER_FIRST_NAME = "_user_first_name"
    const val USER_LAST_NAME = "_user_last_name"
    const val USER_MIDDLE_NAME = "_user_middle_name"
    const val USER_ID = "_user_id"
    const val NAME_TERMINAL = "_name_terminal"
    const val TIME_USE_APP = "_time_use_app"

    const val VALID_DATE = "_valid_date"
    const val TOKEN = "_token"
    const val MODE = "_mode"
    const val BLOCK_AUDIO_VALIDATE = "_block_auto_validate"
    const val TYPE_SCAN = "_type_scan"

    const val PREFIX_SCAN_CARD_CODE = "_prefix_scan_card_code"
    const val POSTFIX_SCAN_CARD_CODE = "_postfix_scan_card_code"
    const val LENGTH_SCAN_CARD_CODE = "_length_scan_card_code"

    const val NAME_DEVICE_ALIEN = "Alien ALR-H450"

    val TYPE_SCAN_CAMERA = SelectedItem(0, "Камера", false)
    val TYPE_SCAN_NFC = SelectedItem(1, "NFC сканер", false)
    val TYPE_SCAN_USB = SelectedItem(2, "USB сканер", false)

    val TYPE_SCAN_LIST = listOf(TYPE_SCAN_CAMERA, TYPE_SCAN_NFC, TYPE_SCAN_USB)

}
