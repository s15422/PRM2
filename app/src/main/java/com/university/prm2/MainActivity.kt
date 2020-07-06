package com.university.prm2


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.university.prm2.DBHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.details.view.*
import kotlinx.android.synthetic.main.edit_dialog.view.*
import kotlinx.android.synthetic.main.new_dialog.view.*


lateinit var dbHelper : DBHelper
private lateinit var selectedImage: ImageView
private lateinit var mUserReference: DatabaseReference

//Firebase references
private var mDatabaseReference: DatabaseReference? = null
private var mDatabase: FirebaseDatabase? = null
private var mAuth: FirebaseAuth? = null
//UI elements
private var tvFirstName: TextView? = null
private var tvLastName: TextView? = null
private var tvEmail: TextView? = null
private var tvEmailVerifiied: TextView? = null


class MainActivity : AppCompatActivity() {
    private val CAMERA_REQUEST = 1888
    private val MY_CAMERA_PERMISSION_CODE = 100

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DBHelper(this)
        val listView = findViewById<ListView>(R.id.placesListView)
        val mapBtn = findViewById<Button>(R.id.mapBtn)
        initialise()

        // When you first run the app on a new device, sqlite DB does not have any "Places" inside
        // Uncomment the function call below to get inital data to the DB
//        initialListFill();
        val mUser = mAuth!!.currentUser
        mUserReference = mDatabaseReference!!.child(mUser!!.uid)
        updateList(listView)


        mapBtn.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) as Place
            val dialogView = LayoutInflater.from(this).inflate(R.layout.details, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Details of  " + element.name)
            val mAlertDialog = mBuilder.show()

            dialogView.backBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }

            //Need to put here replacement of TextView with element info
        }

            // LongClicking on a item in list to edit data
            listView.setOnItemLongClickListener { parent, view, position, id ->
                val element = parent.getItemAtPosition(position) as Place
                val dialogView = LayoutInflater.from(this).inflate(R.layout.edit_dialog, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle("Edit info of " + element.name)
                val mAlertDialog = mBuilder.show()


//            //Code for changing the picture of a place
                selectedImage = mAlertDialog.findViewById(R.id.placeImageView)!!
                dialogView.placeImageView.setOnClickListener {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            MY_CAMERA_PERMISSION_CODE
                        )
                    } else {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)
                    }
                }

//            //Confirm edit button
                dialogView.dialogEditButton.setOnClickListener {
                    mAlertDialog.dismiss()
                    val newDescr = dialogView.txtChangeName.text.toString()
                    dbHelper.updatePlace(element.name, newDescr.toString())
                    updateList(listView)
                }

//            //Cancel button
                dialogView.dialogCancelButton.setOnClickListener {
                    mAlertDialog.dismiss()
                }

//            //Delete button
                dialogView.dialogDeleteButton.setOnClickListener {
                    val element: Place = parent.getItemAtPosition(position) as Place
                    DeleteDialog().show(supportFragmentManager, element.name)
                }
                true
            }


            // Adding a new Place
            addPlaceBtn.setOnClickListener {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.new_dialog, null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle("New Place")
                val mAlertDialog = mBuilder.show()

                //Code for adding a picture of a place
                selectedImage = mAlertDialog.findViewById(R.id.newPictureView)!!
                dialogView.newPictureView.setOnClickListener {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            MY_CAMERA_PERMISSION_CODE
                        )
                    } else {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)
                    }
                }

                //Add new place button
                dialogView.dialogAddButton.setOnClickListener {
                    mAlertDialog.dismiss()
                    val newName = dialogView.txtPlaceName.text.toString()
                    val newDescription = dialogView.txtNote.text.toString()
                    dbHelper.insertPlace(Place(newName, newDescription, mUserReference.toString()))
                    updateList(listView)
                }

                //Cancel button
                dialogView.dialogNewCancelButton.setOnClickListener {
                    mAlertDialog.dismiss()
                }
            }

        }



        // Insert initial data in the sqlite db
        private fun initialListFill() {
//        dbHelper.insertUser(Place("PKIN", "Most known building in Warsaw"))
//        dbHelper.insertUser(Place("Warsaw Spire", "Business Centre"))

        }

        // Update the listView on the MainActivity
        @SuppressLint("SetTextI18n")
        private fun updateList(listView: ListView) {
            val placeList = dbHelper.readAllfromUser(mUserReference.toString())
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, placeList)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        // Reloading the MainActivity
        fun reloadActivity() {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }

        @SuppressLint("MissingSuperCall")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == CAMERA_REQUEST) {
                val img = data?.getExtras()?.get("data");
                selectedImage.setImageBitmap(img as Bitmap?)
            }
        }

        private fun initialise() {
            mDatabase = FirebaseDatabase.getInstance()
            mDatabaseReference = mDatabase!!.reference!!.child("Users")
            mAuth = FirebaseAuth.getInstance()
//        tvFirstName = findViewById<View>(R.id.tv_first_name) as TextView
//        tvLastName = findViewById<View>(R.id.tv_last_name) as TextView
//        tvEmail = findViewById<View>(R.id.tv_email) as TextView
//        tvEmailVerifiied = findViewById<View>(R.id.tv_email_verifiied) as TextView
        }

        override fun onStart() {
            super.onStart()

//            val mUser = mAuth!!.currentUser
//            val mUserReference = mDatabaseReference!!.child(mUser!!.uid)

//        tvEmail!!.text = mUser.email
//        tvEmailVerifiied!!.text = mUser.isEmailVerified.toString()

            mUserReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                user.getUid.value as String
//                tvLastName!!.text = snapshot.child("lastName").value as String
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    }
