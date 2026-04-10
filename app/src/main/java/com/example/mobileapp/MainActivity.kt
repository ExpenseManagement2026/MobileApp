package com.example.mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.room.Room
import com.example.mobileapp.data.local.AppDatabase
import com.example.mobileapp.data.repositoryimpl.TransactionRepositoryImpl
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── 1. Khởi tạo Room Database ─────────────────────────────────
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "expense_db"
        ).build()

        // ── 2. Khởi tạo Firestore ─────────────────────────────────────
        val firestore = Firebase.firestore

        // ── 3. Tạo Repository (Manual DI) ─────────────────────────────
        val transactionRepository: com.example.mobileapp.domain.repository.TransactionRepository =
            TransactionRepositoryImpl(
                transactionDao = db.transactionDao(),
                firestore = firestore
            )

        // ── 4. Bật Edge-to-Edge & khởi chạy Compose ───────────────────
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    AppNavGraph(transactionRepository = transactionRepository)
                }
            }
        }
    }
}
