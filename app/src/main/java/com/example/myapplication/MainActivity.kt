package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.classes.Note
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextPriority: EditText
    private lateinit var editTextTags: EditText
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private lateinit var textViewData: TextView


    //private lateinit var listener:ListenerRegistration  //allows us to remove a listener when we remove the app
    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance()  //getting an instance of the db

    // private val docRf: DocumentReference = db.collection("Notebook").document("My first note")
    private val noteBookRf: CollectionReference =
        db.collection("Notebook") //this allows us to add new notes to it and its refers to a collection of documents
    private var lastResult: DocumentSnapshot? = null  //a document snapshot and its nullable

    //a document reference in our db so we don't keep on typing this over and over again
    private val KEY_TITLE = "title"  //for mutable maps
    private val KEY_DESCRIPTION = "description" //same as above
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextTitle = findViewById(R.id.edit_text_title)
        editTextDescription = findViewById(R.id.edit_text_description)
        saveButton = findViewById(R.id.button_add_button)
        loadButton = findViewById(R.id.load_button)
        textViewData = findViewById(R.id.text_view_data)
        editTextPriority = findViewById(R.id.edit_text_priority)
        editTextTags = findViewById(R.id.edit_text_tags)


        saveButton.setOnClickListener {
            addNote()
        }

        loadButton.setOnClickListener {
            loadNotes()
        }

        updateObjects()
    }


    override fun onStart() {
        super.onStart()

        /* noteBookRf.orderBy("priority").addSnapshotListener(this) { snapshot, error ->
             error?.let {
                 return@addSnapshotListener
             }
             snapshot?.let {
                 for (dc in it.documentChanges) //basically we are looping to check changes in the documents
                 {
                     val id = dc.document.id
                     val oldIndex = dc.oldIndex
                     val newIndex = dc.newIndex

                     when (dc.type) {
                         DocumentChange.Type.ADDED -> {
                             textViewData.append(
                                 "\nAdded: $id" +
                                         "\nOld Index: $oldIndex New Index: $newIndex"
                             )
                         }

                         DocumentChange.Type.REMOVED -> {
                             textViewData.append(
                                 "\nRemoved: $id" +
                                         "\nOld Index: $oldIndex New Index: $newIndex"
                             )
                         }

                         DocumentChange.Type.MODIFIED -> {
                             textViewData.append(
                                 "\nModified: $id" +
                                         "\nOld Index: $oldIndex New Index: $newIndex"
                             )
                         }
                     }
                 }
             }
         }*/
    }


    private fun addNote() {
        val title = editTextTitle.text.toString()
        val description = editTextDescription.text.toString()
        /*  //THE FIRST METHOD MAP
            val note =
                mutableMapOf<String, Any>()  //in firebase data is stored in pairs so we need a mutable map
            note.put(KEY_TITLE, title)
            note.put(KEY_DESCRIPTION, description)

         */
        if (editTextPriority.text.toString().isEmpty()) {
            editTextPriority.setText("0")
        }
        val tagsArray =
            editTextTags.text.toString().trim().split(",")//remove the spaces and split by ,
        // val tags = tagsArray.toMutableList() //converting the edit text contents to a mutable list (normal mutable list part)
        val tags = mutableMapOf<String, Boolean>()

        for (tag in tagsArray) {
            tags[tag] = true  //assigning the all the initial boolean values(nested object) of the tags to true
        }
        val priority = editTextPriority.text.toString().toInt()
        val note = Note(title, description, priority, tags)

        noteBookRf.add(note)  //adds a new document
            //or u can type docRf.set(note) since we made a reference
            //setting a collection name and a document name firebase can do it auto tho then setting it to the note map
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Note added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, "Error: note was not added!", Toast.LENGTH_SHORT)
                    .show()

            }

    }

    private fun loadNotes() {

         noteBookRf.whereEqualTo(
             "tags.tag1",          //here its tags.tag1 since its a mutable map if its a list like before just pass tag
             true
         ) //only displays the document with the tag value of 6 (noteBookRf.whereArrayContains("tags",6)
             .get()
             .addOnSuccessListener {
                 var data = " "

                 for (documentsnapshot in it) {
                     val note = documentsnapshot.toObject(Note::class.java)
                     note.id = documentsnapshot.id
                     data += "ID: ${note.id}"

                     for (tag in note.tags?.keys!!) {  //this retrieves only the keys not the values(boolean) (for the normal mutable list remove the keys!!)
                         data += "\n- $tag"
                     }
                     data += "\n\n"
                 }
                 textViewData.text = data


             }.addOnFailureListener {
                 Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
             }
    }

    /*
.addOnSuccessListener {  //we are basically looping through a query of document snapshots
        querydocumentsnapshots ->
    var data = " "  //used to append the data we get

    for (documentSnapshot in querydocumentsnapshots) {

        val note = documentSnapshot.toObject(Note::class.java)
        val title = note.title
        val description = note.description
        val priority = note.priority
        data += "Title: " + title + "\nDescription: $description" +
                "\nPriority: $priority \n\n"
    }
    textViewData.text = data
}.addOnFailureListener {
    Log.d(TAG,it.toString())  //the link to make the query online
}

     */

    private fun updateObjects() {  //look in the db for the results

        noteBookRf.document("oaYxI9EavkD0qY5u61M6").update("tags.tag1",false)
        noteBookRf.document("D7DL6DVk3Nc2ze7ZSRhF").update("tags.tag1",FieldValue.delete())  //deletes the boolean value of tag1 in this document

        //noteBookRf.document("oaYxI9EavkD0qY5u61M6 ").update("tags", FieldValue.arrayRemove("tag3"))
        //arrayRemove removes the element of value tag3

        //.update("tags",FieldValue.arrayUnion("new tag"))
        //array union has a parameter of var arg and allows us to add elements to array in this document
    }

}




