package com.university.prm2

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
//import com.example.places.db.DBHelper

class DeleteDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context?.let {
//            dbHelper = DBHelper(it)

            return AlertDialog.Builder(it)

                .setTitle("Delete Place")
                .setMessage("Are you sure you want to delete this place?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener {
                        dialog, which ->
//                        dbHelper.deleteUser(tag.toString())
                    (activity as MainActivity).reloadActivity()
                })
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> })
                .setCancelable(false)
                .create()
        }
        return super.onCreateDialog(savedInstanceState)
    }
}