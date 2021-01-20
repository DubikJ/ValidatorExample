package ua.com.expert.validator.model

class ValidateEvent (val status: Int, val result: Any? = null) {
    companion object {
        const val START = 1
        const val ERROR = 2
        const val FINISH = 3

    }
}