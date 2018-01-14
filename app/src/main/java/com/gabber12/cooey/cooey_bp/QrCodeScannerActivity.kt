package com.gabber12.cooey.cooey_bp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


import android.util.SparseArray
import android.widget.Toast
import com.android.volley.Request
import com.google.android.gms.vision.barcode.Barcode

import info.androidhive.barcode.BarcodeReader
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.AuthFailureError
import com.gabber12.cooey.cooey_bp.activity.HomeActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonObject




class QrCodeScannerActivity : AppCompatActivity(), BarcodeReader.BarcodeReaderListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner)
    }

    override fun onScanned(barcode: Barcode) {
        // single barcode scanned
        Log.i("barcode reading", barcode.displayValue)
        var scanned_url = barcode.displayValue
        var systolic_value = intent.getIntExtra("SYSTOLIC", -1)
        var diastolic_value = intent.getIntExtra("DISTOLIC", -1)
        var heart_rate_vale = intent.getIntExtra("HEART_RATE", -1)

        make_get_call(scanned_url, systolic_value, diastolic_value, heart_rate_vale)

    }

    fun make_get_call(scanned_url: String, systolic_value: Int,diastolic_value: Int, heart_rate_vale: Int) {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        // Request a string response from the provided URL.
        var final_scanned_url = scanned_url +  "&s=" + systolic_value + "&d=" + diastolic_value + "&h=" + heart_rate_vale
        val stringRequest = StringRequest(Request.Method.POST, final_scanned_url,
                object : Response.Listener<String> {
                    @Throws(AuthFailureError::class)
                    override fun onResponse(response: String) {
                        // Display the first 500 characters of the response string.
                        Log.i("Volley Response", response.toString())
                        val jsonObject = JsonParser().parse(response.toString()) as JsonObject
                        if(jsonObject.get("message").toString() == "Data Uploaded successfully!!!") {
                            Toast.makeText(applicationContext, "Data has been uploaded successfully !!", Toast.LENGTH_SHORT)
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError) {
                Log.i("Volley Response", "There was an error in the network call !!")
            }

             fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("Content-Type", "application/json")
                return params
            }
        })
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    override fun onScannedMultiple(list: List<Barcode>) {
        // multiple barcodes scanned
    }

    override fun onBitmapScanned(sparseArray: SparseArray<Barcode>) {
        // barcode scanned from bitmap image
    }

    override fun onScanError(s: String) {
        // scan error
    }

    override fun onCameraPermissionDenied() {
        // camera permission denied
    }
}