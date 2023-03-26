package com.example.image_detector

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.image_detector.ml.MobilenetV110224Quant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {
    public lateinit var bitmap: Bitmap
    public lateinit var imgView : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileName = "labels.txt"
        val inputString = application.assets.open(fileName).bufferedReader().use{it.readText()}
        var townList = inputString.split("\n")
        setContentView(R.layout.activity_main)

        this.imgView = findViewById(R.id.imageView);
        var tv:TextView = findViewById(R.id.textView)


        var selectButton = findViewById<Button>(R.id.selectB)

        selectButton.setOnClickListener(View.OnClickListener {

            var intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.type = "image/*"
            startActivityForResult(intent , 100);
        })

        var predict:Button = findViewById(R.id.preBut)
        predict.setOnClickListener(View.OnClickListener {
            var resized: Bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val model = MobilenetV110224Quant.newInstance(this)

// Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
            var tBuffer = TensorImage.fromBitmap(resized)
            var byteBuffer = tBuffer.buffer
            inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            var max = getMax(outputFeature0.floatArray)
            tv.setText(townList[max].toString())

// Releases model resources if no longer used.
            model.close()

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView.setImageURI(data?.data)
        var uri: Uri?= data?.data;
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    fun getMax(arr:FloatArray) : Int{
        var index = 0
        var min = 0.0f

        for(i in 0..1000){
            if(arr[i] > min){
                index = i
                min = arr[i]
            }
        }

        return index
    }
}