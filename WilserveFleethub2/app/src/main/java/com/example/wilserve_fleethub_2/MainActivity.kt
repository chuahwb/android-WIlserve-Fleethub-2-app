package com.example.wilserve_fleethub_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.wilserve_fleethub_2.databinding.ActivityMainBinding
import com.example.wilserve_fleethub_2.manager.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }
    //private lateinit var messageArrived:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setMqttCallBack()

    }

    fun logIn(view:View){
        if (binding.textInputEditTextUsername.text.isNullOrEmpty() || binding.textInputEditTextPassword.text.isNullOrEmpty()) {
            setErrorTextField(true,getString(R.string.login_error_blank))
        } else {
            val username = binding.textInputEditTextUsername.text.toString()
            val password = binding.textInputEditTextPassword.text.toString()
            checkValidity(username, password)
        }
    }

    private fun setErrorTextField(error:Boolean,errorMessage: String) {
        if(error){
            binding.employeePassword.error = errorMessage
        }else{
            binding.employeePassword.error = null
            binding.textInputEditTextPassword.text = null
            binding.textInputEditTextUsername.text = null
        }
    }

    private fun goToNextScreen(){
        val intent = Intent(this, ProcessActivity::class.java)
        startActivity(intent)
    }


    private fun checkValidity(username:String,password:String){
        val topicPub = "identity"
        val message = "{\"${topicPub}\":{\"username\":\"${username}\",\"password\":\"${password}\"}}"
        try {
            mqttClient.publish(topicPub, message)
        } catch (ex: MqttException) {
        }
        val topicSub = "validity"
        try {
            mqttClient.subscribe(topicSub)
        }catch (ex:MqttException){
        }
    }

    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                val snackbarMsg = "Connected to host:\n'$SOLACE_MQTT_HOST'."
                Log.w("Debug", snackbarMsg)
            }
            override fun connectionLost(throwable: Throwable) {
                val snackbarMsg = "Connection to host lost:\n'$SOLACE_MQTT_HOST'"
                Log.w("Debug", snackbarMsg)
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", "Message received from host '$SOLACE_MQTT_HOST': $mqttMessage")
                val arrivalMessage:String = mqttMessage.toString()
                validityResponse(arrivalMessage)
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$SOLACE_MQTT_HOST'")
            }
        })

    }

    private fun validityResponse(responseString:String){
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Validity::class.java)
        val response = adapter.fromJson(responseString)
        if(response?.validity == true){
            setErrorTextField(false,"")
            goToNextScreen()
        }else{
            setErrorTextField(true,getString(R.string.login_error_invalid))
        }
    }

    override fun onDestroy() {
        mqttClient.destroy()
        super.onDestroy()
    }
}