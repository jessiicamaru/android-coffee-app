package com.example.coffeeshop.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.coffeeshop.R
import android.widget.LinearLayout
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val myLinearLayout = findViewById<LinearLayout>(R.id.gmail_button)
        myLinearLayout.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("FIREBASE_LOGSHIT", "Google Sign In thành công: ${account.id}")

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("FIREBASE_LOGSHIT", "Google sign in thất bại", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
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
                    }

                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("FIREBASE_LOG", "Đăng nhập Firebase thất bại", task.exception)
                }
            }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

