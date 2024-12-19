import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Save the login state
    fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", isLoggedIn)
        editor.apply()
    }

    // Get the login state
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    // Save the "keep me logged in" state
    fun saveKeepLoggedInState(keepLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("keep_logged_in", keepLoggedIn)
        editor.apply()
    }

    // Get the "keep me logged in" state
    fun isKeepLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("keep_logged_in", false)
    }
}