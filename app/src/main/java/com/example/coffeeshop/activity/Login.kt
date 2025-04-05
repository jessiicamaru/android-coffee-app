package com.example.coffeeshop.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.coffeeshop.R
import android.widget.LinearLayout
import android.util.Log
import com.example.coffeeshop.data_class.User
import com.example.coffeeshop.service.Service
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : Activity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private var service = Service();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        Log.d("client_id", "${R.string.default_web_client_id}")


        val myLinearLayout = findViewById<LinearLayout>(R.id.gmail_button)
        myLinearLayout.setOnClickListener {
            signIn()
        }

        Log.d("FIREBASE_INIT", "Firebase Auth: ${FirebaseAuth.getInstance()}")
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent

        if (signInIntent == null) {
            Log.e("GOOGLE_SIGNIN", "Lỗi: signInIntent bị null")
        } else {
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        startActivityForResult(signInIntent, RC_SIGN_IN)
        Log.d("GOOGLE_SIGNIN", "GoogleSignInClient: ${mGoogleSignInClient}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("GOOGLE_SIGNIN", "onActivityResult: requestCode = $requestCode, resultCode = $resultCode")

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("GOOGLE_SIGNIN", "Google Sign-In thành công: ${account.id}")

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("GOOGLE_SIGNIN", "Google Sign-In thất bại: ${e.statusCode}")
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.d("GOOGLE_SIGNIN", "Credential: $credential")
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.let {
                        Log.d("FIREBASE_USER", """
                        🟢 Đăng nhập thành công
                        🔹 UID: ${it.uid}
                        🔹 Display Name: ${it.displayName}
                        🔹 Email: ${it.email}
                        🔹 Photo URL: ${it.photoUrl}
                        🔹 Phone Number: ${it.phoneNumber}
                        🔹 Email Verified: ${it.isEmailVerified}
                        🔹 Provider ID: ${it.providerId}
                    """.trimIndent())

                        val userData = User(
                            uid = it.uid,
                            displayName = it.displayName ?: "Unknown",
                            email = it.email ?: "Unknown",
                            phoneNumber = it.phoneNumber ?: "Unknown",
                            photoUrl = it.photoUrl?.toString() ?: ""
                        )

                        service.postToSaveUser(userData) { success ->
                            if (success) {
                                val intent = Intent(this@Login, Home::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e("Retrofit", "Lưu user thất bại")
                            }
                        }
                    }
                } else {
                    Log.w("FIREBASE_LOG", "Đăng nhập Firebase thất bại", task.exception)
                }
            }
    }


    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

