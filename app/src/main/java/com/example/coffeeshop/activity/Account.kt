package com.example.coffeeshop.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.coffeeshop.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class Account : Activity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var logOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account)

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Tìm nút đăng xuất
        logOutButton = findViewById(R.id.logout_button)
        logOutButton.setOnClickListener {
            Log.d("AccountActivity", "Logout button clicked")
            val sharedPreferences = getSharedPreferences("CoffeeShopPrefs", MODE_PRIVATE)
            val uid = sharedPreferences.getString("uid", null)
            Log.d("SharePref", "$uid")
            signOut()
        }

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.selectedItemId = R.id.nav_account
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_fav -> {
                    startActivity(Intent(this, Like::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, Cart::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_pending -> {
                    startActivity(Intent(this, Order::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_account -> return@setOnItemSelectedListener true
                else -> false
            }
        }
    }

    private fun signOut() {
        Log.d("LOGOUT", "Starting logout process")
        try {
            mAuth.signOut()
            Log.d("LOGOUT", "Firebase logout completed")

            val sharedPreferences = getSharedPreferences("CoffeeShopPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("uid")
            editor.apply()
            Log.d("LOGOUT", "UID removed from SharedPreferences")

            mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                Log.d("LOGOUT", "Google Sign-In logout completed")
                redirectToLogin()
            }.addOnFailureListener { e ->
                Log.e("LOGOUT", "Google Sign-In logout failed", e)
                redirectToLogin()
            }
        } catch (e: Exception) {
            Log.e("LOGOUT", "Error during logout process", e)
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        Log.d("LOGOUT", "Redirecting to LoginActivity")
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}