package com.example.mobileapp.Presentation.add

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.android.material.button.MaterialButton

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnExpense = findViewById<MaterialButton>(R.id.btnExpense)
        val btnIncome = findViewById<MaterialButton>(R.id.btnIncome)
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)
        val etAmount = findViewById<EditText>(R.id.etAmount)

        btnBack.setOnClickListener { finish() }

        // Cập nhật danh sách với các icon "dễ thương" vừa tạo
        val categories = listOf(
            CategoryModel(1, "Ăn uống", R.drawable.ic_food),
            CategoryModel(2, "Di chuyển", R.drawable.ic_taxi),
            CategoryModel(3, "Mua sắm", R.drawable.ic_shopping),
            CategoryModel(4, "Hóa đơn", R.drawable.ic_bill),
            CategoryModel(5, "Giải trí", R.drawable.ic_game),
            CategoryModel(6, "Sức khỏe", R.drawable.ic_health),
            CategoryModel(7, "Giáo dục", R.drawable.ic_education),
            CategoryModel(8, "Khác", R.drawable.ic_more)
        )

        rvCategories.layoutManager = GridLayoutManager(this, 3)
        val adapter = CategoryAdapter(categories) { category ->
            // Xử lý khi chọn danh mục
        }
        rvCategories.adapter = adapter

        // Xử lý chuyển đổi nút Chi tiêu / Thu nhập
        btnExpense.setOnClickListener {
            updateToggleButton(btnExpense, btnIncome, true)
        }

        btnIncome.setOnClickListener {
            updateToggleButton(btnExpense, btnIncome, false)
        }
    }

    private fun updateToggleButton(btnExpense: MaterialButton, btnIncome: MaterialButton, isExpense: Boolean) {
        if (isExpense) {
            btnExpense.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_red))
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnExpense.strokeWidth = 0

            btnIncome.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.main_green))
            btnIncome.strokeWidth = 3
            btnIncome.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_green))
        } else {
            btnIncome.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_green))
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnIncome.strokeWidth = 0

            btnExpense.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.main_red))
            btnExpense.strokeWidth = 3
            btnExpense.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main_red))
        }
    }
}
