import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import me.aydgn.mymusictracker.MusicActivity
import me.aydgn.mymusictracker.R
import me.aydgn.mymusictracker.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var checkboxKeepLoggedIn: CheckBox
    private lateinit var txtRegister: TextView
    private lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        edtEmail = findViewById(R.id.editTextEmail)
        edtPassword = findViewById(R.id.editTextPassword)
        btnLogin = findViewById(R.id.btnLogin)
        checkboxKeepLoggedIn = findViewById(R.id.checkbox_keep_logged_in)
        txtRegister = findViewById(R.id.txtRegister)

        // Initialize SharedPrefsHelper
        sharedPrefsHelper = SharedPrefsHelper(this)

        // Check if user is already logged in
        if (sharedPrefsHelper.isLoggedIn()) {
            // User is logged in, navigate to the music section
            navigateToMusicActivity()
        }

        // Handle login button click
        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            // Validate email and password (can be extended further)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Save login state if "Keep me logged in" is checked
                if (checkboxKeepLoggedIn.isChecked) {
                    sharedPrefsHelper.saveLoginState(true)
                    sharedPrefsHelper.saveKeepLoggedInState(true)
                } else {
                    sharedPrefsHelper.saveLoginState(true)
                    sharedPrefsHelper.saveKeepLoggedInState(false)
                }

                // Navigate to the music section after successful login
                navigateToMusicActivity()
            } else {
                // Show validation error (optional)
            }
        }

        // Handle register link click
        txtRegister.setOnClickListener {
            // Navigate to the Register screen
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Navigate to MusicActivity
    private fun navigateToMusicActivity() {
        val intent = Intent(this, MusicActivity::class.java)
        startActivity(intent)
        finish() // Close the login activity to prevent user from going back
    }
}