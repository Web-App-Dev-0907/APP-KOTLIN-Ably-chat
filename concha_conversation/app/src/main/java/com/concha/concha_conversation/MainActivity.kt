package com.concha.concha_conversation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import com.google.android.material.textfield.TextInputEditText

import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.types.ClientOptions
import io.ably.lib.rest.AblyRest
import io.ably.lib.realtime.Channel
import io.ably.lib.realtime.ChannelState
import io.ably.lib.realtime.CompletionListener
import io.ably.lib.types.ErrorInfo
import io.ably.lib.types.Message

class MainActivity : ComponentActivity() {

    private lateinit var ably: AblyRealtime
    private lateinit var ablyRest: AblyRest
    private lateinit var channel: Channel
    private val clientId = "12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)

        val myButton: Button = findViewById(R.id.sendButton)
        var messageInput: TextInputEditText = findViewById(R.id.inputName);
        myButton.setOnClickListener {
            // Call the function when the button is clicked
            sendMessage(messageInput.text.toString())
        }

        val receiveButton: Button = findViewById(R.id.receiveButton)
        receiveButton.setOnClickListener{
            receiveMessage()
        }
        InitializeAbly();
    }

    fun InitializeAbly() {
        val options = ClientOptions("rMgTzg.Moni7A:4J2M0_MjjjeZ7BLRJ2akL2AEJ7tj_2MjvxWZ0t_t2d4").apply {
            tls = true  // Ensure that TLS is enabled for port 443
            disconnectedRetryTimeout = 30000
        }

        ably = AblyRealtime(options)
        ablyRest = AblyRest("rMgTzg.Moni7A:4J2M0_MjjjeZ7BLRJ2akL2AEJ7tj_2MjvxWZ0t_t2d4");

        ably.connection.on { stateChange ->
            when (stateChange.current) {
                ConnectionState.connected -> {
                    Log.d("Ably", "Connected to Ably")
                    channel = ably.channels.get("my-channel");
                    checkChannelState()
                }
                ConnectionState.failed -> {
                    Log.e("Ably", "Connection failed: ${stateChange.reason?.message}")
                }
                else -> {
                    Log.d("Ably", "Connection state changed: ${stateChange.current}")
                }
            }
        }
    }

    private fun checkChannelState() {

        val state = channel.state

        when (state) {
            ChannelState.attached -> Log.d("Ably", "Channel is attached and ready to send/receive messages.")
            ChannelState.detached -> Log.d("Ably", "Channel is detached and not ready.")
            ChannelState.failed -> Log.d("Ably", "Channel has failed.")
            ChannelState.suspended -> Log.d("Ably", "Channel is suspended.")
            ChannelState.initialized -> Log.d("Ably", "Channel is initialized but not yet attached.")
            else -> Log.d("Ably", "Unknown channel state.")
        }
    }

    fun sendMessage(message: String){

        val messageData = Message("event-name", message, clientId)

        channel.publish(messageData, object : CompletionListener {
            override fun onSuccess() {
                Log.d("Ably", "Message sent successfully")
            }

            override fun onError(error: ErrorInfo?) {
                Log.e("Ably", "Error publishing message: ${error?.message}")
            }
        })
    }

    fun receiveMessage(){

        Log.d("button event", "Receive button clicked")

        channel.subscribe("event-name") { message ->

            if(message.clientId == clientId){
                Log.d("Ably", "Received message from client $clientId: ${message.data}")
            }else{
                Log.d("Ably", "Received message from another client: ${message.data}")
            }
        }
    }
}
